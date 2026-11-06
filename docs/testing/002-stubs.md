# Test Stubs

Tests need reusable values and configurable collaborators without hiding the scenario. Each stub
also needs a clear owner.

## Value stubs

Build repeated models through an object named after the value:

```kotlin
internal object StubCharacter {

    const val ID = "character-id"
    const val NAME = "Sam"

    fun stubCharacter(
        id: String = ID,
        name: String = NAME,
        speech: Speech = stubSpeech(),
    ): Character = Character(
        id = CharacterId(id),
        name = name,
        speech = speech,
    )

    fun stubSpeech(
        manner: String = "Casual American English",
    ): Speech = Speech(
        manner = manner,
    )
}
```

- Name the object `StubX` and its base builder `stubX`.
- Keep every field a named, overridable parameter.
- Store reused primitive values as `const val` in the same object; use `val` for other types.
- Build nested values in the same object and use them as defaults in the root builder.
- Do not make nested builders private; tests should be able to call them and override their fields.
- Add named scenario builders only for recurring states, such as `stubPublishedTopic()`.

## Behavioral stubs

Use `stubcall` for behavioral collaborators. The repository does not have it yet. The first
behavioral stub adds it:

- copy the file-based Maven repository `libs/` from `RasulMingazov/yap-app`; it holds the
  published `stubcall` artifacts and nothing else;
- declare that directory as a Maven repository in `settings.gradle.kts`;
- add the coordinates and the version to `gradle/libs.versions.toml`, the same as in yap-app.

Do not `includeBuild` a sibling `stub-call` checkout: the committed repository keeps the build
independent from files outside this project.

```kotlin
internal class StubTopicRepository(
    topic: Topic = StubTopic.stubTopic(),
) : TopicRepository {

    val findCall = StubCall1.returns<TopicId, Topic?>(topic)

    override suspend fun find(id: TopicId): Topic? = findCall.invoke(id)
}
```

- Name the implementation `StubX` and keep it in its own file, in the test package of the
  production code that owns the contract.
- Expose a `StubCallN` property named after the delegated method, such as `invokeCall` or `findCall`.
- Accept the default result in the constructor and build it through the corresponding value stub.
- Match `N` to the method arity; use `unit()` for `Unit` methods and `returns(...)` for a default result.
- Delegate the method directly to `StubCallN`; do not duplicate return values, errors, counters,
  or captured arguments.
- Configure results and failures and verify calls through that property in tests.
- Server use-case tests stub repositories and adapters. Admin view-model tests stub use cases,
  never repositories.
- Expose stubbed observation as `MutableStateFlow` behind a read-only `StateFlow` or `Flow`,
  matching the contract being stubbed.
