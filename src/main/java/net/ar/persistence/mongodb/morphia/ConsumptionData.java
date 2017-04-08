package net.ar.persistence.mongodb.morphia;

import org.mongodb.morphia.annotations.*;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.LocalDate;

/**
 * @author alexander
 */
@Entity(value = "consumption", noClassnameStored = true)
@Indexes(@Index(fields = @Field("mileage")))
public class ConsumptionData {

    @Id
    private String id;

    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    private LocalDate date;

    private int mileage;

    private Double quantity;

    private Double price;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public int getMileage() {
        return mileage;
    }

    public void setMileage(int mileage) {
        this.mileage = mileage;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "ConsumptionData [id=" + id + ", date=" + date + ", mileage=" + mileage + ", quantity=" + quantity
                + ", price=" + price + "]";
    }

}
