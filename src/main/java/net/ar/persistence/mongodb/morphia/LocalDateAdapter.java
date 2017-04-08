package net.ar.persistence.mongodb.morphia;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LocalDateAdapter extends XmlAdapter<String, LocalDate> {

    @Override
    public LocalDate unmarshal(String v) throws Exception {
        return DateString.of(v).parse(DateTimeFormatter.BASIC_ISO_DATE).orElse(DateTimeFormatter.ISO_DATE)
                .orElse(DateTimeFormatter.ISO_OFFSET_DATE).orElse(DateTimeFormatter.ISO_OFFSET_DATE_TIME).toLocalDate();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * javax.xml.bind.annotation.adapters.XmlAdapter#marshal(java.lang.Object)
     */
    @Override
    public String marshal(LocalDate v) throws Exception {
        return DateTimeFormatter.ISO_DATE.format(v);
    }

}