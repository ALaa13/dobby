package org.example.util

object DiscordStrings {
    object Commands {
        object Ping {
            const val NAME = "ping"
            const val DESCRIPTION = "Check bot latency"
            const val DEFERRED_MESSAGE = "Calculating ping..."
        }

        object Roast {
            const val NAME = "roast"
            const val DESCRIPTION = "Roast the living soul outta the Homies in this channel"
            const val DEFERRED_MESSAGE = "Aight fam, lemme cook"
            const val SUCCESS_REPLIED_MESSAGE_TITLE = "🔥 The Roast Master Has Spoken 🔥"
            const val FAILURE_REPLIED_MESSAGE_TITLE = "❌ Roast Failed ❌"
            const val IS_BOT_REPLY = "Yo fam I ain't roasting one of my own. Pick a human"
            const val DISCORD_INTERACTION_FAILED =
                "Ugh fam, I couldn't deliver the roast to the channel... Try again later"


            object Count {
                const val NAME = "count"
                const val DESCRIPTION = "How many messages to read for the roast (Default 50)"
                const val CHOICE_10 = "Last 10 messages"
                const val CHOICE_50 = "Last 50 messages"
                const val CHOICE_250 = "Last 250 messages"
                const val CHOICE_500 = "Last 500 messages"
            }

            object Persona {
                const val NAME = "persona"
                const val DESCRIPTION =
                    "Type a persona for the roast e.g. league of legends (Default: No persona, just a straight up roast)"
            }

            object Target {
                const val NAME = "target"
                const val DESCRIPTION = "Roast a specific homie instead of the whole channel"
            }
        }
    }

    object HttpEndPoints {
        object PostRoast {
            const val PATH = "/roasts"
            const val FAILED_MESSAGE = "Ugh fam, my brain aka the server is down... Try again later"
        }

        object InternalBotDelivery {
            const val PATH = "/api/internal/deliver"
            const val HEADERS = "X-Internal-Token"
        }
    }
}
