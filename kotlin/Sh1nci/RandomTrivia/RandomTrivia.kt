package Sh1nci.RandomTrivia

import Sh1nci.RandomTrivia.commands.TriviaCommand
import Sh1nci.RandomTrivia.listeners.ChatListener
import Sh1nci.RandomTrivia.trivia.TriviaManager
import net.milkbowl.vault2.economy.Economy
import org.bukkit.plugin.java.JavaPlugin

class RandomTrivia : JavaPlugin() {

    lateinit var triviaManager: TriviaManager
        private set

    companion object {
        var econ: Economy? = null
            private set
    }

    override fun onEnable() {
        // Copies the bundled config.yml to the server's plugin folder if absent
        saveDefaultConfig()

        if (server.pluginManager.getPlugin("Vault") == null) {
            logger.info("[Untitled] Vault plugin is not installed. Economy rewards are disabled.")
        } else if (!setupEconomy()) {
            logger.info("[Untitled] Vault is installed, but no Economy plugin was found. Economy rewards are disabled.")
        } else {
            logger.info("[Untitled] Vault and Economy plugin hooked successfully! Economy rewards enabled.")
        }

        triviaManager = TriviaManager(this)
        triviaManager.reloadConfig()

        // Register the chat listener
        server.pluginManager.registerEvents(ChatListener(triviaManager), this)

        // Register the /trivia command
        val triviaCmd = TriviaCommand(triviaManager, this)
        getCommand("trivia")?.setExecutor(triviaCmd)
        getCommand("trivia")?.tabCompleter = triviaCmd

        logger.info("[Untitled] Trivia system enabled.")
    }

    override fun onDisable() {
        if (::triviaManager.isInitialized) {
            triviaManager.shutdown()
        }
        logger.info("[Untitled] Trivia system disabled.")
    }

    private fun setupEconomy(): Boolean {
        if (server.pluginManager.getPlugin("Vault") == null) {
            return false
        }
        val rsp = server.servicesManager.getRegistration(Economy::class.java)
        if (rsp == null) {
            return false
        }
        econ = rsp.provider
        return econ != null
    }
}