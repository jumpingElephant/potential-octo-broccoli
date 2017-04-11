/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.ar.persistence.mongodb.morphia;

import java.util.List;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Field;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Index;
import org.mongodb.morphia.annotations.IndexOptions;
import org.mongodb.morphia.annotations.Indexes;

/**
 *
 * @author alexander
 */
@Entity(value = "automobiles", noClassnameStored = true)
@Indexes(
        @Index(fields = @Field("name"), options = @IndexOptions(unique = true)))
public class Automobile {

    @Id
    private String id;

    private String automobileId;

    private String displayName;

    private List<Bill> bills;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAutomobileId() {
        return automobileId;
    }

    public void setAutomobileId(String automobileId) {
        this.automobileId = automobileId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public List<Bill> getBills() {
        return bills;
    }

    public void setBills(List<Bill> bills) {
        this.bills = bills;
    }

}
