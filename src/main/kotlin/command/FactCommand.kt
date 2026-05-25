package org.example.command

import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.interaction.MultiApplicationCommandBuilder
import dev.kord.rest.builder.interaction.input
import dev.kord.rest.builder.interaction.string
import dev.kord.rest.builder.interaction.user
import org.example.dto.FactRequest
import org.example.service.DobbyCoreBackend
import org.example.util.DiscordStrings
import org.example.util.Logging

class FactCommand(
    private val dobbyCoreBackend: DobbyCoreBackend
) : ChatInputCommand() {

    override val name = DiscordStrings.Commands.Fact.NAME
    override val description = DiscordStrings.Commands.Fact.DESCRIPTION

    override suspend fun register(builder: MultiApplicationCommandBuilder) {
        builder.input(name, description) {
            user(
                DiscordStrings.Commands.Fact.Target.NAME,
                DiscordStrings.Commands.Fact.Target.DESCRIPTION
            ) {
                required = true
            }
            string(
                DiscordStrings.Commands.Fact.Fact.NAME,
                DiscordStrings.Commands.Fact.Fact.DESCRIPTION
            ) {
                required = true
            }
        }
    }

    override suspend fun run(event: GuildChatInputCommandInteractionCreateEvent) {
        val interaction = event.interaction
        val deferredMessage = interaction.deferEphemeralResponse()

        // Get command options
        val target = interaction.command.users[DiscordStrings.Commands.Fact.Target.NAME]!!
        val fact = interaction.command.strings[DiscordStrings.Commands.Fact.Fact.NAME]!!

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
        val requestBody = FactRequest(
            fact = fact,
            discordUserId = discordUserid,
            guildId = guildId,
            displayName = displayName
        )
        return if (dobbyCoreBackend.sendFactRequest(factRequest = requestBody)) {
            DiscordStrings.Commands.Fact.SUCCESS_REPLY
        } else {
            DiscordStrings.HttpEndPoints.FAILED_MESSAGE
        }
    }
}