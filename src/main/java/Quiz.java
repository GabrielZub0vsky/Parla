package src.main.java;

import java.time.LocalDate;

/**
 * Represents a quiz session, including score, question count, and words quizzed.
 */
public class Quiz {
    protected int id;
    protected String[] words;
    protected int numQuestions;
    protected int score;
    protected LocalDate date;

    /**
     * Creates a new quiz record.
     *
     * @param id the database ID or -1 if not yet persisted
     * @param score the number of correct answers
     * @param numQuestions the total number of quiz prompts
     */
    public Quiz(int id, int score, int numQuestions) {
        this.id = id;
        this.numQuestions = numQuestions;
        this.score = score;
        this.date = LocalDate.now();
    }
}

