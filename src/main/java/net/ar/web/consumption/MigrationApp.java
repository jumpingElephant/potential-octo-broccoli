package net.ar.web.consumption;

import java.util.ArrayList;
import java.util.List;
import net.ar.persistence.mongodb.MyMongoDB;
import net.ar.persistence.mongodb.morphia.Automobile;
import net.ar.persistence.mongodb.morphia.ConsumptionData;
import org.mongodb.morphia.Key;

/**
 * Created by alexander on 01.03.17.
 */
public class MigrationApp {

    private static MyMongoDB myMongoDB;

    public static void main(String[] args) {
        myMongoDB = new MyMongoDB();
        List<ConsumptionData> consumptionList = myMongoDB.getDatastore().find(ConsumptionData.class).asList();
        Automobile auto = new Automobile();
        auto.setAutomobileId("Audi_A3");
        auto.setDisplayName("Audi A3");
        auto.setBills(new ArrayList(consumptionList));
        Key<Automobile> keys = myMongoDB.getDatastore().save(auto);
        System.out.println("keys=[id=" + keys.getId() + ", name=" + keys.getCollection() + "]");
    }

}
