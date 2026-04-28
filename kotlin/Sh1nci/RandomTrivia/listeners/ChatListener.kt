package Sh1nci.RandomTrivia.listeners

import Sh1nci.RandomTrivia.trivia.TriviaManager
import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

/**
 * Listens to chat messages and forwards them to the [TriviaManager].
 *
 * Priority is set to LOWEST so we run before other chat plugins.
 * If the message is a correct answer we cancel it so it doesn't pollute chat
 * (optional — remove the `event.isCancelled = true` line if you prefer to keep it visible).
 */
class ChatListener(private val triviaManager: TriviaManager) : Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    fun onChat(event: AsyncChatEvent) {
        val player = event.player
        val rawMessage = PlainTextComponentSerializer.plainText().serialize(event.message())

        val wasAnswer = triviaManager.handleChatMessage(player, rawMessage)
        if (wasAnswer) {
            // Suppress the raw answer from appearing in chat so it doesn't spoil
            // the question for anyone else typing. Remove this line to keep it visible.
            event.isCancelled = true
        }
    }
}