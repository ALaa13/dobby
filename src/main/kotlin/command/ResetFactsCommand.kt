package org.example.command

import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import org.example.service.DobbyCoreBackend
import org.example.util.DiscordStrings

class ResetFactsCommand(
    private val dobbyCoreBackend: DobbyCoreBackend
) : ChatInputCommand() {
    override val name = DiscordStrings.Commands.ResetFacts.NAME
    override val description = DiscordStrings.Commands.ResetFacts.DESCRIPTION

    override suspend fun run(event: GuildChatInputCommandInteractionCreateEvent) {
        val interaction = event.interaction
        val target = interaction.user
        respondEphemeral(
            event,
            target,
            "Catastrophic failure during facts reset"
        ) {
            content = when (dobbyCoreBackend.deleteFactsForUser(
                target.id.toString(),
                interaction.guildId.toString()
            )) {
                true -> DiscordStrings.Commands.ResetFacts.SUCCESS_REPLY
                false -> DiscordStrings.Commands.ResetFacts.FAILURE_REPLY
            }
        }
    }
}