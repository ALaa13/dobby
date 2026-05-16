package org.example.utils

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
            const val REPLIED_MESSAGE_TITLE = "🔥 The Roast Master Has Spoken 🔥"

            object Count {
                const val NAME = "count"
                const val DESCRIPTION = "How many messages to read for the roast"
                const val CHOICE_10 = "Last 10 messages"
                const val CHOICE_50 = "Last 50 messages"
                const val CHOICE_250 = "Last 250 messages"
                const val CHOICE_500 = "Last 500 messages"
            }

            object Since {
                const val NAME = "since"
                const val DESCRIPTION = "How far back to read messages for the roast"
                const val CHOICE_30 = "Last 30 minutes"
                const val CHOICE_60 = "Last 1 hour"
                const val CHOICE_180 = "Last 3 hours"
                const val CHOICE_360 = "Last 6 hours"
                const val CHOICE_720 = "Last 12 hours"
                const val CHOICE_1440 = "Last 24 hours"
            }
        }
    }

    object HttpEndPoints {
        object PostRoast {
            const val PATH = "/roast"
        }

        object InternalBotDelivery {
            const val PATH = "/api/internal/deliver"
            const val HEADERS = "X-Internal-Token"
        }
    }
}
