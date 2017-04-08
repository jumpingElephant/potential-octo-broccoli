package net.ar.persistence.mongodb.morphia;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.NoSuchElementException;
import java.util.Optional;

public class DateString {

    private final String text;
    private final TemporalAccessor temporalAccessor;

    private DateString(String text, TemporalAccessor temporalAccessor) {
        this.text = text;
        this.temporalAccessor = temporalAccessor;
    }

    public static DateString of(String text) {
        return new DateString(text, null);
    }

    public DateString parse(DateTimeFormatter formatter) {
        try {
            return new DateString(text, (formatter.parse(text)));
        } catch (DateTimeParseException e) {
            return new DateString(text, null);
        }
    }

    public DateString orElse(DateTimeFormatter formatter) {
        return temporalAccessor != null ? this : parse(formatter);
    }

    /**
     * Returns an Optional describing the parsed date as LocalDate, if text
     * could get parsed, otherwise returns an empty Optional.
     *
     * @return
     */
    public Optional<LocalDate> mayBeLocalDate() {
        return Optional.ofNullable(LocalDate.from(temporalAccessor));
    }

    /**
     * If the text could get parsed in this DateString, returns the value as
     * LocalDate, otherwise throws NoSuchElementException.
     *
     * @return the non-null value held by this {@code Optional}
     * @throws NoSuchElementException if the text could not get parsed
     */
    public LocalDate toLocalDate() {
        return mayBeLocalDate().get();
    }

}