package src.main.java;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.sql.*;

/**
 * Manages the vocabulary and quiz history for the application.
 * <p>
 * This class keeps an in-memory list of words and quiz results while also
 * persisting changes to a PostgreSQL database. It loads existing data on
 * construction and updates database records as words and quizzes are modified.
 */
public class Words {
    protected ArrayList<Word> words;
    protected ArrayList<Quiz> quizzes;
    private Connection conn;
    private DatabaseHelper db;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    /**
     * Creates a new Words manager and initializes the SQLite database.
     * <p>
     * The constructor opens a connection through {@link DatabaseHelper}
     * and loads any existing words and quiz history from the database.
     */
    public Words() {
        words = new ArrayList<>();
        quizzes = new ArrayList<>();
        db = new DatabaseHelper();

        try {
            db.connect();
            conn = db.getConnection();
            loadData();
        } catch (SQLException e) {
            System.err.println("Unable to initialize database: " + e.getMessage());
        }
    }

    /**
     * Validates that a word is long enough and contains only letters.
     *
     * @param word the word to validate
     * @return {@code true} if the word is valid, {@code false} otherwise
     */
    public boolean isValid(String word) {
        if (word.length() < 2) {
            return false;
        }
        for (char c : word.toCharArray()) {
            if (!Character.isLetter(c)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Adds a new word to the in-memory list and persists it in the database.
     *
     * @param language the language of the foreign word
     * @param foreignWord the word to translate
     * @param englishTranslation the English translation for the foreign word
     */
    public void addWord(String language, String foreignWord, String englishTranslation) {
        Word newWord = new Word(-1, language, foreignWord, englishTranslation, 0, LocalDate.now());

        if (conn != null) {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO words(language, foreign_word, english, lookups, added_date) VALUES (?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, language);
                stmt.setString(2, foreignWord);
                stmt.setString(3, englishTranslation);
                stmt.setInt(4, newWord.lookups);
                stmt.setString(5, newWord.addedDate.format(DATE_FORMAT));
                stmt.executeUpdate();

                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        newWord.id = keys.getInt(1);
                    }
                }
            } catch (SQLException e) {
                System.err.println("Error inserting word into database: " + e.getMessage());
            }
        }

        words.add(newWord);
    }

    /**
     * Prints all stored words in order by the date they were added.
     * <p>
     * The output includes language, foreign word, English translation, lookup count,
     * and the added date for each entry.
     */
    public void displayWordsByDate() {
        if (words.isEmpty()) {
            System.out.println("\nNo words in the database.\n");
            return;
        }

        List<Word> sortedWords = new ArrayList<>(words);
        Collections.sort(sortedWords, Comparator.comparing(w -> w.addedDate));

        System.out.println("\n--- Words by Added Date ---");
        for (Word word : sortedWords) {
            System.out.printf("%s | %s -> %s | lookups: %d | added: %s%n",
                    word.language,
                    word.foreign,
                    word.english,
                    word.lookups,
                    word.addedDate.format(DATE_FORMAT));
        }
        System.out.println("---------------------------\n");
    }

    /**
     * Looks up the English translation of a foreign word.
     * <p>
     * If the word exists, the lookup count is incremented and stored.
     *
     * @param foreignWord the foreign word to translate
     * @return the English translation or {@code null} if not found
     */
    public String lookup(String foreignWord) {
        for (Word word : words) {
            if (word.foreign.equalsIgnoreCase(foreignWord)) {
                word.lookups++;
                updateWordLookups(word);
                return word.english;
            }
        }
        return null;
    }

    /**
     * Returns the language for a given foreign word.
     *
     * @param foreignWord the foreign word to inspect
     * @return the language name or {@code null} if the word is not found
     */
    public String getLanguage(String foreignWord) {
        for (Word word : words) {
            if (word.foreign.equalsIgnoreCase(foreignWord)) {
                return word.language;
            }
        }
        return null;
    }

    /**
     * Increases the lookup count for a word by index and saves the update.
     *
     * @param index the index of the word in the in-memory list
     */
    public void incrementLookup(int index) {
        if (index >= 0 && index < words.size()) {
            Word word = words.get(index);
            word.lookups++;
            updateWordLookups(word);
        }
    }

    /**
     * Decreases the lookup count for a word by index but never below zero.
     *
     * @param index the index of the word in the in-memory list
     */
    public void decrementLookup(int index) {
        if (index >= 0 && index < words.size()) {
            Word word = words.get(index);
            word.lookups = Math.max(0, word.lookups - 1);
            updateWordLookups(word);
        }
    }

    /**
     * Selects a set of quiz words based on lookup frequency.
     * <p>
     * Higher-lookup words are prioritized for the quiz, while a small
     * fraction of less-searched words is always included.
     *
     * @param quizSize the number of quiz questions requested
     * @return an array of quiz entries formatted as foreign|english|index
     */
    public String[] getQuizWords(int quizSize) {
        if (words.isEmpty()) {
            return new String[0];
        }

        List<Integer> selectedIndices = new ArrayList<>();
        int rareDWordsCount = Math.max(1, quizSize / 10);
        int frequentWordsCount = quizSize - rareDWordsCount;

        List<Integer> frequentIndices = new ArrayList<>();
        List<Integer> rareIndices = new ArrayList<>();

        int maxLookups = getMaxLookups();
        int threshold = Math.max(1, maxLookups / 3);

        for (int i = 0; i < words.size(); i++) {
            if (words.get(i).lookups > threshold) {
                frequentIndices.add(i);
            } else {
                rareIndices.add(i);
            }
        }

        Collections.sort(frequentIndices, (a, b) -> words.get(b).lookups - words.get(a).lookups);

        Random rand = new Random();

        for (int i = 0; i < frequentWordsCount && i < frequentIndices.size(); i++) {
            selectedIndices.add(frequentIndices.get(i));
        }

        if (frequentIndices.size() < frequentWordsCount && !rareIndices.isEmpty()) {
            for (int i = frequentIndices.size(); i < frequentWordsCount && !rareIndices.isEmpty(); i++) {
                int randomIndex = rand.nextInt(rareIndices.size());
                selectedIndices.add(rareIndices.remove(randomIndex));
            }
        }

        for (int i = 0; i < rareDWordsCount && !rareIndices.isEmpty(); i++) {
            int randomIndex = rand.nextInt(rareIndices.size());
            selectedIndices.add(rareIndices.remove(randomIndex));
        }

        String[] quizWords = new String[selectedIndices.size()];
        for (int i = 0; i < selectedIndices.size(); i++) {
            int idx = selectedIndices.get(i);
            quizWords[i] = words.get(idx).foreign + "|" + words.get(idx).english + "|" + idx;
        }

        return quizWords;
    }

    private int getMaxLookups() {
        int max = 0;
        for (Word word : words) {
            if (word.lookups > max) {
                max = word.lookups;
            }
        }
        return max;
    }

    /**
     * Saves a completed quiz to the database and adds it to history.
     *
     * @param quiz the quiz result to persist
     */
    public void addQuiz(Quiz quiz) {
        if (conn != null) {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO quizzes(date, score, num_questions) VALUES (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, quiz.date.format(DATE_FORMAT));
                stmt.setInt(2, quiz.score);
                stmt.setInt(3, quiz.numQuestions);
                stmt.executeUpdate();

                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        quiz.id = keys.getInt(1);
                    }
                }
            } catch (SQLException e) {
                System.err.println("Error inserting quiz into database: " + e.getMessage());
            }

            if (quiz.words.length > 0 && quiz.id >= 0) {
                try (PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO quiz_words(quiz_id, foreign_word, translation) VALUES (?, ?, ?)") ) {
                    for (String quizWord : quiz.words) {
                        String[] parts = quizWord.split("\\^", 2);
                        String foreign = parts[0];
                        String translation = parts.length > 1 ? parts[1] : "";
                        stmt.setInt(1, quiz.id);
                        stmt.setString(2, foreign);
                        stmt.setString(3, translation);
                        stmt.addBatch();
                    }
                    stmt.executeBatch();
                } catch (SQLException e) {
                    System.err.println("Error inserting quiz words into database: " + e.getMessage());
                }
            }
        }

        quizzes.add(quiz);
    }

    /**
     * Prints summary statistics and quiz history to the console.
     */
    public void displayStats() {
        System.out.println("\n--- Statistics ---");
        System.out.println("Total words in database: " + words.size());
        System.out.println("Quizzes taken: " + quizzes.size());

        System.out.println("\n--- Quiz History ---");
        if (quizzes.isEmpty()) {
            System.out.println("No quiz history available.");
        } else {
            List<Quiz> sortedQuizzes = new ArrayList<>(quizzes);
            Collections.sort(sortedQuizzes, Comparator.comparing(q -> q.date));
            for (Quiz quiz : sortedQuizzes) {
                System.out.println("Date: " + quiz.date.format(DATE_FORMAT));
                System.out.println("Score: " + quiz.score + "/" + quiz.numQuestions);
                if (quiz.words.length > 0) {
                    System.out.println("Words quizzed:");
                    for (String wordItem : quiz.words) {
                        String[] parts = wordItem.split("\\^", 2);
                        String foreign = parts[0];
                        String translation = parts.length > 1 ? parts[1] : "";
                        System.out.println("  " + foreign + " -> " + translation);
                    }
                }
                System.out.println();
            }
        }
        System.out.println("------------------\n");
    }

    /**
     * Closes the database connection and flushes any pending resources.
     */
    public void saveData() {
        try {
            db.close();
        } catch (SQLException e) {
            System.err.println("Error closing database: " + e.getMessage());
        }
    }

    /**
     * Loads saved words and quiz history from the PostgreSQL database.
     */
    private void loadData() {
        if (conn == null) {
            return;
        }

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, language, foreign_word, english, lookups, added_date FROM words")) {
            while (rs.next()) {
                Word word = new Word( rs.getInt("id"), 
                                      rs.getString("language"), 
                                      rs.getString("foreign_word"), 
                                      rs.getString("english"), 
                                      rs.getInt("lookups"), 
                                      LocalDate.parse(rs.getString("added_date"), DATE_FORMAT));
                words.add(word);
            }
        } catch (SQLException e) {
            System.err.println("Error loading words from database: " + e.getMessage());
        }

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, date, score, num_questions FROM quizzes ORDER BY date")) {
            while (rs.next()) {
                Quiz quiz = new Quiz( rs.getInt("id"), 
                                      rs.getInt("num_questions"), 
                                      rs.getInt("score"));

                try (PreparedStatement questionStmt = conn.prepareStatement(
                        "SELECT foreign_word, translation FROM quiz_words WHERE quiz_id = ? ORDER BY id")) {
                    questionStmt.setInt(1, quiz.id);
                    try (ResultSet quizWords = questionStmt.executeQuery()) {
                        List<String> quizWordList = new ArrayList<>();
                        while (quizWords.next()) {
                            String foreign = quizWords.getString("foreign_word");
                            String translation = quizWords.getString("translation");
                            quizWordList.add(foreign + "^" + translation);
                        }
                        quiz.words = quizWordList.toArray(new String[0]);
                    }
                }

                quizzes.add(quiz);
            }
        } catch (SQLException e) {
            System.err.println("Error loading quizzes from database: " + e.getMessage());
        }
    }

    /**
     * Updates the stored lookup count for a single word record.
     *
     * @param word the word whose lookup count should be persisted
     */
    private void updateWordLookups(Word word) {
        if (conn == null || word.id < 0) {
            return;
        }

        try (PreparedStatement stmt = conn.prepareStatement(
                "UPDATE words SET lookups = ? WHERE id = ?")) {
            stmt.setInt(1, word.lookups);
            stmt.setInt(2, word.id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating word lookups: " + e.getMessage());
        }
    }
}
