import java.util.*;

public class Main {    
    public static void main(String[] args) {
        Words words = new Words();
        Scanner scanner = new Scanner(System.in);

        while(true) {
            System.out.print("\n> ");
            String cmd = scanner.next();

            switch (cmd) {
                case "add": {
                    String language = scanner.next();
                    String foreignWord = scanner.next();
                    String english = scanner.next();
                    if (words.isValid(foreignWord) && words.isValid(english)) {
                        words.addWord(language, foreignWord, english);
                        System.out.println("Word added: " + foreignWord + " -> " + english + " (" + language + ")\n");
                    } else {
                        System.out.println("Invalid input.\n");
                        scanner.nextLine(); // Clear the invalid input
                    }
                }
                    continue;
                case "translate":
                    String foreignWord = scanner.next();
                    String translation = words.lookup(foreignWord);
                    if (translation != null) {
                        String wordLanguage = words.getLanguage(foreignWord);
                        System.out.println("The English translation of " + foreignWord + " (" + wordLanguage + ") is: " + translation + "\n"); 
                    } else {
                        System.out.println("Word not found in database.\n");
                    }
                    continue;
                case "quiz":
                    int numQuestions = scanner.nextInt();
                    String[] quizWords = (numQuestions >= 1) ? words.getQuizWords(numQuestions) : new String[0];

                    if (quizWords.length < 1) {
                        System.out.println("Not enough words to create a quiz.\n");
                    } else {
                        int correctCount = 0;
                        String[] quizzedWords = new String[quizWords.length];
                        for (int q = 0; q < quizWords.length; q++) {
                            String[] parts = quizWords[q].split("\\|");
                            String quizForeign = parts[0];
                            String correctTranslation = parts[1];
                            int wordIndex = Integer.parseInt(parts[2]);
                            quizzedWords[q] = quizForeign + "^" + correctTranslation;

                            System.out.println("Translate: " + quizForeign);
                            String userAnswer = scanner.next();

                            if (userAnswer.equalsIgnoreCase(correctTranslation)) {
                                System.out.println("Correct!\n");
                                correctCount++;
                                words.decrementLookup(wordIndex);
                            } else {
                                System.out.println("Incorrect. The correct translation is: " + correctTranslation + "\n");
                                words.incrementLookup(wordIndex);
                            }
                        }
                        System.out.println("Quiz complete! (" + correctCount + "/" + quizWords.length + ")\n");
                        Quiz quiz = new Quiz();
                        quiz.score = correctCount;
                        quiz.numQuestions = quizWords.length;
                        quiz.words = quizzedWords;
                        words.addQuiz(quiz);
                    }
                    continue;
                case "stats":
                    words.displayStats();
                    continue;
                case "list":
                    words.displayWordsByDate();
                    continue;
                case "exit":
                    System.out.println("\nSaving progress...\n");
                    words.saveData();
                    scanner.close();
                    return;
                case "help":
                    System.out.println("Available commands:");
                    System.out.println("add <language> <foreign_word> <english_translation> - Add new words to the database.");
                    System.out.println("translate <foreign_word> - Translate a foreign word to English.");
                    System.out.println("quiz <number_of_questions> - Take a quiz on the words in the database.");
                    System.out.println("list - Display all words sorted by the date they were added.");
                    System.out.println("stats - Display statistics and quiz history with scores and words quizzed.");
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