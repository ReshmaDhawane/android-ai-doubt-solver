# AI Doubt Solver - Android (Kotlin, Jetpack Compose)

An AI tutor chat app where students pick a subject (Maths, Physics, Chemistry, Programming, General) and ask study doubts. Built with **Android (Kotlin, Jetpack Compose)** as part of a cross-platform portfolio: the same product implemented in Flutter, React Native, native Android and native iOS.

## Features

- Chat with an AI tutor per subject; each subject keeps its own history
- Subject-specific system prompts (step-by-step maths, formulas for physics, code for programming)
- Works with any **OpenAI-compatible** Chat Completions API (OpenAI, Azure OpenAI, Groq, local LLM servers)
- **Offline demo mode**: with no API key the app uses a mock tutor, so it runs out of the box
- Chat history persisted locally and restored on app launch
- Failed questions are marked and can be retried without duplicating them
- Friendly error handling (network, invalid key, rate limit, server errors) with a 30 s timeout
- Only the last 12 messages are sent as context to control token cost
- Typing indicator, auto-scroll, long-press to copy, light/dark theme, accessibility labels
- Unit tests for the core chat logic

## Tech stack

- Kotlin 2.0, **Jetpack Compose** + Material 3 (dynamic color)
- Architecture: **MVVM + Clean Architecture** (domain / data / ui), StateFlow, Coroutines & Flow
- DI: **Hilt**
- Persistence: **Room** (reactive Flow queries)
- Networking: **Retrofit + OkHttp + kotlinx.serialization**
- Build: Gradle Kotlin DSL, version catalog, R8 for release
- Testing: JUnit4, MockK, Turbine, coroutines-test

## Architecture

```
app/src/main/java/com/reshma/aidoubtsolver/
  domain/          Models.kt (ChatMessage, Subject), ChatRepository.kt (interface)
  data/
    local/         Room: MessageEntity, MessageDao, AppDatabase
    remote/        Retrofit AiApi, AiDataSource (remote + mock)
    repository/    ChatRepositoryImpl
  di/              AppModule (Hilt)
  ui/
    chat/          ChatViewModel (StateFlow), ChatScreen (Compose)
    theme/         Theme.kt
app/src/test/      ChatViewModelTest
```

UI -> state holder (ViewModel / Cubit / Redux slice) -> repository / service abstraction -> HTTP API + local storage. The AI provider is behind an interface, so it is swapped for a mock in tests and in demo mode.

## Getting started

1. Open the folder in Android Studio (Koala or newer), let Gradle sync.
2. Optional, for real answers, add to `local.properties` (git-ignored):

```properties
AI_API_KEY=sk-...
AI_MODEL=gpt-4o-mini
```

3. Run the `app` configuration on an emulator or device (minSdk 24).

## Tests

```bash
./gradlew testDebugUnitTest
```

## Security note

API keys are never committed. They are read at build time from a git-ignored file / build flag. For production, route requests through your own backend so the key never ships inside the app.

## Author

**Reshma Dhawane** - Senior Mobile Developer (Flutter, React Native, Android, iOS)

## License

MIT
