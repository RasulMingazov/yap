# Design

## Context

- `server/core-llm` (#5, #11) has `ModelProvider` (`json`, `text`), `ModelRequest(messages, model)`,
  `ModelReply` (`Answered`, `Failed`), `ModelChoice(provider, model)`, the catalog and the DeepSeek
  adapter. The adapter maps every failure, a timeout, a stop that is not `stop` and blank content
  to `ModelReply.Failed`. No Koin module binds a `ModelProvider`: #11 left "a router of providers"
  to this change.
- `feature-prompts` (#10): `PromptKey.TOPIC_CHECK` and `PromptKey.TOPIC_REPLY` with their variables,
  and `GetPromptUseCase`, which returns the `Prompt`; its `current` is null when no text was saved. The archived design of #10
  fixes the values of the variables and how an engine sends a prompt (table below).
- `feature-levels` (#9): `GetLevelRulesUseCase(code)` returns the saved block or `null`. It has no
  caller yet; this change is its caller.
- `feature-models` (#11): `GetTaskModelUseCase(code, task)` returns the `ModelChoice` of a call. It
  reads the choices at each call, and applies the level model only to `TOPIC_REPLY` and
  `TOPIC_HINTS`.
- `feature-topics` (#7, #8): `GetTopicUseCase(topicId)` returns `TopicDetails` with the `Topic`:
  setting, role, `Interlocutor` (`Cast(characterId)` or `Guest(description)`), `OpeningLine` and
  ordered `Step`s. A draft may have empty fields.
- `feature-characters` (#6): `GetCharacterUseCase(characterId)` returns the profile.
- The design canvas (Admin, Prompts) shows the Step check and Character reply texts. Both end with
  "Return JSON", with `verdict` for the check and `line` and `translation` for the reply.

## Goals / Non-Goals

**Goals:**

- One turn rule that #15 and the mobile app call.
- Tests with the fake provider (`StubModelProvider`) for each verdict and each failure.
- The end-to-end check, owned by this change (decision of 2026-11-07), that a saved level block
  shows in the next reply prompt.

**Non-Goals:**

- A route, a DTO or a stored conversation. #15 adds the admin API of a trial run and decides where
  a trial run lives.
- A record of the prompt versions and the models of a turn for "Why did the character answer this
  way?" (#15).
- A second adapter, retries, streaming, `max_tokens`.

## Decisions

### The engine keeps no state: the caller holds the conversation

`Conversation` is a value. A turn takes it and returns a new one, or an outcome that leaves it as it
was. "A failed model call changes nothing" then holds by construction: nothing is written before
both calls succeed.

```text
Conversation(topicId: TopicId, level: LevelCode, lines: List<ConversationLine>, currentStep: Int?)
  currentStep: from 1; null when the conversation is finished
ConversationLine = Interlocutor(english, translation) | Learner(text, step: Int, verdict: Verdict)
Verdict = ACCEPTED | CONTINUE | UNCLEAR | NOT_ACCEPTED   (wire: accepted, continue, unclear, not_accepted)
Conversation.start(topicId, openingLine, level)  -> lines [Interlocutor(opening)], currentStep 1
Conversation.answered(answer, verdict, reply, stepCount) -> the answer, the reply, and the next step
```

| Option | Choice |
| --- | --- |
| A stateless value that the caller keeps | yes: no table, no migration, a failure writes nothing |
| A conversation table in this change | no: no consumer yet; #15 says trial runs are not user progress, and the user progress has no issue |

So the reserved Flyway `V8` is not used. The orchestrator can give it to the next plan.

The turn reads the topic at each turn, so an edit of a draft applies to the next turn of a trial.
A step that the edit removed gives the reason "no current step".

### The module `server/feature-conversation` (owner decision, 2026-11-08: allow direct dependencies)

The engine uses five features. It goes into its own module, `feature-conversation`, which depends on
`feature-topics`, `feature-characters`, `feature-levels`, `feature-models` and `feature-prompts`.
None of them depends on it, so there is no cycle. #13 (hints) and #14 (review) add to the same
module.

- The rule: `docs/server/001-structure.md:15-17` lets a feature use another feature through its use
  cases, its ports and the model types in their signatures, when the dependency has no cycle.
- The precedents on the server: `feature-topics -> feature-characters` (#7) and
  `feature-models -> feature-levels` (#11).
- The owner rule "no feature depends on another" (2026-11-08) was said for the admin app features.
  Asked for the server, the owner decided on 2026-11-08: allow direct dependencies. So
  `feature-conversation` depends on `feature-topics`, `feature-characters`, `feature-levels`,
  `feature-models`, `feature-prompts` and `core-llm`.

| Option | Cost | Choice |
| --- | --- | --- |
| Direct dependencies on the five features, through their public use cases | none: the use cases exist and are public | yes (owner decision) |
| Ports in `feature-conversation/usecase/` (topic, interlocutor, prompt text, level rules, task model), and adapters in `app` that call each feature's use case | about 5 adapters and their tests in `app`, ~150 lines and one more part; `app` then holds glue code that `docs/server/001-structure.md:8-9` does not give it | no |
| Ports in each provider feature | each of them then depends on `feature-conversation`: a cycle | no |
| The engine in `feature-topics` | `feature-topics` then depends on prompts, levels and models; the topic content and the dialogue are two slices | no |

### `ModelProviders` routes a call by its choice

```kotlin
class ModelProviders(
    private val providers: Map<ModelProviderId, ModelProvider>,
) : AutoCloseable {

    suspend fun json(choice: ModelChoice, messages: List<ModelMessage>): ModelReply<JsonObject>
}
```

- It sends `ModelRequest(messages, choice.model)` to the provider of `choice.provider`.
- A provider without a key is not in the map: the call logs a warning and returns `Failed`.
- `coreLlmModule(config)` builds it in a lambda, because no public constructor reads `LlmConfig`:
  the map holds `DeepSeekModelProvider(config.deepSeek)` when the key exists.
  The adapter opens an HTTP client, a resource, so the definition sets `createdAtStart = true` and
  closes each adapter in `onClose`, as `docs/server/003-dependency-injection.md` says: a failure
  stops the start, not the first turn.
- Tests build `ModelProviders(mapOf(DEEPSEEK to StubModelProvider(...)))`: the fake provider of #5.

Only `json` is added: both calls of this change read JSON. A `text` route comes with its first
caller.

Alternative: bind one `ModelProvider` in Koin. Rejected: the choice of #11 names a provider, so
something must route by it.

### How the prompts are filled and sent

The values follow the archived design of #10:

| Variable | Value |
| --- | --- |
| `{interlocutor}` | Cast: three lines `Name: <name>`, `Personality: <personality>`, `Manner: <manner of speech>`. Guest: the guest description. |
| `{topic.setting}`, `{topic.role}` | The setting and the role of the topic. |
| `{level.rules}` | The saved block of the level (`GetLevelRulesUseCase`). |
| `{step.n}`, `{step.task}`, `{step.passWhen}` | The checked step: its number from 1, its task, its Pass when. |
| `{last.line}` | The English of the last interlocutor line before the answer. |
| `{answers}` | The answers on the checked step, with the new one, one per line, the newest last. |
| `{check.verdict}` | The verdict of the check, in its wire form. |

- `feature-prompts` gets the rule `PromptKey.fill(text, values)`: it replaces each variable of the
  key with its value and leaves other text in braces. A missing value for a variable of the key is a
  broken invariant and throws. #13 and #14 use the same rule.
- Step check: one `SYSTEM` message, the filled prompt and the format. Its variables carry the
  dialogue of the step (#10: "the system message is the whole request").
- Character reply: the `SYSTEM` message, the filled prompt and the format, then the dialogue, oldest
  first: each interlocutor line as `ASSISTANT` (English only), each answer as `USER`, the new answer
  last.
- History is not in `{interlocutor}`: #10 fixed three lines. The owner can change that later in #10's
  value table; it is not part of this change.

### The server owns the answer format

The server adds the format after a blank line at the end of the system message, and reads the
answer strictly:

```text
Step check:  Answer only with json: {"verdict": "accepted" | "continue" | "unclear" | "not_accepted"}
Reply:       Answer only with json: {"line": "<the English line>", "translation": "<its Russian translation>"}
```

- Check: `verdict` must be one of the four wire values, after the spaces are removed.
- Reply: `line` and `translation` must be strings, not empty after the spaces are removed. The
  server stores them trimmed.
- Anything else is a failed call.

| Option | Choice |
| --- | --- |
| The server adds the format, beside the code that reads it | yes |
| The prompt text of the owner holds the format, as in the canvas | no: one edit on Prompts could break every turn, and the reader is code |

DeepSeek JSON mode needs `response_format: json_object` (the adapter sets it in `json`), and the
word "json" in the system or user prompt. It says to give an example of the JSON. It can return
blank content; the adapter already maps that to `Failed`. The fixed format line meets these terms
whatever the owner writes. Source: <https://api-docs.deepseek.com/guides/json_mode> (read on
2026-11-08). A format line that the owner keeps in the prompt repeats it; the repeat does no harm,
and the owner can remove it.

### The turn reads everything before the first call

```text
TakeTurnUseCase(conversation, answer) -> TurnOutcome
  1. trim the answer; empty -> NotRun(EmptyAnswer); finished -> NotRun(Finished)
  2. topic (GetTopicUseCase) -> NotRun(TopicNotFound); no interlocutor -> NotRun(NoInterlocutor)
     empty setting, role, opening line (English) or guest description -> NotRun(SceneIncomplete)
     no current step -> NotRun(NoStep); empty task or Pass when -> NotRun(StepIncomplete)
  3. interlocutor value (GetCharacterUseCase for Cast; a missing character throws: the topic save checks it)
  4. prompts TOPIC_CHECK and TOPIC_REPLY -> NotRun(PromptMissing(key)); level block -> NotRun(LevelNotSaved)
  5. models: GetTaskModelUseCase(level, TOPIC_CHECK) and (level, TOPIC_REPLY)
     a provider without a key (ProviderConnections) -> NotRun(ProviderNotConnected(provider))
  6. check call -> Failed; reply call -> Failed
  7. Answered(conversation.answered(...), verdict)

TurnOutcome = Answered(conversation, verdict) | Failed | NotRun(TurnGap)
TurnGap = EmptyAnswer | Finished | TopicNotFound | NoInterlocutor | SceneIncomplete | NoStep
        | StepIncomplete | PromptMissing(promptKey) | LevelNotSaved | ProviderNotConnected(provider)
```

No model is called until every input exists, as the spec says. A turn of a topic that is not ready
costs nothing.

- A draft can have an empty setting, role, opening line or guest description: `TopicPublishRule`
  checks them only at publish, and a trial runs on drafts. Each of them goes into the reply prompt
  (the opening line also into `{last.line}` and the first `ASSISTANT` message), so an empty one
  stops the turn with `SceneIncomplete`, for the same reason as a level without a block. The
  translation of the opening line goes into no prompt, so it does not stop the turn.
- A provider without a key stops the turn with `ProviderNotConnected` before the first call, so the
  outcome names the reason. `ModelProviders` still returns `Failed` for it, as a guard.

| Option for a level without a saved block | Choice |
| --- | --- |
| The turn does not run and names the level | yes: the owner sees why, and saves the levels once |
| An empty `{level.rules}` | no: the reply would ignore the level without a sign |

The use case is `internal`. #15 adds the routes in the same module, or widens it.

### The Levels note

The note under the block on Levels says that the server puts the block into the reply and hints
prompts as `{level.rules}`. After this change it is true for Character reply; #13 makes it true for
Hints. The copy stays as it is.

## Risks / Trade-offs

- [Two calls per answer double the wait and the cost] → The check uses the cheap default
  `deepseek-flash`; the adapter timeout (60 s) bounds each call. #15 shows the progress.
- [A model call fails after the check passed] → The turn fails and the step does not move. The next
  answer runs the check again; no half-done turn exists.
- [The owner removes "json" from a prompt] → The fixed format line holds the word.
- [The caller sends a forged conversation] → Not in this change: there is no route. #15 keeps the
  trial run on the server, behind the admin key.
- [Profile text holds braces, such as `{answers}`] → The fill replaces only the variables of the key
  and does not fill values again, so a value is never treated as a variable.
