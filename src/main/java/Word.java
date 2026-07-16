package src.main.java;

import java.time.LocalDate;

/**
 * Represents a single vocabulary entry stored by the application.
 * <p>
 * Each word contains the original foreign text, its English translation,
 * the language name, the number of times it has been looked up, and the
 * date it was added.
 */
public class Word {
    protected int id;
    protected String language;
    protected String foreign;
    protected String english;
    protected int lookups;
    protected LocalDate addedDate;

    /**
     * Creates a new Word record.
     *
     * @param id the database ID, or -1 if not yet persisted
     * @param language the language name
     * @param foreign the foreign word text
     * @param english the English translation
     * @param lookups how many times the word has been looked up
     * @param addedDate the date the word was created
     */
    public Word(int id, String language, String foreign, String english, int lookups, LocalDate addedDate) {
        this.id = id;
        this.language = language;
        this.foreign = foreign;
        this.english = english;
        this.lookups = lookups;
        this.addedDate = addedDate;
    }
}
