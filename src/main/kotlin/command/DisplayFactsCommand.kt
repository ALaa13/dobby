package org.example.command

import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.interaction.MultiApplicationCommandBuilder
import dev.kord.rest.builder.interaction.input
import dev.kord.rest.builder.interaction.user
import dev.kord.rest.builder.message.embed
import org.example.dto.Fact
import org.example.service.DobbyCoreBackend
import org.example.util.DiscordStrings

class DisplayFactsCommand(
    private val dobbyCoreBackend: DobbyCoreBackend
) : ChatInputCommand() {
    override val name = DiscordStrings.Commands.GetFacts.NAME
    override val description = DiscordStrings.Commands.GetFacts.DESCRIPTION

    override suspend fun register(builder: MultiApplicationCommandBuilder) {
        builder.input(name, description) {
            user(
                DiscordStrings.Commands.GetFacts.Target.NAME,
                DiscordStrings.Commands.GetFacts.Target.DESCRIPTION
            ) {
                required = true
            }
        }
    }

    override suspend fun run(event: GuildChatInputCommandInteractionCreateEvent) {
        val interaction = event.interaction
        val target = interaction.command.users[DiscordStrings.Commands.GetFacts.Target.NAME]!!

        respondEphemeral(
            event = event,
            botGuardTarget = target,
            errorLogMessage = "Catastrophic failure during fact lookup"
        ) {
            val facts = getFacts(
                discordUserId = target.id.toString(),
                guildId = interaction.guildId.toString()
            )
            if (facts.isEmpty()) {
                DiscordStrings.Commands.GetFacts.NO_FACTS_FOUND
            } else {
                respondWithFacts(event, facts)
                DiscordStrings.Commands.GetFacts.SUCCESS_REPLY
            }
        }
    }

    private suspend fun getFacts(
        discordUserId: String,
        guildId: String,
    ): List<Fact> {
        val facts = dobbyCoreBackend.getFactsForUser(discordUserId, guildId)
        if (facts.isEmpty()) {
            return emptyList()
        }
        return facts
    }

    suspend fun respondWithFacts(event: GuildChatInputCommandInteractionCreateEvent, facts: List<Fact>) {
        val target = event.interaction.command.users[DiscordStrings.Commands.GetFacts.Target.NAME]!!
        event.interaction.channel.createMessage {
            embed {
                title = DiscordStrings.Commands.GetFacts.SUCCESS_EMBED_TITLE

                description =
                    buildString {
                        append("Facts about <@${target.id}>\n\n")

                        facts.forEachIndexed { index, fact ->
                            append("`#${index + 1}`  ${fact.factText}\n")
                        }
                    }
            }
        }
    }
}

