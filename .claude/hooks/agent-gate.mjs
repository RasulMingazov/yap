// Return gate of an agent that pushes a branch (planner, executor). It may return only with a
// well-formed report and, after a plan or a code stage: a clean pushed branch, a passing build for
// code, and declared changes to protected files and existing tests. `Stage: blocked` returns
// without checks. After 3 blocks it returns anyway: CI and the review still check the branch.
import { spawnSync } from "node:child_process";
import fs from "node:fs";
import os from "node:os";
import path from "node:path";

const PROTECTED = [".github/", ".githooks/", ".claude/", "config/detekt/"];
const TEST_DIR = /\/src\/[A-Za-z]*[Tt]est[A-Za-z]*\//;

const data = JSON.parse(fs.readFileSync(0, "utf8") || "{}");
// A stop that comes with a coordinator message can lack the subagent transcript: read it at its
// standard path too.
const transcripts = [data.agent_transcript_path, subagentTranscript(data.transcript_path, data.agent_id)].filter(Boolean);
const handed = transcripts.map(handback).find(Boolean) || "";
const report = handed || data.last_assistant_message || "";
// The agent has a message that it has not answered yet: it works on it and returns again.
if (!handed && transcripts.some(waiting)) process.exit(0);
const agent = data.agent_id || "unknown";
const project = process.env.CLAUDE_PROJECT_DIR || data.cwd || process.cwd();
const worktree = path.join(project, ".claude", "worktrees", `agent-${agent}`);
const cwd = fs.existsSync(worktree) ? worktree : data.cwd || process.cwd();
const counter = path.join(os.tmpdir(), `agent-gate-${agent}`);
const stage = (field("Stage") || "").toLowerCase().split(/[\s—-]/)[0];

const problems = [];
const missing = ["Stage", "Issue", "Verification"].filter((name) => !field(name));
if (missing.length) problems.push(`The report has no line for: ${missing.join(", ")}. Use the report format.`);
if (["plan", "build", "review", "archive"].includes(stage)) checkBranch();

if (!problems.length) {
  fs.rmSync(counter, { force: true });
  process.exit(0);
}

const blocks = (fs.existsSync(counter) ? Number(fs.readFileSync(counter, "utf8")) : 0) + 1;
if (blocks > 3) {
  fs.rmSync(counter, { force: true });
  process.exit(0);
}
fs.writeFileSync(counter, String(blocks));
const hint = blocks >= 2 ? "\nIf you cannot fix this, return `Stage: blocked` with what you tried." : "";
console.log(JSON.stringify({ decision: "block", reason: `Gate ${blocks}/3:\n- ${problems.join("\n- ")}${hint}` }));

// <session>.jsonl → <session>/subagents/agent-<id>.jsonl
function subagentTranscript(parent, id) {
  return parent && id && parent.endsWith(".jsonl") ? path.join(parent.slice(0, -".jsonl".length), "subagents", `agent-${id}.jsonl`) : "";
}

function entries(file) {
  let lines = [];
  try {
    lines = fs.readFileSync(file, "utf8").split("\n");
  } catch {
    // No transcript: the last message is the report.
  }
  return lines.flatMap((line) => {
    try {
      return [JSON.parse(line)];
    } catch {
      return [];
    }
  });
}

function handback(file) {
  let found = "";
  for (const entry of entries(file)) {
    const content = (entry.message || {}).content;
    for (const part of Array.isArray(content) ? content : []) {
      if (part && part.name === "SubagentHandback") found = (part.input || {}).message || found;
    }
  }
  return found;
}

// A message came after the agent's last turn. A tool result is not a message.
function waiting(file) {
  let unanswered = false;
  for (const entry of entries(file)) {
    const content = (entry.message || {}).content;
    if (entry.type === "assistant") unanswered = false;
    else if (entry.type === "user") unanswered = !(Array.isArray(content) && content.some((part) => part && part.type === "tool_result"));
  }
  return unanswered;
}

function field(name) {
  const match = report.match(new RegExp(`^\\s*${name}:\\s*(.+)$`, "mi"));
  return match ? match[1].trim() : null;
}

function declared(name) {
  return !(field(name) || "none").toLowerCase().startsWith("none");
}

function run(cmd, ...args) {
  const result = spawnSync(cmd, args, { cwd, encoding: "utf8", maxBuffer: 64 * 1024 * 1024 });
  return { code: result.status ?? 1, stdout: result.stdout || "", stderr: result.stderr || "" };
}

function checkBranch() {
  const base = `origin/${(field("Base") || "develop").replace(/^origin\//, "")}`;
  run("git", "fetch", "-q", "origin");
  const changed = run("git", "diff", "--name-status", `${base}...HEAD`).stdout.split("\n").filter(Boolean);
  const name = (line) => line.split("\t").at(-1);
  if (changed.some((line) => PROTECTED.some((prefix) => name(line).startsWith(prefix))) && !declared("Protected")) {
    problems.push("The branch changes protected files (CI, git hooks, Detekt config, .claude). "
      + "Undo that, or name each file and the reason in `Protected:`.");
  }
  // Only a deleted or changed line can weaken a test: added tests need no declaration.
  const editedTests = run("git", "diff", "--numstat", "--diff-filter=MDR", `${base}...HEAD`).stdout.split("\n")
    .map((line) => line.split("\t"))
    .filter(([, deleted, file]) => file && deleted !== "0" && TEST_DIR.test(file))
    .map(([, , file]) => file);
  if (editedTests.length && !declared("Tests changed")) {
    problems.push(`The branch deletes or changes lines of existing tests: ${editedTests.slice(0, 5).join(", ")}. `
      + "Do not weaken a test to pass. Name the spec requirement in `Tests changed:`, or return "
      + "`Stage: blocked` if the test and the spec conflict.");
  }
  if (run("git", "status", "--porcelain").stdout.trim()) {
    problems.push("The worktree has uncommitted changes. Commit and push them.");
    return;
  }
  const unpushed = run("git", "log", "@{u}..", "--oneline");
  if (unpushed.code !== 0) problems.push("The branch is not pushed. Push it.");
  else if (unpushed.stdout.trim()) problems.push("The branch has commits that are not pushed. Push them.");
  const textOnly = changed.every((line) => name(line).startsWith("openspec/") || name(line).endsWith(".md"));
  if (problems.length || textOnly) return;
  const common = run("git", "rev-parse", "--path-format=absolute", "--git-common-dir").stdout.trim();
  if (fs.existsSync(path.join(common, "build-ok", run("git", "rev-parse", "HEAD").stdout.trim()))) return;
  // The script of the main checkout: a branch on an older base may not have it.
  const build = run("sh", path.join(project, ".claude", "scripts", "build.sh"));
  if (build.code !== 0) {
    const tail = (build.stdout + build.stderr).trim().split("\n").slice(-40).join("\n");
    problems.push(`The build fails. Fix it before you report:\n${tail}`);
  }
}
