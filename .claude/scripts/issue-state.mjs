// One file per issue in the active run: docs/process/runs/<run>/issues/<n>.md. The gate, the
// scripts and the agents write it; the orchestrator reads the summary instead of editing a table.
//   node .claude/scripts/issue-state.mjs <n> [key=value ...] [--log "text"]   set fields, add a log line
//   node .claude/scripts/issue-state.mjs --list [all]                          one line per open issue
import fs from "node:fs";
import path from "node:path";
import { execSync } from "node:child_process";
import { fileURLToPath } from "node:url";

const FIELDS = ["title", "stage", "part", "pr", "agent", "needs", "updated"];

export function mainCheckout(cwd = process.cwd()) {
  const common = execSync("git rev-parse --path-format=absolute --git-common-dir", { cwd, encoding: "utf8" }).trim();
  return path.dirname(common);
}

export function runDir(cwd = process.cwd()) {
  const runs = path.join(mainCheckout(cwd), "docs", "process", "runs");
  if (!fs.existsSync(runs)) return null;
  const active = fs.readdirSync(runs).sort().filter((dir) => {
    const state = path.join(runs, dir, "STATE.md");
    return fs.existsSync(state) && /^Status: active/m.test(fs.readFileSync(state, "utf8"));
  });
  return active.length ? path.join(runs, active.at(-1)) : null;
}

const now = () => new Date().toISOString().slice(0, 16) + "Z";

function parse(text) {
  const fields = {};
  const log = [];
  for (const line of text.split(/\r?\n/)) {
    const field = line.match(/^(\w+): (.*)$/);
    if (field && FIELDS.includes(field[1])) fields[field[1]] = field[2];
    else if (line.startsWith("- ")) log.push(line.slice(2));
  }
  return { fields, log };
}

function render(number, { fields, log }) {
  const head = FIELDS.filter((key) => fields[key]).map((key) => `${key}: ${fields[key]}`);
  return [`# #${number}`, "", ...head, "", "## Log", "", ...log.map((line) => `- ${line}`), ""].join("\n");
}

export function update(number, values = {}, logLine = null, cwd = process.cwd()) {
  const dir = runDir(cwd);
  if (!dir) return null;
  const file = path.join(dir, "issues", `${String(number).replace(/^#/, "")}.md`);
  fs.mkdirSync(path.dirname(file), { recursive: true });
  const state = fs.existsSync(file) ? parse(fs.readFileSync(file, "utf8")) : { fields: {}, log: [] };
  Object.assign(state.fields, values, { updated: now() });
  if (logLine) state.log.push(`${now()} · ${logLine}`);
  fs.writeFileSync(file, render(path.basename(file, ".md"), state));
  return file;
}

function list(all) {
  const dir = runDir();
  const issues = dir ? path.join(dir, "issues") : null;
  if (!issues || !fs.existsSync(issues)) return console.log("no issue files");
  fs.readdirSync(issues)
    .filter((name) => name.endsWith(".md"))
    .map((name) => ({ number: Number(name.slice(0, -3)), ...parse(fs.readFileSync(path.join(issues, name), "utf8")) }))
    .filter((row) => all || row.fields.stage !== "merged")
    .sort((a, b) => a.number - b.number)
    .forEach(({ number, fields, log }) => {
      const parts = [`#${number}`, fields.stage || "?", fields.title, fields.part && `part ${fields.part}`,
        fields.pr && `PR ${fields.pr}`, fields.agent, fields.needs && `needs ${fields.needs}`,
        log.length && `last: ${log.at(-1)}`];
      console.log(parts.filter(Boolean).join(" · "));
    });
}

if (process.argv[1] && path.resolve(process.argv[1]) === fileURLToPath(import.meta.url)) {
  const args = process.argv.slice(2);
  if (args[0] === "--list") {
    list(args[1] === "all");
  } else if (/^#?\d+$/.test(args[0] || "")) {
    const values = {};
    let logLine = null;
    for (let i = 1; i < args.length; i++) {
      if (args[i] === "--log") {
        logLine = args[++i];
        continue;
      }
      const [key, ...rest] = args[i].split("=");
      if (!FIELDS.includes(key)) throw new Error(`unknown field ${key}`);
      values[key] = rest.join("=");
    }
    const file = update(args[0], values, logLine);
    console.log(file ? `ISSUE #${args[0].replace(/^#/, "")} · ${path.relative(process.cwd(), file)}` : "no active run");
  } else {
    console.error("usage: issue-state.mjs <n> [key=value ...] [--log text] | --list [all]");
    process.exit(2);
  }
}
