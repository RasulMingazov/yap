// The state of the Claude review of the head commit of a PR, for the orchestrator's scripts.
//   node .claude/scripts/pr-review.mjs <pr>             the review state and one line per open thread
//   node .claude/scripts/pr-review.mjs <pr> --resolve   also resolve the review threads that are done
// Exit 0: the newest «Claude review:» of the head commit says «No problems found» with no open
// thread, and with --resolve no thread stays open. A push that changes only openspec/ keeps the review of the commit
// before it. Exit 8: no review of the head commit yet. Exit 1: problems, or a gh call failed.
import { spawnSync } from "node:child_process";

const OWNER_WAIT = "Owner decision needed";
const QUERY = `query($owner: String!, $name: String!, $pr: Int!) {
  repository(owner: $owner, name: $name) {
    pullRequest(number: $pr) {
      headRefOid
      comments(last: 50) { nodes { author { login __typename } body createdAt } }
      reviewThreads(first: 100) {
        nodes { id isResolved path line comments(first: 20) { nodes { author { login __typename } body createdAt } } }
      }
    }
  }
}`;

const [pr, flag] = process.argv.slice(2);
if (!/^\d+$/.test(pr || "")) {
  console.log("usage: pr-review.mjs <pr> [--resolve]");
  process.exit(1);
}
const query = gh("api", "graphql", "-F", "owner={owner}", "-F", "name={repo}", "-F", `pr=${pr}`, "-f", `query=${QUERY}`);
if (!query.ok) {
  console.log(`FAILED · cannot read PR #${pr}: ${query.err.split("\n")[0]}`);
  process.exit(1);
}
const { headRefOid: head, comments, reviewThreads } = JSON.parse(query.out).data.repository.pullRequest;
const open = reviewThreads.nodes.filter((thread) => !thread.isResolved);

// The repository is public: only the review bot's comment counts as a review.
const review = comments.nodes.filter((comment) => isBot(comment.author) && comment.body.startsWith("Claude review:")).at(-1);
const reviewed = review?.body.match(/^Reviewed:\s*([0-9a-f]{7,40})\s*$/m)?.[1];
if (!reviewed || !(head.startsWith(reviewed) || onlySpecsSince(reviewed))) {
  console.log(reviewed
    ? `REVIEW WAIT · the newest review is of ${reviewed.slice(0, 7)}, the head is ${head.slice(0, 7)}`
    : `REVIEW WAIT · no Claude review of ${head.slice(0, 7)} yet`);
  open.forEach(printThread);
  process.exit(8);
}
const summary = review.body.split("\n")[0].replace(/^Claude review:\s*/, "");
// «No problems found» counts only new problems: the review must also report no open thread.
const openCount = Number(review.body.match(/(\d+) open threads?|open threads: (\d+)/i)?.slice(1).find(Boolean) ?? 0);
const clean = /^No problems found/i.test(summary) && openCount === 0;
console.log(`REVIEW ${clean ? "CLEAN" : "PROBLEMS"} · ${head.slice(0, 7)} · ${summary}`);

let left = 0;
for (const thread of open) {
  if (flag === "--resolve" && clean && isDone(thread.comments.nodes) && resolve(thread.id)) {
    console.log(`RESOLVED ${where(thread)}`);
  } else {
    printThread(thread);
    left++;
  }
}
process.exit(clean && (flag !== "--resolve" || left === 0) ? 0 : 1);

function gh(...args) {
  const result = spawnSync("gh", args, { encoding: "utf8", maxBuffer: 64 * 1024 * 1024 });
  return { ok: result.status === 0, out: result.stdout || "", err: (result.stderr || "").trim() };
}

// GraphQL gives the bot's login without the [bot] suffix.
function isBot(author) {
  return author?.__typename === "Bot" && author.login === "claude";
}

// A thread of the bot is done when the bot wrote «Agreed.» last, or when the author wrote last
// before the clean review: the review saw that reply and did not answer it, so the point is fixed.
// A thread where the bot wrote anything else last, or that waits for the owner, stays open.
function isDone(messages) {
  const last = messages.at(-1);
  if (!isBot(messages[0]?.author) || messages.some((message) => message.body.includes(OWNER_WAIT))) return false;
  return isBot(last.author) ? last.body.trim().startsWith("Agreed") : Date.parse(last.createdAt) < Date.parse(review.createdAt);
}

function onlySpecsSince(sha) {
  const compare = gh("api", `repos/{owner}/{repo}/compare/${sha}...${head}`, "--jq", "{status, files: [.files[].filename]}");
  if (!compare.ok) return false;
  const { status, files } = JSON.parse(compare.out);
  return status === "ahead" && files.every((file) => file.startsWith("openspec/"));
}

function resolve(id) {
  const mutation = "mutation($id: ID!) { resolveReviewThread(input: { threadId: $id }) { thread { isResolved } } }";
  return gh("api", "graphql", "-f", `id=${id}`, "-f", `query=${mutation}`).ok;
}

function where(thread) {
  return thread.line ? `${thread.path}:${thread.line}` : thread.path;
}

function printThread(thread) {
  const last = thread.comments.nodes.at(-1);
  console.log(`THREAD ${where(thread)} · ${last?.author?.login}: ${(last?.body || "").replace(/\s+/g, " ").slice(0, 100)}`);
}
