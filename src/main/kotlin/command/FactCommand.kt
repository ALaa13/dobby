package org.example.command

import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.interaction.MultiApplicationCommandBuilder
import dev.kord.rest.builder.interaction.input
import dev.kord.rest.builder.interaction.string
import dev.kord.rest.builder.interaction.user
import dev.kord.rest.builder.message.modify.InteractionResponseModifyBuilder
import org.example.dto.FactRequest
import org.example.service.DobbyCoreBackend
import org.example.util.DiscordStrings

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
        val target = interaction.command.users[DiscordStrings.Commands.Fact.Target.NAME]!!
        val fact = interaction.command.strings[DiscordStrings.Commands.Fact.Fact.NAME]!!

        respondEphemeral(
            event,
            target,
            "Catastrophic failure during fact delivery"
        ) {
            sendFact(
                fact,
                target.id.toString(),
                target.username,
                interaction.guildId.toString(),
                this
            )
        }
    }

    private suspend fun sendFact(
        fact: String,
        discordUserid: String,
        displayName: String,
        guildId: String,
        builder: InteractionResponseModifyBuilder
    ) {
        val requestBody = FactRequest(
            fact,
            discordUserid,
            guildId,
            displayName
        )
        builder.content = when {
            dobbyCoreBackend.sendFactRequest(requestBody) -> DiscordStrings.Commands.Fact.SUCCESS_REPLY
            else -> DiscordStrings.HttpEndPoints.FAILED_MESSAGE
        }
    }
}
