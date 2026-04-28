package Sh1nci.RandomTrivia.trivia

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitTask
import java.util.UUID
import java.util.concurrent.atomic.AtomicReference

class TriviaManager(private val plugin: JavaPlugin) {

    private var questionPool = emptyList<TriviaQuestion>()
    private var timeLimitSeconds: Long = 30L
    private var rewardAmount: Double = 0.0
    private var autoDropTask: BukkitTask? = null
    
    private val mutedPlayers = mutableSetOf<UUID>()
    private val _currentGame = AtomicReference<TriviaGame?>(null)

    val currentGame: TriviaGame?
        get() = _currentGame.get()

    /**
     * Loads the questions and interval from config.yml asynchronously.
     * Restarts the auto-drop timer on the main thread once loaded.
     */
    fun reloadConfig() {
        plugin.server.scheduler.runTaskAsynchronously(plugin, Runnable {
            // Read disk on async thread
            plugin.reloadConfig()
            val data = TriviaConfigLoader.load(plugin)
            
            // Switch back to main thread to update state and start timer
            plugin.server.scheduler.runTask(plugin, Runnable {
                questionPool = data.questions
                timeLimitSeconds = data.timeLimitSeconds
                rewardAmount = data.rewardAmount
                
                // Cancel existing task
                autoDropTask?.cancel()
                autoDropTask = null

                if (data.intervalSeconds > 0 && questionPool.isNotEmpty()) {
                    val ticks = data.intervalSeconds * 20L
                    autoDropTask = plugin.server.scheduler.runTaskTimer(plugin, Runnable {
                        dropRandomQuestion()
                    }, ticks, ticks)
                }
            })
        })
    }

    /**
     * Manually drops a random question, replacing any active one.
     */
    fun dropRandomQuestion(): Boolean {
        if (questionPool.isEmpty()) return false

        // Cancel previous question if no one answered it
        val oldGame = _currentGame.getAndSet(null)
        oldGame?.cancel(silent = true)

        val question = questionPool.random()
        val gameRef = AtomicReference<TriviaGame>()
        val newGame = TriviaGame(plugin, this, question, timeLimitSeconds, rewardAmount) { winner ->
            // Clear current game when finished if it hasn't been replaced
            _currentGame.compareAndSet(gameRef.get(), null)
        }
        gameRef.set(newGame)
        
        _currentGame.set(newGame)
        newGame.start()
        return true
    }

    /**
     * Skips the active question.
     */
    fun skipQuestion(): Boolean {
        val game = _currentGame.get()
        if (game?.isActive == true) {
            game.cancel(silent = false)
            return true
        }
        return false
    }

    /**
     * Forward chat messages to the active game.
     * Runs asynchronously via AsyncChatEvent.
     */
    fun handleChatMessage(player: Player, message: String): Boolean {
        return _currentGame.get()?.handleAnswer(player, message) == true
    }

    fun shutdown() {
        autoDropTask?.cancel()
        _currentGame.get()?.cancel(silent = true)
    }

    fun toggleMute(uuid: UUID): Boolean {
        return if (mutedPlayers.contains(uuid)) {
            mutedPlayers.remove(uuid)
            false // no longer muted
        } else {
            mutedPlayers.add(uuid)
            true // now muted
        }
    }

    fun broadcast(component: net.kyori.adventure.text.Component) {
        Bukkit.getConsoleSender().sendMessage(component)
        for (player in Bukkit.getOnlinePlayers()) {
            if (!mutedPlayers.contains(player.uniqueId)) {
                player.sendMessage(component)
            }
        }
    }
}