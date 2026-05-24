package org.example.command

import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.interaction.*
import org.example.dto.ChatMessage
import org.example.dto.RoastDeliveryRequest
import org.example.service.DobbyCoreBackend
import org.example.util.*

class RoastCommand(
    private val dobbyCoreBackend: DobbyCoreBackend
) : ChatInputCommand() {

    override val name = DiscordStrings.Commands.Roast.NAME
    override val description = DiscordStrings.Commands.Roast.DESCRIPTION

    override suspend fun register(builder: MultiApplicationCommandBuilder) {
        builder.input(name, description) {
            integer(
                DiscordStrings.Commands.Roast.Count.NAME,
                DiscordStrings.Commands.Roast.Count.DESCRIPTION
            ) {
                required = false
                choice(DiscordStrings.Commands.Roast.Count.CHOICE_10, 10)
                choice(DiscordStrings.Commands.Roast.Count.CHOICE_50, 50)
                choice(DiscordStrings.Commands.Roast.Count.CHOICE_250, 250)
                choice(DiscordStrings.Commands.Roast.Count.CHOICE_500, 500)
            }
            string(
                DiscordStrings.Commands.Roast.Persona.NAME,
                DiscordStrings.Commands.Roast.Persona.DESCRIPTION
            ) {
                required = false
            }
            user(
                DiscordStrings.Commands.Roast.Target.NAME,
                DiscordStrings.Commands.Roast.Target.DESCRIPTION
            ) {
                required = false
            }
        }
    }

    override suspend fun run(event: GuildChatInputCommandInteractionCreateEvent) {
        val interaction = event.interaction
        val deferredMessage = interaction.deferEphemeralResponse()
        val channel = interaction.channel


        // Get command options
        val messagesCount = interaction.command.integers[DiscordStrings.Commands.Roast.Count.NAME]?.toInt()
        val target = interaction.command.users[DiscordStrings.Commands.Roast.Target.NAME]
        val persona = interaction.command.strings[DiscordStrings.Commands.Roast.Persona.NAME]

        // The selected target is Bot and not a user
        if (target?.isBot == true) {
            deferredMessage.respond { content = DiscordStrings.Commands.Roast.IS_BOT_REPLY }
            return
        }

        runCatching {
            val config = FetchMessagesConfig(
                messagesToFetch = messagesCount,
                authorId = target?.id
            )

            val messages = fetchMessages(channel, interactionId = interaction.id, config = config)
            val formattedMessages = formatMessagesForAI(messages)

            deliverRoast(
                channelId = channel.id.toString(),
                messages = formattedMessages,
                persona = persona
            )
        }.onSuccess { response ->
            deferredMessage.respond { content = response }
        }.onFailure { error ->
            Logging.logError("Catastrophic failure during roast generation", error)
            deferredMessage.respond { content = DiscordStrings.Commands.DISCORD_INTERACTION_FAILED }
        }
    }

    private suspend fun deliverRoast(
        channelId: String,
        messages: List<ChatMessage>,
        persona: String?
    ): String {
        val requestBody = RoastDeliveryRequest(
            channelId = channelId,
            messages = messages,
            persona = persona
        )

        return if (dobbyCoreBackend.sendDiscordMessages(requestBody = requestBody)) {
            DiscordStrings.Commands.Roast.DEFERRED_MESSAGE
        } else {
            DiscordStrings.HttpEndPoints.FAILED_MESSAGE
        }
    }
}