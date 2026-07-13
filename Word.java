import java.time.LocalDate;

public class Word {
    protected String language;
    protected String foreign;
    protected String english;
    protected int lookups;
    protected LocalDate addedDate;

    public Word() {
        this.language = "";
        this.foreign = "";
        this.english = "";
        this.lookups = 0;
        this.addedDate = LocalDate.now();
    }
}