# Dobby Discord Bot

Dobby is a Kotlin Discord bot service. It registers Discord application commands, forwards roast and fact requests to a
separate Dobby Core Backend, and exposes an internal HTTP endpoint so the backend can deliver processed roast results
back into Discord. Redis is used for caching and state management.

## What the Service Does

The service has three runtime parts:

1. Discord bot client
    - Logs in to Discord with `DISCORD_TOKEN`.
    - Registers slash commands either globally or to a development guild.
    - Handles user interactions from Discord.

2. Embedded Ktor HTTP server
    - Starts inside the same process as the bot.
    - Listens on `EMBEDDED_SERVER_HOST:EMBEDDED_SERVER_PORT`.
    - Exposes `POST /api/internal/deliver` for internal backend callbacks.
    - Requires the `X-Internal-Token` header to match `INTERNAL_SECURITY_TOKEN`.

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

The command responses ephemerally first, then the backend later calls this service at `/api/internal/deliver`; the bot
posts the final roast embed into the Discord channel.

### `/fact`

Stores a fact about a Discord user by sending it to Dobby Core Backend.

Options:

- `target`: Required Discord user the fact is about.
- `fact`: Required fact text to remember.

The bot rejects bot users for both `/roast target` and `/fact target`.

## Architecture

Main flow:

1. `Main.kt` loads configuration from `.env`.
2. `Main.kt` creates the Discord `Kord` client, shared Ktor HTTP client, and Redis connection.
3. Koin wires dependencies from `di/AppModule.kt`.
4. `EmbeddedServerManager` starts the internal Ktor server.
5. `DiscordBot` registers application commands and logs in to Discord.
6. Command handlers call `DobbyCoreBackend` and use Redis for caching.
7. Dobby Core Backend calls back to `POST /api/internal/deliver`.
8. `RoastDeliveryService` posts the result to the Discord channel.

Important source files:

- `src/main/kotlin/Main.kt`: application entry point.
- `src/main/kotlin/DiscordBot.kt`: Discord command registration and event routing.
- `src/main/kotlin/command/`: command implementations.
- `src/main/kotlin/service/DobbyCoreBackendService.kt`: outbound HTTP calls to Dobby Core Backend.
- `src/main/kotlin/service/RoastDeliveryService.kt`: posts backend roast results to Discord.
- `src/main/kotlin/route/RoastDeliveryRoute.kt`: internal callback endpoint.
- `src/main/kotlin/config/BotConfig.kt`: environment configuration.
- `src/main/kotlin/util/MessagesHandler.kt`: Discord message fetching and formatting.

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
- Ktor client/server
- Koin
- kotlinx serialization
- dotenv-kotlin
- Redis client

## Environment Variables

Create a `.env` file in the repository root:

```env
DISCORD_TOKEN=your-discord-bot-token
DEV_GUILD_ID=your-discord-server-id
INTERNAL_SECURITY_TOKEN=shared-secret-used-by-core-backend
DOBBY_BACKEND_URL=http://localhost:8000
EMBEDDED_SERVER_PORT=8080
EMBEDDED_SERVER_HOST=0.0.0.0
REDIS_HOST=localhost
REDIS_PORT=6379
```

Variables:

| Name                      | Required | Default       | Description                                                                                                                                   |
|---------------------------|----------|---------------|-----------------------------------------------------------------------------------------------------------------------------------------------|
| `DISCORD_TOKEN`           | Yes      | None          | Discord bot token used by Kord to log in.                                                                                                     |
| `DEV_GUILD_ID`            | No       | None          | Discord guild/server ID for instant command registration during development. If omitted, commands are registered globally and can take up to about one hour to propagate. |
| `INTERNAL_SECURITY_TOKEN` | Yes      | None          | Shared secret required on internal callback requests. The backend must send it as `X-Internal-Token`.                                         |
| `DOBBY_BACKEND_URL`       | Yes      | None          | Base URL of Dobby Core Backend. This service calls `POST {DOBBY_BACKEND_URL}/roast` and `POST {DOBBY_BACKEND_URL}/fact`.                      |
| `EMBEDDED_SERVER_PORT`    | No       | `8080`        | Port for this service's embedded Ktor server.                                                                                                 |
| `EMBEDDED_SERVER_HOST`    | No       | `0.0.0.0`     | Host/interface for the embedded Ktor server.                                                                                                  |
| `REDIS_HOST`              | No       | `localhost`   | Hostname or IP address of the Redis server.                                                                                                   |
| `REDIS_PORT`              | No       | `6379`        | Port number of the Redis server.                                                                                                             |

Do not commit `.env`; it is already ignored by `.gitignore`.

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

## Internal HTTP API

### `POST /api/internal/deliver`

Used by Dobby Core Backend to deliver a completed roast result.

Required header:

```http
X-Internal-Token: <INTERNAL_SECURITY_TOKEN>
```

Request body:

```json
{
  "channelId": "123456789012345678",
  "content": "Processed roast text",
  "success": true
}
```

Behavior:

- If the token is invalid, the service returns `401 Unauthorized`.
- If `success` is `true`, the bot posts an embed titled `The Roast Master Has Spoken`.
- If `success` is `false`, the bot posts a failure embed.
- If Discord rejects the post, the service logs the Discord API error.

## Outbound Backend API

This service expects Dobby Core Backend to provide these endpoints:

### `POST /roast`

Called when a user runs `/roast`.

Request body:

```json
{
  "channelId": "123456789012345678",
  "guildId": "123456789012345678",
  "messages": [
    {
      "author": "123456789012345678",
      "content": "message text",
      "timestamp": "2026-01-01T12:00:00Z"
    }
  ],
  "persona": "league of legends"
}
```

### `POST /fact`

Called when a user runs `/fact`.

Request body:

```json
{
  "fact": "fact text",
  "discordUserId": "123456789012345678",
  "guildId": "123456789012345678",
  "displayName": "username"
}
```

For both outbound calls, any successful HTTP status is treated as accepted. Failures are logged and reported back to the
Discord user with a generic server-down message.

## Discord Permissions and Setup Notes

The bot needs to be installed in the Discord server where commands are used. At minimum, it must be able to:

- Register and receive application command interactions.
- Read channel message history for `/roast`.
- Send messages in channels where roast results are posted.

If the internal callback succeeds but no message appears in Discord, check:

- The bot has permission to send messages in the target channel.
- The `channelId` sent by Dobby Core Backend is correct.
- The backend is using the same `INTERNAL_SECURITY_TOKEN`.
- The embedded server host and port are reachable by Dobby Core Backend.

## Troubleshooting

### `DISCORD_TOKEN not set`

The `.env` file is missing or does not contain `DISCORD_TOKEN`.

### Commands do not appear in Discord

Use `DEV_GUILD_ID` during development. Global Discord command registration can take up to about one hour to propagate.

### `JAVA_HOME is not set and no 'java' command could be found`

Install JDK 24 and set `JAVA_HOME`, or run the service from an IDE configured with JDK 24.

### `/roast` returns the generic server-down message

Check that Dobby Core Backend is running, `DOBBY_BACKEND_URL` is correct, and the backend accepts `POST /roast`.

### Backend callback returns `401 Unauthorized`

The `X-Internal-Token` request header does not match `INTERNAL_SECURITY_TOKEN`.

### Redis connection fails

Check that Redis is running on `REDIS_HOST:REDIS_PORT` and that both variables are correctly set in your `.env` file.
