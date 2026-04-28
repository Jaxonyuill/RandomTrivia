package Sh1nci.RandomTrivia.trivia

/**
 * Represents a single trivia question.
 *
 * @param question     The question text shown in chat.
 * @param answers      All accepted correct answers (case-insensitive comparison is applied at runtime).
 * @param hint         An optional hint revealed mid-round.
 * @param points       How many points a correct answer awards.
 * @param timeSeconds  How long players have to answer.
 */
data class TriviaQuestion(
    val question: String,
    val answers: List<String>,
    val hint: String? = null
)