# Dobby Discord Bot

Dobby is a Kotlin Discord bot service. It registers Discord application commands, forwards roast and fact requests to a
separate Dobby Core Backend, and exposes an internal HTTP endpoint so the backend can deliver processed roast results
back into Discord. Redis is used for caching and state management.

### Integrations

- **[Dobby Core](https://github.com/ALaa13/dobby-core)** — AI roast generation and fact storage backend
- **[Web Dashboard](https://github.com/ALaa13/dobby-web)** — View and manage roasts via browser

## What the Service Does

The service has three runtime parts:

1. Discord bot client
    - Logs in to Discord with `DISCORD_TOKEN`.
    - Registers slash commands either globally or to a development guild.
    - Handles user interactions from Discord.

2. Backend HTTP client
    - Makes outbound HTTP calls to Dobby Core Backend for roast and fact processing.
    - Configured with `DOBBY_BACKEND_URL` environment variable.

3. Redis client
    - Connects to Redis for caching and state management.
    - Configured via `REDIS_HOST` and `REDIS_PORT` environment variables.

## Commands

### `/ping`

Checks whether the bot is responsive and returns a simple latency measurement.

### `/roast`

Collects recent messages from the current Discord channel, filters out bot and blank messages, then sends the message
history to Dobby Core Backend.

Options:

- `count`: Optional number of recent messages to read. Supported choices are 10, 50, 100, 250, and 500. Defaults to 50.
- `persona`: Optional free-text persona or style for the roast.
- `target`: Optional Discord user. When provided, only that user's messages are sent.

The command responses ephemerally first, then the backend later publishes the payload to a
Redis Pub/Sub channel where the bot's background subscriber picks it up asynchronously to post the final roast embed
into the Discord channel.

### `/fact`

Stores a fact about a Discord user by sending it to Dobby Core Backend.

Options:

- `target`: Required Discord user the fact is about.
- `fact`: Required fact text to remember.

The bot rejects bot users for both `/roast target` and `/fact target`.

## Architecture

### Main Flow

1. `Main.kt` loads configuration from `.env`,
   creates the Discord `Kord` client and initializes the Redis connection.
2. Koin wires dependencies from `di/AppModule.kt`.
3. `DiscordBot` registers application commands and logs in to Discord.
4. Command handlers call `DobbyCoreBackendService` to trigger requests.
5. `RedisSubscriberManager` listens for incoming results from the backend and passes them to `RoastDeliveryService` to
   post the final roast embed into the Discord channel.

### Important Source Files

* **`src/main/kotlin/Main.kt`**: Application entry point.
* **`src/main/kotlin/DiscordBot.kt`**: Discord command registration and event routing.
* **`src/main/kotlin/command/`**: Slash command implementations.
* **`src/main/kotlin/service/DobbyCoreBackendService.kt`**: Outbound calls to the Dobby Core Backend.
* **`src/main/kotlin/queue/RedisSubscriberManager.kt`**: Long-running background worker handling the Redis Pub/Sub
  subscription and connection resilience.
* **`src/main/kotlin/service/RoastDeliveryService.kt`**: Formats and posts backend roast results to Discord channels.
* **`src/main/kotlin/config/BotConfig.kt`**: Environment configuration.
* **`src/main/kotlin/util/MessagesHandler.kt`**: Discord message fetching and formatting.

## Requirements

- JDK 24 available on `PATH` or through `JAVA_HOME`.
- Discord bot token.
- A Discord application with the bot installed in the server where you want to use it.
- Dobby Core Backend running and reachable from this bot service.
- Redis instance running and reachable from this bot service.
- Gradle wrapper from this repository.

The Gradle build uses:

- Kotlin JVM `2.2.21`
- Gradle `8.14.4`
- Kord `0.17.0`
- Koin
- kotlinx serialization
- dotenv-kotlin
- Redis client

## Environment Variables

Create a `.env` file in the repository root:

```env
DISCORD_TOKEN=your-discord-bot-token
DEV_GUILD_ID=your-discord-server-id
BACKEND_URL=http://localhost:8000/api/v1
BACKEND_API_HEADER=X-API-Key
BACKEND_API_KEY=your-backend-api-key
REDIS_HOST=localhost
REDIS_PORT=6379
```

Variables:

| Name                 | Required | Default     | Description                                                                                                                                                               |
|----------------------|----------|-------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `DISCORD_TOKEN`      | Yes      | None        | Discord bot token used by Kord to log in.                                                                                                                                 |
| `DEV_GUILD_ID`       | No       | None        | Discord guild/server ID for instant command registration during development. If omitted, commands are registered globally and can take up to about one hour to propagate. |
| `BACKEND_URL`        | Yes      | None        | Base URL of Dobby Core Backend. This service calls `POST {DOBBY_BACKEND_URL}/roast` and `POST {DOBBY_BACKEND_URL}/fact`.                                                  |
| `BACKEND_API_HEADER` | Yes      | `X-API-Key` | API key header for the backend.                                                                                                                                           |
| `BACKEND_API_KEY`    | Yes      | None        | API key for the backend.                                                                                                                                                  |
| `REDIS_HOST`         | Yes      | `localhost` | Hostname or IP address of the Redis server.                                                                                                                               |
| `REDIS_PORT`         | Yes      | `6379`      | Port number of the Redis server.                                                                                                                                          |

## Running Locally

1. Install or select JDK 24.

   ```bash
   java -version
   ```

   If Java is not found, set `JAVA_HOME` to your JDK 24 installation and ensure `$JAVA_HOME/bin` is on `PATH`.

2. Start Redis and ensure it is running on the configured `REDIS_HOST:REDIS_PORT`.

3. Create `.env` in the project root using the variables above.

4. Start Dobby Core Backend and make sure `DOBBY_BACKEND_URL` points to it.

5. Run the bot service:

   ```bash
   ./gradlew run
   ```

6. Watch the logs for command registration, Redis connection, and bot startup messages.

For development, set `DEV_GUILD_ID` so command changes appear immediately in one Discord server. Without it, the bot
registers global commands, which can take time to propagate.

## Build and Test

Build:

```bash
./gradlew build
```

Run tests:

```bash
./gradlew test
```

There are currently no test source files in this repository.

## Outbound Backend API

This service expects Dobby Core Backend to provide these endpoints:

### `POST /roast`

Called when a user runs `/roast`.

Request body:

```json
{
  "channelId": "123456789012345678",
  "guildId": "987654321098765432",
  "persona": "Arch Linux user",
  "messages": [
    {
      "author": "111111111111111111",
      "content": "You use a GUI for that?",
      "timestamp": "2026-05-25T16:30:00Z"
    }
  ]
}
```

### `POST /fact`

Called when a user runs `/fact`.

Request body:

```json
{
  "fact": "He'll fix a kernel panic before fixing his posture.",
  "discordUserId": "111111111111111111",
  "guildId": "987654321098765432",
  "displayName": "SomeUser"
}
```

For both outbound calls, any successful HTTP status is treated as accepted. Failures are logged and reported back to the
Discord user with a generic server-down message.

## Discord Permissions and Setup Notes

The bot needs to be installed in the Discord server where commands are used. At minimum, it must be able to:

- Register and receive application command interactions.
- Read channel message history for `/roast`.
- Send messages in channels where roast results are posted.

## 🐛 Troubleshooting

| Problem                                                     | Solution                                                                                                                                                 |
|:------------------------------------------------------------|:---------------------------------------------------------------------------------------------------------------------------------------------------------|
| `DISCORD_TOKEN not set`                                     | Ensure your `.env` file exists in the project root and contains a valid `DISCORD_TOKEN`.                                                                 |
| Commands do not appear in Discord                           | Set `DEV_GUILD_ID` in your `.env` for instant synchronization during development. Global Discord command propagation can take up to an hour.             |
| `JAVA_HOME is not set and no 'java' command could be found` | Install JDK 24 and configure your system's `JAVA_HOME` environment variable, or execute the application through an IDE configured with JDK 24.           |
| `/roast` returns a generic server-down message              | Verify that the Dobby Core Backend application is running, the `DOBBY_BACKEND_URL` is correct, and that the network pathways are clear.                  |
| Redis connection fails or drops                             | Ensure your Redis container or service is actively running. Check that your `.env` file has the correct `REDIS_HOST` and `REDIS_PORT` values configured. |
| Missing environment variables                               | Run `cp .env.example .env` (or create a `.env` file manually) and ensure all required connection strings and secrets are populated.                      |
