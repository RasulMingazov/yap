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
const report = handback(data.agent_transcript_path || "") || data.last_assistant_message || "";
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

function handback(file) {
  let found = "";
  try {
    for (const line of fs.readFileSync(file, "utf8").split("\n")) {
      let content;
      try {
        content = (JSON.parse(line).message || {}).content;
      } catch {
        continue;
      }
      for (const part of Array.isArray(content) ? content : []) {
        if (part && part.name === "SubagentHandback") found = (part.input || {}).message || found;
      }
    }
  } catch {
    // No transcript: the last message is the report.
  }
  return found;
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
  const editedTests = changed.filter((line) => "MDR".includes(line[0]) && TEST_DIR.test(name(line))).map(name);
  if (editedTests.length && !declared("Tests changed")) {
    problems.push(`The branch edits or deletes existing tests: ${editedTests.slice(0, 5).join(", ")}. `
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
  const build = run("sh", path.join(cwd, ".claude", "scripts", "build.sh"));
  if (build.code !== 0) {
    const tail = (build.stdout + build.stderr).trim().split("\n").slice(-40).join("\n");
    problems.push(`The build fails. Fix it before you report:\n${tail}`);
  }
}
