import java.util.*;
import java.io.*;

public class Words {
    protected ArrayList<String[]> words;
    protected ArrayList<Integer> lookupCounts;
    protected ArrayList<Integer> quizScores;
    private static String DATA_FILE = "parla_data.txt";

    public Words() {
        words = new ArrayList<String[]>();
        lookupCounts = new ArrayList<Integer>();
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
        String[] newWord = {language, foreignWord, englishTranslation};
        words.add(newWord);
        lookupCounts.add(0);
    }

    public String lookupWord(String foreignWord) {
        for (int i = 0; i < words.size(); i++) {
            if (words.get(i)[1].equalsIgnoreCase(foreignWord)) {
                lookupCounts.set(i, lookupCounts.get(i) + 1);
                return words.get(i)[2];
            }
        }
        return null;
    }

    public String getLanguageOfWord(String foreignWord) {
        for (int i = 0; i < words.size(); i++) {
            if (words.get(i)[1].equalsIgnoreCase(foreignWord)) {
                return words.get(i)[0];
            }
        }
        return null;
    }

    public void incrementLookupCount(int index) {
        if (index >= 0 && index < lookupCounts.size()) {
            lookupCounts.set(index, lookupCounts.get(index) + 1);
        }
    }

    public void decrementLookupCount(int index) {
        if (index >= 0 && index < lookupCounts.size()) {
            lookupCounts.set(index, Math.max(0, lookupCounts.get(index) - 1));
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
            if (lookupCounts.get(i) > threshold) {
                frequentIndices.add(i);
            } else {
                rareIndices.add(i);
            }
        }

        Collections.sort(frequentIndices, (a, b) -> lookupCounts.get(b).compareTo(lookupCounts.get(a)));

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
            quizWords[i] = words.get(idx)[1] + "|" + words.get(idx)[2] + "|" + idx;
        }

        return quizWords;
    }

    private int getMaxLookups() {
        int max = 0;
        for (int count : lookupCounts) {
            if (count > max) {
                max = count;
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
                String[] word = words.get(i);
                int lookupCount = lookupCounts.get(i);
                writer.println(word[0] + "|" + word[1] + "|" + word[2] + "|" + lookupCount);
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
                        String foreignWord = parts[1];
                        String englishTranslation = parts[2];
                        int lookupCount = Integer.parseInt(parts[3]);

                        String[] newWord = {language, foreignWord, englishTranslation};
                        words.add(newWord);
                        lookupCounts.add(lookupCount);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading data: " + e.getMessage());
        }
    }

}