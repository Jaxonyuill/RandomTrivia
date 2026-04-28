package Sh1nci.RandomTrivia.trivia

import Sh1nci.RandomTrivia.RandomTrivia
import java.math.BigDecimal
import java.util.concurrent.atomic.AtomicBoolean
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitTask

/**
 * Manages the lifecycle of a single active trivia question. Thread-safe to handle asynchronous chat
 * events.
 */
class TriviaGame(
    private val plugin: JavaPlugin,
    private val triviaManager: TriviaManager,
    private val question: TriviaQuestion,
    private val timeLimitSeconds: Long,
    private val rewardAmount: Double,
    private val onFinish: (winner: Player?) -> Unit
) {

    private var hintTask: BukkitTask? = null
    private var timerTask: BukkitTask? = null
    private val active = AtomicBoolean(false)

    val isActive: Boolean
        get() = active.get()

    // ── Public API ────────────────────────────────────────────────────────────

    /** Broadcasts the question and starts listening. */
    fun start() {
        if (active.compareAndSet(false, true)) {
            broadcastQuestion()
            scheduleHint()
            scheduleTimeout()
        }
    }

    /**
     * Called by the chat listener on every message (usually async). Returns true if the message was
     * a correct answer and the round ended.
     */
    fun handleAnswer(player: Player, message: String): Boolean {
        if (!active.get()) return false
        val normalised = message.trim().lowercase()
        val correct = question.answers.any { it.trim().lowercase() == normalised }

        if (correct && active.compareAndSet(true, false)) {
            finish(player)
            return true
        }
        return false
    }

    /** Forcibly ends the game with no winner (skip / timeout). */
    fun cancel(silent: Boolean = false) {
        if (active.compareAndSet(true, false)) {
            finish(winner = null, silent = silent)
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private fun broadcastQuestion() {
        triviaManager.broadcast(
            Component.text("★ RANDOM TRIVIA ★", NamedTextColor.BLUE, TextDecoration.BOLD)
        )
        triviaManager.broadcast(Component.text(question.question, NamedTextColor.WHITE))
        
        if (timeLimitSeconds > 0) {
            triviaManager.broadcast(
                Component.text("First to answer in chat wins! You have ", NamedTextColor.GRAY)
                    .append(Component.text("${timeLimitSeconds}s", NamedTextColor.AQUA))
                    .append(Component.text(".", NamedTextColor.GRAY))
            )
        } else {
            triviaManager.broadcast(
                Component.text("First to answer in chat wins!", NamedTextColor.GRAY)
            )
        }
    }

    private fun scheduleHint() {
        if (timeLimitSeconds <= 0) return
        val hint = question.hint ?: return
        // Show hint halfway through the time limit
        val hintDelay = (timeLimitSeconds / 2) * 20L
        hintTask = plugin.server.scheduler.runTaskLater(
            plugin,
            Runnable {
                if (!active.get()) return@Runnable
                triviaManager.broadcast(
                    Component.text("💡 Hint: ", NamedTextColor.AQUA, TextDecoration.BOLD)
                        .append(Component.text(hint, NamedTextColor.WHITE))
                )
            },
            hintDelay
        )
    }

    private fun scheduleTimeout() {
        if (timeLimitSeconds <= 0) return
        timerTask = plugin.server.scheduler.runTaskLater(
            plugin,
            Runnable {
                if (active.compareAndSet(true, false)) {
                    plugin.server.scheduler.runTask(
                        plugin,
                        Runnable {
                            hintTask?.cancel()
                            triviaManager.broadcast(
                                Component.text("⌛ Time's up! ", NamedTextColor.RED, TextDecoration.BOLD)
                                    .append(Component.text("Nobody answered correctly.", NamedTextColor.GRAY))
                            )
                            triviaManager.broadcast(
                                Component.text("✔ The answer was: ", NamedTextColor.GREEN)
                                    .append(Component.text(question.answers.first(), NamedTextColor.WHITE, TextDecoration.BOLD))
                            )
                            onFinish(null)
                        }
                    )
                }
            },
            timeLimitSeconds * 20L
        )
    }

    private fun finish(winner: Player?, silent: Boolean = false) {
        // Run cleanup and announcements on the main thread to be safe with Bukkit API
        plugin.server.scheduler.runTask(
            plugin,
            Runnable {
                hintTask?.cancel()
                timerTask?.cancel()

                if (!silent) {
                    if (winner != null) {
                        val econ = RandomTrivia.econ
                        if (econ != null && rewardAmount > 0) {
                            econ.deposit(
                                plugin.name,
                                winner.uniqueId,
                                BigDecimal.valueOf(rewardAmount)
                            )
                            triviaManager.broadcast(
                                Component.text("🏆 ", NamedTextColor.BLUE)
                                    .append(Component.text(winner.name, NamedTextColor.AQUA, TextDecoration.BOLD))
                                    .append(Component.text(" answered correctly and won ", NamedTextColor.GREEN))
                                    .append(Component.text(econ.format(BigDecimal.valueOf(rewardAmount)), NamedTextColor.GOLD, TextDecoration.BOLD))
                                    .append(Component.text("!", NamedTextColor.GREEN))
                            )
                        } else {
                            triviaManager.broadcast(
                                Component.text("🏆 ", NamedTextColor.BLUE)
                                    .append(Component.text(winner.name, NamedTextColor.AQUA, TextDecoration.BOLD))
                                    .append(Component.text(" answered correctly! ", NamedTextColor.GREEN))
                            )
                        }

                        triviaManager.broadcast(
                            Component.text("✔ The answer was: ", NamedTextColor.GREEN)
                                .append(Component.text(question.answers.first(), NamedTextColor.WHITE, TextDecoration.BOLD))
                        )
                    } else {
                        triviaManager.broadcast(
                            Component.text("⏭ Question skipped. ", NamedTextColor.AQUA)
                                .append(Component.text("The answer was: ", NamedTextColor.GRAY))
                                .append(Component.text(question.answers.first(), NamedTextColor.WHITE, TextDecoration.BOLD))
                        )
                    }
                }
                onFinish(winner)
            }
        )
    }
}