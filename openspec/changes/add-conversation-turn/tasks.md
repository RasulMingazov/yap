# Tasks

One group is one PR, in merge order: its size, its files (hot first). `llm` is
`server/core-llm/src/{main,test}/kotlin/app/yap/server/core/llm`, `fp` is
`server/feature-prompts/src/{main,test}/kotlin/app/yap/server/feature/prompts`, `fc` is
`server/feature-conversation/src/{main,test,testFixtures}/kotlin/app/yap/server/feature/conversation`.
The tests use the fake provider `StubModelProvider` of `core-llm` inside `ModelProviders`. No part
adds a migration, a route or a DTO. No part changes `contract-admin/`, `Application.kt`,
`gradle/libs.versions.toml` or `docs/`.

## 1. [1/8] The plan · this PR

- [x] 1.1 Write the change; verify with `openspec validate add-conversation-turn --strict`

## 2. [2/8] Call a model by its choice · ~150 · `llm/ModelProviders.kt`, `llm/CoreLlmModule.kt`, `llm/ModelProvidersTest.kt`

- [ ] 2.1 Add `ModelProviders(providers: Map<ModelProviderId, ModelProvider>)` with `json(choice, messages)`: it sends `ModelRequest(messages, choice.model)` to the provider of the choice; a provider that is not in the map logs a warning and returns `Failed`; `close()` closes each adapter that is `AutoCloseable`; verify with `ModelProvidersTest`: the stub gets the messages and the model of the choice, a `Failed` of the stub comes back as it is, and a provider not in the map returns `Failed` without a call
- [ ] 2.2 Bind it in `coreLlmModule(config)` in a lambda (the map holds `DeepSeekModelProvider` only when `config.deepSeek` is set), with `createdAtStart = true` (the adapter opens an HTTP client: `docs/server/003-dependency-injection.md`) and `onClose`; verify with the unchanged `AppModulesTest` and `ApplicationTest`, and a `ModelProvidersTest` case that a module without the key returns `Failed` for DeepSeek

## 3. [3/8] The conversation module and its state · ~260 · `settings.gradle.kts` (hot), `CLAUDE.md` (hot: the rows of `server/feature-conversation` and `server/feature-models`), `server/feature-conversation/build.gradle.kts`, `fc/model/` (`Conversation`, `ConversationLine`, `Verdict`), `fc/model/StubConversation.kt` (testFixtures), `fc/model/ConversationTest.kt`

- [ ] 3.1 Add `server/feature-conversation` with the dependencies this part uses (`feature-topics`, `feature-levels`); add its row and the missing row of `server/feature-models` (#11) to the module table of `CLAUDE.md`; verify with `./gradlew build`
- [ ] 3.2 Add `Conversation.start(topicId, openingLine, level)` (the opening line, step 1) and `answered(answer, verdict, reply, stepCount)`, with the answers of a step and the last interlocutor line; verify with the model tests for the start, `accepted` on step 1 of 3 (step 2), `continue`, `unclear` and `not_accepted` (step 1 stays), `accepted` on the last step (finished), the order answer then reply, the answers of a step after a `continue`, and the last line

## 4. [4/8] The prompt fill and the step check request · ~300 · `fp/model/PromptFill.kt` and its test, `server/feature-conversation/build.gradle.kts` (`feature-prompts`, `core-llm`), `fc/model/` (`StepCheckRequest`, `StepCheckAnswer`, `Verdict` wire values), their tests

- [ ] 4.1 Add `PromptKey.fill(text, values)`: it replaces each variable of the key, leaves other text in braces, does not fill a value again, and throws for a missing value; verify with the tests for each of these four cases
- [ ] 4.2 Build the step check messages (one `SYSTEM` message: the filled Step check prompt, a blank line, the format line of `design.md`) and read its answer (`verdict` out of the four wire values, trimmed; else no verdict); verify with the tests for the message with `{step.task}`, `{step.passWhen}`, `{last.line}` and `{answers}` filled and the format last, each of the four verdicts, a verdict with spaces, `maybe`, a missing `verdict`, and a `verdict` that is not a string

## 5. [5/8] The reply request · ~320 · `server/feature-conversation/build.gradle.kts` (`feature-characters`), `fc/model/` (`InterlocutorText`, `ReplyRequest`, `ReplyAnswer`), their tests

- [ ] 5.1 Build the value of `{interlocutor}`: three lines `Name:`, `Personality:`, `Manner:` for a character of the cast, the guest description for a guest; verify with the tests for both
- [ ] 5.2 Build the reply messages (the `SYSTEM` message with the filled Character reply prompt and the format line, then the dialogue oldest first: interlocutor lines as `ASSISTANT` in English, answers as `USER`, the new answer last), with `{step.n}`, `{step.task}` and `{check.verdict}` of the checked step; read the answer (`line` and `translation`, trimmed, not empty); verify with the tests for the system message after `not_accepted` on step 2, `{answers}` that stays as it is in the reply prompt, the dialogue of a second answer, a reply read with spaces, an empty translation, and a missing line

## 6. [6/8] The turn and the turns that do not run · ~350 · `server/feature-conversation/build.gradle.kts` (`feature-models`, the test fixtures of the five features and `core-llm`), `fc/model/` (`TurnOutcome`, `TurnGap`), `fc/usecase/TakeTurnUseCase.kt`, `fc/usecase/TakeTurnUseCaseTest.kt`

- [ ] 6.1 Add `TakeTurnUseCase(conversation, answer)`: it trims the answer, reads the topic, the interlocutor, both prompts, the level block, both models and their connection (`ProviderConnections`) before any call, runs the check and then the reply, and returns `Answered(conversation, verdict)`, or `Failed` with the unchanged conversation when a call fails; verify with the use case tests with the fake provider for each verdict: `accepted` (step 2 is current), `continue`, `unclear` and `not_accepted` (step 1 stays), and `accepted` on the last step (finished, the reply is still called)
- [ ] 6.2 Return `NotRun` with its `TurnGap`, and call no model, for an empty answer, a finished conversation, an unknown topic, no interlocutor, an incomplete scene (an empty setting, role, English opening line or guest description), no current step, a step without a task, a step without Pass when, each prompt without a saved text, a level without a block, and a provider without a key; verify with a test for each that also checks that the fake provider got no call; and return `Failed` for a failed check; verify with the test that the reply is not called and the conversation stays as it was

## 7. [7/8] The turn: failures, models and the level block · ~250 · `fc/usecase/TakeTurnUseCaseTest.kt`

- [ ] 7.1 Failures change nothing; verify with the tests for a failed reply after `accepted` and a check that answers `maybe`: each returns `Failed`, and the conversation stays as it was
- [ ] 7.2 Use `TOPIC_CHECK` and `TOPIC_REPLY` for the level; verify with the test: B1 with `deepseek-v4-pro` and both tasks with `deepseek-flash` check with `deepseek-flash` and reply with `deepseek-v4-pro`
- [ ] 7.3 The end-to-end check of #9; verify with the test of two turns at B1 with a new block saved between them (the stub level repository returns the new block): the first reply system message holds the old block, the second the new one; and with the test that a check at A2 and at C1 gets the same message

## 8. [8/8] The wiring and the archive · ~60 · `server/app/build.gradle.kts`, `server/app/src/main/kotlin/app/yap/server/AppModules.kt`, `fc/FeatureConversationModule.kt`, `openspec/`

- [ ] 8.1 Add `featureConversationModule()` with `factoryOf(::TakeTurnUseCase)` and list it in `appModules`; `Application.kt` stays as it is (no route); verify with `AppModulesTest` (`verify()`) and `./gradlew build`
- [ ] 8.2 Run `/opsx:archive` as the last commit; verify that `openspec/specs/conversation/spec.md` exists and `openspec validate --specs --strict` passes
