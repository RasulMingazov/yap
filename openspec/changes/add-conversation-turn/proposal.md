# Proposal

## Why

A topic is a dialogue with a character, step by step (#12). The server must check each answer of
the learner against the current step and let the character reply in role. One engine does this
turn, so the admin app (Try, #15) and later the mobile app use the same rules.

## What Changes

- A conversation of a topic at a level starts with the opening line of the topic and step 1. The
  start calls no model.
- A turn takes the conversation and the answer of the learner. It runs two model calls in order:
  - The step check: the Step check prompt (#10) with the task, Pass when, the last line of the
    interlocutor and the answers of the current step. It returns one verdict: `accepted`,
    `continue`, `unclear` or `not_accepted`. The step check does not depend on the level.
  - The character reply: the Character reply prompt (#10) with `{interlocutor}`, the setting, the
    role, `{level.rules}` (#9), the step and the verdict. The dialogue follows as messages. It
    returns an English line and its Russian translation.
- Only `accepted` completes the step. The next step becomes current. After the last step, the
  conversation is finished.
- Each call uses the model of its task for the level (#11): Step check uses the model of its task,
  and Character reply uses the model of the level when the level has one.
- The server adds the answer format of each call to the end of the prompt, and reads the answer
  strictly. An answer in another format is a failed call.
- A failed call, or an answer that the server cannot read, changes nothing in the conversation.
- A turn does not run, and changes nothing, when the answer is empty, the conversation is finished,
  the topic does not exist, the topic has no interlocutor, the current step has no task or no Pass
  when, a prompt has no saved text, or the level has no saved block.
- The server can call a model by its choice: it routes the call to the adapter of the provider. The
  DeepSeek adapter starts when the server has its key.
- With this change, the note on the Levels screen comes true for Character reply: the server puts
  the level block into the reply prompt as `{level.rules}`. Hints follow in #13.

Out of scope:

- Hints: #13. Review and recommendations: #14.
- The admin API of a trial run, its storage, and the Try panel: #15. This change holds no
  conversation state on the server: the caller keeps the conversation between turns.
- What the model got for "Why did the character answer this way?" (prompt version, model): #15.
- The mobile app API, the user progress, the rating update and the calibration: no issue yet.

## Capabilities

### New Capabilities

- `conversation`: the start of a conversation of a topic, the turn with the step check and the
  character reply, the verdicts, the step progress, the values of the prompt variables, the answer
  formats and the failures that change nothing. #13 and #14 add hints and the review to it.

### Modified Capabilities

None. The prompts, levels and task models keep their requirements; this change uses them.

## Impact

- A new module `server/feature-conversation`. It uses the use cases of `feature-topics`,
  `feature-characters`, `feature-levels`, `feature-models` and `feature-prompts`.
- `server/core-llm` gets `ModelProviders`: the call of a model by `ModelChoice`, and the DeepSeek
  adapter in Koin.
- `server/feature-prompts` gets the rule that fills the variables of a prompt.
- `server/app` lists the new Koin module. No route, no DTO, no `contract-admin` change.
- No table and no Flyway migration: the reserved `V8` is not needed.
- `settings.gradle.kts` and `CLAUDE.md` get the new module.
