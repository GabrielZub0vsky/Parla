import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.io.*;

public class Words {
    protected ArrayList<Word> words;
    protected ArrayList<Quiz> quizzes;
    private static final String DATA_FILE = "parla_data.txt";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    public Words() {
        words = new ArrayList<Word>();
        quizzes = new ArrayList<Quiz>();
        loadData();
    }

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

    public void addWord(String language, String foreignWord, String englishTranslation) {
        Word newWord = new Word();
        newWord.language = language;
        newWord.foreign = foreignWord;
        newWord.english = englishTranslation;
        newWord.lookups = 0;
        words.add(newWord);
    }

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

    public String lookup(String foreignWord) {
        for (int i = 0; i < words.size(); i++) {
            if (words.get(i).foreign.equalsIgnoreCase(foreignWord)) {
                words.get(i).lookups++;
                return words.get(i).english;
            }
        }
        return null;
    }

    public String getLanguage(String foreignWord) {
        for (int i = 0; i < words.size(); i++) {
            if (words.get(i).foreign.equalsIgnoreCase(foreignWord)) {
                return words.get(i).language;
            }
        }
        return null;
    }

    public void incrementLookup(int index) {
        if (index >= 0 && index < words.size()) {
            words.get(index).lookups++;
        }
    }

    public void decrementLookup(int index) {
        if (index >= 0 && index < words.size()) {
            words.get(index).lookups = Math.max(0, words.get(index).lookups - 1);
        }
    }

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

    public void addQuiz(Quiz quiz) {
        quizzes.add(quiz);
    }

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

    public void saveData() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(DATA_FILE))) {
            for (int i = 0; i < words.size(); i++) {
                Word word = words.get(i);
                writer.println(word.language + "|" + word.foreign + "|" + word.english + "|" + word.lookups + "|" + word.addedDate.format(DATE_FORMAT));
            }
            writer.println("QUIZZES");
            for (Quiz quiz : quizzes) {
                writer.print(quiz.date.format(DATE_FORMAT) + "|" + quiz.score + "|" + quiz.numQuestions);
                if (quiz.words.length > 0) {
                    writer.print("|");
                    for (int i = 0; i < quiz.words.length; i++) {
                        if (i > 0) {
                            writer.print(";");
                        }
                        writer.print(quiz.words[i]);
                    }
                }
                writer.println();
            }
        } catch (IOException e) {
            System.err.println("Error saving data: " + e.getMessage());
        }
    }

    public void loadData() {
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(DATA_FILE))) {
            String line;
            boolean readingScores = false;

            while ((line = reader.readLine()) != null) {
                if (line.equals("QUIZZES")) {
                    readingScores = true;
                    continue;
                }

                if (readingScores) {
                    String[] parts = line.split("\\|");
                    if (parts.length >= 3) {
                        try {
                            Quiz quiz = new Quiz();
                            quiz.date = LocalDate.parse(parts[0], DATE_FORMAT);
                            quiz.score = Integer.parseInt(parts[1]);
                            quiz.numQuestions = Integer.parseInt(parts[2]);
                            if (parts.length == 4 && !parts[3].isEmpty()) {
                                quiz.words = parts[3].split(";");
                            } else {
                                quiz.words = new String[0];
                            }
                            quizzes.add(quiz);
                        } catch (Exception e) {
                            System.err.println("Error parsing quiz history: " + line);
                        }
                    } else {
                        System.err.println("Invalid quiz data format: " + line);
                    }
                } else {
                    String[] parts = line.split("\\|");
                    if (parts.length == 5) {
                        String language = parts[0];
                        String foreign = parts[1];
                        String english = parts[2];
                        int lookupCount = Integer.parseInt(parts[3]);
                        LocalDate addedDate = LocalDate.parse(parts[4], DATE_FORMAT);

                        Word newWord = new Word();
                        newWord.language = language;
                        newWord.foreign = foreign;
                        newWord.english = english;
                        newWord.lookups = lookupCount;
                        newWord.addedDate = addedDate;
                        words.add(newWord);
                    } else if (parts.length == 4) {
                        String language = parts[0];
                        String foreign = parts[1];
                        String english = parts[2];
                        int lookupCount = Integer.parseInt(parts[3]);

                        Word newWord = new Word();
                        newWord.language = language;
                        newWord.foreign = foreign;
                        newWord.english = english;
                        newWord.lookups = lookupCount;
                        newWord.addedDate = LocalDate.now();
                        words.add(newWord);
                    } else {
                        System.err.println("Invalid data format: " + line);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading data: " + e.getMessage());
        }
    }
}
