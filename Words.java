import java.util.*;
import java.io.*;

public class Words {
    protected ArrayList<Word> words;
    protected ArrayList<Integer> quizScores;
    private static String DATA_FILE = "parla_data.txt";

    public Words() {
        words = new ArrayList<Word>();
        quizScores = new ArrayList<Integer>();
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

    public void addQuizScore(int score) {
        quizScores.add(score);
    }

    public void saveData() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(DATA_FILE))) {
            for (int i = 0; i < words.size(); i++) {
                Word word = words.get(i);
                writer.println(word.language + "|" + word.foreign + "|" + word.english + "|" + word.lookups);
            }
            writer.println("QUIZ_SCORES");
            for (int score : quizScores) {
                writer.println(score);
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
                if (line.equals("QUIZ_SCORES")) {
                    readingScores = true;
                    continue;
                }

                if (readingScores) {
                    try {
                        int score = Integer.parseInt(line);
                        quizScores.add(score);
                    } catch (NumberFormatException e) {
                        System.err.println("Error parsing quiz score: " + line);
                    }
                } else {
                    String[] parts = line.split("\\|");
                    if (parts.length == 4) {
                        String language = parts[0];
                        String foreign = parts[1];
                        String english = parts[2];
                        int lookupCount = Integer.parseInt(parts[3]);

                        Word newWord = new Word();
                        newWord.language = language;
                        newWord.foreign = foreign;
                        newWord.english = english;
                        newWord.lookups = lookupCount;
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