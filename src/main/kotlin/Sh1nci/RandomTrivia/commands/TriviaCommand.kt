package Sh1nci.RandomTrivia.commands

import Sh1nci.RandomTrivia.trivia.TriviaManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.plugin.java.JavaPlugin

/**
 * /trivia — root command for the trivia system.
 *
 * Sub-commands:
 *   /trivia drop     — instantly drop a random question into chat
 *   /trivia skip     — skip the current question
 *   /trivia reload   — reload config.yml and question pool
 *   /trivia mute     — toggle seeing trivia broadcasts
 */
class TriviaCommand(
    private val triviaManager: TriviaManager,
    private val plugin: JavaPlugin
) : CommandExecutor, TabCompleter {

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        if (args.isEmpty()) {
            sendHelp(sender)
            return true
        }

        // The mute command is accessible to players with randomtrivia.play.skip
        if (args[0].lowercase() == "mute") {
            if (!sender.hasPermission("randomtrivia.play.mute")) {
                sender.sendMessage(noPermission())
                return true
            }
            handleMute(sender)
            return true
        }

        // The rest are admin commands
        if (!sender.hasPermission("randomtrivia.admin")) {
            sender.sendMessage(noPermission())
            return true
        }

        when (args[0].lowercase()) {
            "drop"   -> handleDrop(sender)
            "skip"   -> handleSkip(sender)
            "reload" -> handleReload(sender)
            else     -> sendHelp(sender)
        }
        return true
    }

    private fun handleDrop(sender: CommandSender) {
        val success = triviaManager.dropRandomQuestion()
        if (!success) {
            sender.sendMessage(error("No questions available in the pool. Check your config.yml!"))
        }
    }

    private fun handleSkip(sender: CommandSender) {
        val skipped = triviaManager.skipQuestion()
        if (!skipped) {
            sender.sendMessage(error("No trivia question is currently active."))
        }
    }

    private fun handleReload(sender: CommandSender) {
        triviaManager.reloadConfig()
        sender.sendMessage(info("Trivia config and question pool reloaded!"))
    }

    private fun handleMute(sender: CommandSender) {
        if (sender !is org.bukkit.entity.Player) {
            sender.sendMessage(error("Only players can mute trivia."))
            return
        }
        val isMuted = triviaManager.toggleMute(sender.uniqueId)
        if (isMuted) {
            sender.sendMessage(error("You have muted trivia broadcasts."))
        } else {
            sender.sendMessage(info("You will now see trivia broadcasts again."))
        }
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): List<String> {
        val completions = mutableListOf<String>()
        if (args.size == 1) {
            if (sender.hasPermission("randomtrivia.admin")) {
                completions.addAll(listOf("drop", "skip", "reload"))
            }
            if (sender.hasPermission("randomtrivia.play.mute")) {
                completions.add("mute")
            }
            return completions.filter { it.startsWith(args[0].lowercase()) }
        }
        return emptyList()
    }

    private fun sendHelp(sender: CommandSender) {
        sender.sendMessage(Component.text("  TRIVIA COMMANDS", NamedTextColor.BLUE, TextDecoration.BOLD))
        val commands = mutableListOf<Pair<String, String>>()
        
        if (sender.hasPermission("randomtrivia.admin")) {
            commands.add("/trivia drop" to "Instantly drop a random question")
            commands.add("/trivia skip" to "Skip the active question")
            commands.add("/trivia reload" to "Reload config.yml question pool")
        }
        
        if (sender.hasPermission("randomtrivia.play.mute")) {
            commands.add("/trivia mute" to "Toggle seeing trivia broadcasts")
        }

        commands.forEach { (cmd, desc) ->
            sender.sendMessage(
                Component.text("  $cmd", NamedTextColor.AQUA)
                    .append(Component.text(" — $desc", NamedTextColor.GRAY))
            )
        }
    }

    private fun error(msg: String) = Component.text("  ✖ $msg", NamedTextColor.RED)
    private fun info(msg: String) = Component.text("  ✔ $msg", NamedTextColor.GREEN)
    private fun noPermission() = error("You don't have permission to do that.")
}