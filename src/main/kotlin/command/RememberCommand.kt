package org.example.command

import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.interaction.MultiApplicationCommandBuilder
import dev.kord.rest.builder.interaction.input
import dev.kord.rest.builder.interaction.string
import dev.kord.rest.builder.interaction.user
import org.example.dto.RememberFactRequest
import org.example.service.DobbyCoreBackend
import org.example.util.DiscordStrings
import org.example.util.Logging

class RememberCommand(
    private val dobbyCoreBackend: DobbyCoreBackend
) : ChatInputCommand() {

    override val name = DiscordStrings.Commands.Remember.NAME
    override val description = DiscordStrings.Commands.Remember.DESCRIPTION

    override suspend fun register(builder: MultiApplicationCommandBuilder) {
        builder.input(name, description) {
            user(
                DiscordStrings.Commands.Remember.Target.NAME,
                DiscordStrings.Commands.Remember.Target.DESCRIPTION
            ) {
                required = true
            }
            string(
                DiscordStrings.Commands.Remember.Fact.NAME,
                DiscordStrings.Commands.Remember.Fact.DESCRIPTION
            ) {
                required = true
            }
        }
    }

    override suspend fun run(event: GuildChatInputCommandInteractionCreateEvent) {
        val interaction = event.interaction
        val deferredMessage = interaction.deferEphemeralResponse()

        // Get command options
        val target = interaction.command.users[DiscordStrings.Commands.Remember.Target.NAME]!!
        val fact = interaction.command.strings[DiscordStrings.Commands.Remember.Fact.NAME]!!

        // The selected target is Bot and not a user
        if (target.isBot) {
            deferredMessage.respond { content = DiscordStrings.Commands.IS_BOT_REPLY }
            return
        }

        runCatching {
            deliverFact(
                fact = fact,
                discordUserid = target.id.toString(),
                displayName = target.username,
                guildId = interaction.guildId.toString(),

                )
        }.onSuccess { response ->
            deferredMessage.respond { content = response }
        }.onFailure { error ->
            Logging.logError("Catastrophic failure during roast generation", error)
            deferredMessage.respond { content = DiscordStrings.Commands.DISCORD_INTERACTION_FAILED }
        }
    }

    private suspend fun deliverFact(
        fact: String,
        discordUserid: String,
        displayName: String,
        guildId: String
    ): String {
        val requestBody = RememberFactRequest(
            fact = fact,
            discordUserId = discordUserid,
            guildId = guildId,
            displayName = displayName
        )
        return if (dobbyCoreBackend.sendFactRequest(rememberFactRequest = requestBody)) {
            DiscordStrings.Commands.Remember.SUCCESS_REPLY
        } else {
            DiscordStrings.HttpEndPoints.FAILED_MESSAGE
        }
    }
}