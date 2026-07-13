import java.time.LocalDate;

public class Quiz {
    protected String[] words;
    protected int numQuestions;
    protected int score;
    protected LocalDate date;

    public Quiz() {
        this.words = new String[0];
        this.numQuestions = 0;
        this.score = 0;
        this.date = LocalDate.now();
    }
}