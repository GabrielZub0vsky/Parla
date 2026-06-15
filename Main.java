import java.util.*;

public class Main {    
    public static void main(String[] args) {
        Words words = new Words();
        Scanner scanner = new Scanner(System.in);
        System.out.println("Welcome to Parla, your personal language learning assistant!");

        while(true) {
            System.out.println("\nPlease type W if you wish to add words, T to translate a word, Q for a quiz, or E to exit: ");
            char mode = scanner.next().charAt(0);

            if (mode == 'W') {
                System.out.println("\nYou have selected word mode.\n");
                for (int i = 0; i < 5; i++) {
                    System.out.println("Enter a language, a word in that language, and its English translation, all separated by 1 space:\n");
                    String language = scanner.next();
                    String foreignWord = scanner.next();
                    String englishTranslation = scanner.next();
                    if (words.isValid(foreignWord) && words.isValid(englishTranslation)) {
                        System.out.println("Valid word + translation added! (" + (i + 1) + "/5)\n");
                        words.addWord(language, foreignWord, englishTranslation);
                    } else {
                        System.out.println("Invalid word, please enter a valid English word and its valid translation, separated by 1 space: \n");
                    }
                }
            } else if (mode == 'T') {
                System.out.println("\nYou have selected translation mode.\n");
                System.out.println("Enter a foreign word to translate: ");
                String wordToTranslate = scanner.next();
                String translation = words.lookupWord(wordToTranslate);
                if (translation != null) {
                    String language = words.getLanguageOfWord(wordToTranslate);
                    System.out.println("Translate " + wordToTranslate + " from " + language + " to english:\n");
                    System.out.println(translation + "\n");
                } else {
                    System.out.println("Word not found in database. Returning to main menu...\n");
                }
            } else if (mode == 'Q') {
                System.out.println("\nYou have selected quiz mode.\n");
                String[] quizWords = words.getQuizWords(5);
                
                if (quizWords.length == 0) {
                    System.out.println("Not enough words to create a quiz. Please add more words first. Returning to main menu...\n");
                } else {
                    int correctCount = 0;
                    for (String quizItem : quizWords) {
                        String[] parts = quizItem.split("\\|");
                        String foreignWord = parts[0];
                        String correctTranslation = parts[1];
                        
                        System.out.println("Translate: " + foreignWord);
                        String userAnswer = scanner.next();
                        
                        if (userAnswer.equalsIgnoreCase(correctTranslation)) {
                            System.out.println("Correct!\n");
                            correctCount++;
                        } else {
                            System.out.println("Incorrect. The correct translation is: " + correctTranslation);
                            int wordIndex = Integer.parseInt(parts[2]);
                            words.incrementLookupCount(wordIndex);
                            System.out.println();
                        }
                    }
                    System.out.println("Quiz complete! You got " + correctCount + " out of " + quizWords.length + " correct. Returning to main menu...\n");
                    words.addQuizScore(correctCount);
                }
            } else if (mode == 'E') {
                System.out.println("\nThank you for using Parla. Saving your progress...\n");
                words.saveData();
                System.out.println("Goodbye!\n");
                break;
            } else {
                System.out.println("Invalid input, please enter W, T, Q, or E\n");
            }
        }
        
        scanner.close();
    }
}