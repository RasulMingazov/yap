// PostToolUse hook of /orchestrate. The model does not see its own context size, so this hook
// tells the orchestrator when the context passes the limit of docs/process/README.md
// («Context limit: <n>k»), and again every 50k more.
import fs from "node:fs";
import os from "node:os";
import path from "node:path";

const STEP = 50_000;

const data = JSON.parse(fs.readFileSync(0, "utf8") || "{}");
if (data.agent_id || !data.transcript_path) process.exit(0);

const project = process.env.CLAUDE_PROJECT_DIR || data.cwd || process.cwd();
const tokens = safely(() => context(data.transcript_path), 0);
const limit = safely(() => limitOf(path.join(project, "docs", "process", "README.md")), 500_000);
if (tokens < limit) process.exit(0);

const marker = path.join(os.tmpdir(), `context-watch-${data.session_id}`);
const last = fs.existsSync(marker) ? Number(fs.readFileSync(marker, "utf8")) : 0;
if (tokens < last + STEP) process.exit(0);
fs.writeFileSync(marker, String(tokens));

console.log(JSON.stringify({
  hookSpecificOutput: {
    hookEventName: "PostToolUse",
    additionalContext: `Context is ${Math.round(tokens / 1000)}k tokens, over the ${limit / 1000}k limit. `
      + "Take no new issue. Finish the issues that are in progress, then close the session as «Context» in "
      + "orchestrate.md says.",
  },
}));

function safely(read, fallback) {
  try {
    return read();
  } catch {
    return fallback;
  }
}

function limitOf(file) {
  const match = fs.readFileSync(file, "utf8").match(/Context limit:\s*(\d+)k/i);
  return match ? Number(match[1]) * 1000 : 500_000;
}

function context(file) {
  const size = fs.statSync(file).size;
  const length = Math.min(size, 512 * 1024);
  const buffer = Buffer.alloc(length);
  const fd = fs.openSync(file, "r");
  fs.readSync(fd, buffer, 0, length, size - length);
  fs.closeSync(fd);
  for (const line of buffer.toString("utf8").split("\n").reverse()) {
    if (!line.includes('"usage"')) continue;
    try {
      const usage = JSON.parse(line).message.usage;
      return (usage.input_tokens || 0) + (usage.cache_creation_input_tokens || 0) + (usage.cache_read_input_tokens || 0);
    } catch {
      // A cut first line or another entry: keep looking.
    }
  }
  return 0;
}
