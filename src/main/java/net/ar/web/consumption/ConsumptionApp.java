package net.ar.web.consumption;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import net.ar.persistence.mongodb.MyMongoDB;
import net.ar.persistence.mongodb.morphia.Automobile;
import net.ar.persistence.mongodb.morphia.Bill;
import net.ar.persistence.mongodb.morphia.ConsumptionData;
import org.mongodb.morphia.Key;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.mongodb.morphia.query.UpdateResults;
import static spark.Spark.*;

/**
 * Created by alexander on 01.03.17.
 */
public class ConsumptionApp {

    final static Logger logger = Logger.getLogger(ConsumptionApp.class.getName());

    private static MyMongoDB myMongoDB;

    public static void main0(String[] args) {
        myMongoDB = new MyMongoDB();
        List<ConsumptionData> consumptionList = myMongoDB.getDatastore().find(ConsumptionData.class).asList();
        Automobile auto = new Automobile();
        auto.setAutomobileId("Audi_A3");
        auto.setDisplayName("Audi A3");
        auto.setBills(new ArrayList(consumptionList));
        Key<Automobile> keys = myMongoDB.getDatastore().save(auto);
        System.out.println("keys=[id=" + keys.getId() + ", name=" + keys.getCollection() + "]");
    }

    public static void main(String[] args) {
        logger.info("args=" + Arrays.asList(args).stream().collect(Collectors.joining(", ")));
        if ((args.length == 2) && ("--mongoHost".equals(args[0]))) {
            myMongoDB = new MyMongoDB(args[1]);
        } else {
            myMongoDB = new MyMongoDB();
        }

        staticFileLocation("/public");
        path("/rest", () -> {
            get("/consumption", (req, res) -> {
                return myMongoDB.getDatastore().find(Automobile.class).project("automobileId", true).project("displayName", true).project("_id", false).asList();
            }, new JsonTransformer());

            get("/consumption/:automobileId", (req, res) -> {
                String automobileName = req.params("automobileId");
                return myMongoDB.getDatastore().find(Automobile.class).filter("automobileId", automobileName).get().getBills();
            }, new JsonTransformer());

            post("/consumption/:automobileId", (req, res) -> {
                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new JavaTimeModule());

                String automobileId = req.params("automobileId");
                final Query<Automobile> query = myMongoDB.getDatastore().find(Automobile.class).filter("automobileId", automobileId);
                Automobile auto = query.get();
                if (auto == null) {
                    res.status(400);
                    return createErrorResponseMap("Automobile does not exist.");
                }

                Bill bill = null;
                try {
                    bill = mapper.readValue(req.body(), Bill.class);
                } catch (Exception e) {
                    logger.log(Level.SEVERE, e.getMessage(), e);
                    res.status(400);
                    return createErrorResponseMap("Wrong data format.");
                }
                try {
                    validateData(bill);
                    Integer newMileage = bill.getMileage();
                    auto.getBills().stream()
                            .map(Bill::getMileage)
                            .filter(newMileage::equals)
                            .findAny()
                            .ifPresent(mileage -> {
                                throw new RuntimeException("A Entry with this mileage exists already.");
                            });
                } catch (NullPointerException e) {
                    res.status(400);
                    return createErrorResponseMap(e.getMessage());
                }

                UpdateOperations<Automobile> updateOperations = myMongoDB.getDatastore().createUpdateOperations(Automobile.class).push("bills", bill);
                myMongoDB.getDatastore().update(query, updateOperations);
                return createSuccessResponseMap();
            }, new JsonTransformer());

            delete("/consumption/:automobileId/:mileage", (req, res) -> {
                int automobileId = Integer.parseInt(req.params("automobileId"));
                int mileage = Integer.parseInt(req.params("mileage"));

                UpdateOperations<Automobile> updateOperations = myMongoDB.getDatastore().createUpdateOperations(Automobile.class).removeAll("mileage", mileage);
                Query<Automobile> query = myMongoDB.getDatastore().find(Automobile.class).filter("automobileId", automobileId);
                UpdateResults updateResults = myMongoDB.getDatastore().update(query, updateOperations);
                if (!updateResults.getWriteResult().wasAcknowledged()) {
                    throw new RuntimeException("Deletion failed");
                } else if (updateResults.getUpdatedCount() != 1) {
                    res.status(400);
                    return createErrorResponseMap("Found no such entry.");
                }
                return createSuccessResponseMap();
            }, new JsonTransformer());
        });
        get("/greeting", (req, res) -> "Hello World");
        get("*", (req, res) -> {
            res.status(404);
            res.redirect("/404.html");
            return "";
        });
    }

    private static void validateData(Bill data) {
        Objects.requireNonNull(data.getDate(), "Datum fehlt");
        Objects.requireNonNull(data.getMileage(), "Kilometerstand fehlt");
        Objects.requireNonNull(data.getQuantity(), "Füllmenge fehlt");
        Objects.requireNonNull(data.getPrice(), "Gesamtpreis fehlt");
    }

    private static Map<String, Object> createErrorResponseMap(String errorMessage) {
        Map<String, Object> responseMap = new HashMap<>();
        Map<String, Object> errorMap = new HashMap<>();
        errorMap.put("message", errorMessage);

        responseMap.put("error", errorMap);
        return responseMap;
    }

    private static Map<String, Object> createSuccessResponseMap() {
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("success", true);
        return responseMap;
    }

}
