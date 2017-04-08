package net.ar.persistence.mongodb.morphia;

import com.mongodb.BasicDBObject;
import org.mongodb.morphia.converters.SimpleValueConverter;
import org.mongodb.morphia.converters.TypeConverter;
import org.mongodb.morphia.mapping.MappedField;

import java.time.*;
import java.util.Date;

public class LocalDateConverter extends TypeConverter implements SimpleValueConverter {

    public LocalDateConverter() {
        // TODO: Add other date/time supported classes here
        // Other java.time classes: LocalDateTime.class, LocalTime.class
        // Arrays: LocalDateTime[].class, etc
        super(LocalDate.class);
    }

    @Override
    public LocalDate decode(Class<?> targetClass, Object fromDBObject, MappedField optionalExtraInfo) {
        if (fromDBObject == null) {
            return null;
        }

        if (fromDBObject instanceof Date) {
            return ((Date) fromDBObject).toInstant().atZone(ZoneOffset.systemDefault()).toLocalDate();
        }

        if (fromDBObject instanceof LocalDateTime) {
            return ((LocalDateTime) fromDBObject).toLocalDate();
        }

        if (fromDBObject instanceof BasicDBObject) {
            BasicDBObject obj = (BasicDBObject) fromDBObject;
            try {
                int year = obj.getInt("year");
                int month = obj.getInt("month");
                int dayOfMonth = obj.getInt("day");
                return LocalDate.of(year, month, dayOfMonth);
            } catch (NullPointerException e) {
            }
        }

        // TODO: decode other types

        throw new IllegalArgumentException(
                String.format("Cannot decode object of class: %text", fromDBObject.getClass().getName()));
    }

    @Override
    public Date encode(Object value, MappedField optionalExtraInfo) {
        if (value == null) {
            return null;
        }

        if (value instanceof Date) {
            return (Date) value;
        }

        if (value instanceof LocalDateTime) {
            ZonedDateTime zoned = ((LocalDateTime) value).atZone(ZoneOffset.systemDefault());
            return Date.from(zoned.toInstant());
        }

        if (value instanceof LocalDate) {
            Date date = Date.from(((LocalDate) value).atStartOfDay(ZoneId.systemDefault()).toInstant());
            return Date.from(date.toInstant());
        }

        // TODO: encode other types

        throw new IllegalArgumentException(
                String.format("Cannot encode object of class: %text", value.getClass().getName()));
    }
}