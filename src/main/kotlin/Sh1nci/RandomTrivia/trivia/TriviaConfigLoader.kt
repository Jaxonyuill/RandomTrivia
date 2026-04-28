package Sh1nci.RandomTrivia.trivia

import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.plugin.java.JavaPlugin

data class TriviaConfigData(
    val intervalSeconds: Long,
    val timeLimitSeconds: Long,
    val rewardAmount: Double,
    val questions: List<TriviaQuestion>
)

object TriviaConfigLoader {

    fun load(plugin: JavaPlugin): TriviaConfigData {
        val config: FileConfiguration = plugin.config

        val interval = config.getLong("interval-seconds", 300L)
        val timeLimit = config.getLong("answer-time-limit", 30L)
        val rewardAmount = config.getDouble("reward-amount", 0.0)
        val questionList = config.getMapList("questions")

        if (questionList.isEmpty()) {
            plugin.logger.warning("[Trivia] No 'questions' found in config.yml.")
            return TriviaConfigData(interval, timeLimit, rewardAmount, emptyList())
        }

        val questions = mutableListOf<TriviaQuestion>()

        questionList.forEachIndexed { index, raw ->
            @Suppress("UNCHECKED_CAST")
            val map = raw as? Map<String, Any> ?: run {
                plugin.logger.warning("[Trivia] Question #${index + 1} is malformed — skipping.")
                return@forEachIndexed
            }

            val questionText = map["question"] as? String ?: run {
                plugin.logger.warning("[Trivia] Question #${index + 1} missing 'question' field — skipping.")
                return@forEachIndexed
            }

            @Suppress("UNCHECKED_CAST")
            val answers = (map["answers"] as? List<*>)
                ?.filterIsInstance<String>()
                ?.filter { it.isNotBlank() }
                ?: emptyList()

            if (answers.isEmpty()) {
                plugin.logger.warning("[Trivia] Question #${index + 1} has no valid answers — skipping.")
                return@forEachIndexed
            }

            val hint = map["hint"] as? String

            questions += TriviaQuestion(
                question = questionText,
                answers  = answers,
                hint     = hint
            )
        }

        plugin.logger.info("[Trivia] Loaded ${questions.size} question(s). Auto-drop interval: $interval s | Time limit: $timeLimit s | Reward: $rewardAmount")
        return TriviaConfigData(interval, timeLimit, rewardAmount, questions)
    }
}