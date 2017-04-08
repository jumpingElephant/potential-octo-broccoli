package net.ar.persistence.mongodb;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import net.ar.persistence.mongodb.morphia.LocalDateConverter;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

/**
 * @author Alexander Rettner
 */
public class MyMongoDB {

    private static final String MORPHIA_MAP_PACKAGE = "net.ar.persistence.mongodb.morphia";
    private final MongoClient mongoClient;
    private final String mongoURIString;
    private Datastore carMaintenanceDatastore;

    public MyMongoDB(String hostName) {
        mongoURIString = "mongodb://" + hostName.trim();
        mongoClient = new MongoClient(new MongoClientURI(mongoURIString));
    }

    public MyMongoDB() {
        this("localhost");
    }

    public Datastore getDatastore() {
        if (carMaintenanceDatastore == null) {
            Morphia morphia = new Morphia();
            morphia.mapPackage(MORPHIA_MAP_PACKAGE);
            morphia.getMapper().getConverters().addConverter(new LocalDateConverter());
            carMaintenanceDatastore = morphia.createDatastore(mongoClient, "carMaintenance");
        }
        return carMaintenanceDatastore;
    }

    public void closeConnection() {
        mongoClient.close();
    }

}
