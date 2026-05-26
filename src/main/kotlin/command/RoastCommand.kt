package org.example.command

import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.interaction.*
import org.example.dto.ChatMessage
import org.example.dto.RoastDeliveryRequest
import org.example.service.DobbyCoreBackend
import org.example.util.DiscordStrings
import org.example.util.FetchMessagesConfig
import org.example.util.fetchMessages
import org.example.util.formatMessagesForAI

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
                choice(DiscordStrings.Commands.Roast.Count.CHOICE_100, 100)
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
        val channel = interaction.channel

        val messagesCount = interaction.command.integers[DiscordStrings.Commands.Roast.Count.NAME]?.toInt()
        val target = interaction.command.users[DiscordStrings.Commands.Roast.Target.NAME]
        val persona = interaction.command.strings[DiscordStrings.Commands.Roast.Persona.NAME]

        respondEphemeral(
            event = event,
            botGuardTarget = target,
            errorLogMessage = "Catastrophic failure during roast generation"
        ) {
            val config = FetchMessagesConfig(
                messagesToFetch = messagesCount ?: 50,
                authorId = target?.id
            )

            val messages = fetchMessages(channel, interactionId = interaction.id, config = config)
            val formattedMessages = formatMessagesForAI(messages)

            deliverRoast(
                channelId = channel.id.toString(),
                guildId = interaction.guildId.toString(),
                messages = formattedMessages,
                persona = persona
            )
        }
    }

    private suspend fun deliverRoast(
        channelId: String,
        guildId: String,
        messages: List<ChatMessage>,
        persona: String?
    ): String {
        val requestBody = RoastDeliveryRequest(
            channelId = channelId,
            guildId = guildId,
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
