import java.util.*;

public class Main {    
    public static void main(String[] args) {
        Words words = new Words();
        Scanner scanner = new Scanner(System.in);

        while(true) {
            System.out.print("\n> ");
            String cmd = scanner.next();

            switch (cmd) {
                case "add":
                    for (int i = 0; i < 5; i++) {
                        String language = scanner.next();
                        String foreignWord = scanner.next();
                        String englishTranslation = scanner.next();
                        if (words.isValid(foreignWord) && words.isValid(englishTranslation)) {
                            words.addWord(language, foreignWord, englishTranslation);
                        } else {
                            System.out.println("Please enter a language, a word in that language, and its English translation, separated by 1 space: \n");
                        }
                    }
                    continue;   
                case "translate":
                    String wordToTranslate = scanner.next();
                    String translation = words.lookupWord(wordToTranslate);
                    if (translation != null) {
                        String language = words.getLanguageOfWord(wordToTranslate);
                        System.out.println("The English translation of " + wordToTranslate + " (" + language + ") is: " + translation + "\n"); 
                    } else {
                        System.out.println("Word not found in database.\n");
                    }

                case "quiz":
                    int numQuestions = scanner.nextInt();
                    String[] quizWords = words.getQuizWords(numQuestions);
                    
                    if (quizWords.length < 1) {
                        System.out.println("Not enough words to create a quiz.\n");
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
                                System.out.println("Incorrect. The correct translation is: " + correctTranslation + "\n");
                                int wordIndex = Integer.parseInt(parts[2]);
                                words.incrementLookupCount(wordIndex);
                            }
                        }
                        System.out.println("Quiz complete! (" + correctCount + "/" + quizWords.length + ")\n");
                        words.addQuizScore(correctCount);
                    }
                    continue;
                case "exit":
                    System.out.println("\nSaving progress...\n");
                    words.saveData();
                    scanner.close();
                    break;
                case "help":
                    System.out.println("Available commands:");
                    System.out.println("add [<language>] [<foreign_word>] [<english_translation>] - Add new words to the database.");
                    System.out.println("translate [<foreign_word>] - Translate a foreign word to English.");
                    System.out.println("quiz [<number_of_questions>] - Take a quiz on the words in the database.");
                    System.out.println("exit - Save progress and exit the program.");
                    System.out.println("help - Show this help message.");
                    continue;
                default:
                    System.out.println("Invalid command. Run help to see commands.\n");
                    continue;
            }
        }
    }
}