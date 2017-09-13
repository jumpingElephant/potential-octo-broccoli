package net.ar.web.consumption;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mongodb.BasicDBObject;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import net.ar.persistence.mongodb.MyMongoDB;
import net.ar.persistence.mongodb.morphia.Automobile;
import net.ar.persistence.mongodb.morphia.Bill;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.mongodb.morphia.query.UpdateResults;
import org.slf4j.bridge.SLF4JBridgeHandler;
import spark.Request;
import spark.Response;
import static spark.Spark.*;

/**
 * Created by alexander on 01.03.17.
 */
public class ConsumptionApp {

    final static Logger LOGGER = Logger.getLogger(ConsumptionApp.class.getName());

    private static final int CREATED = 201;
    private static final int BAD_REQUEST = 400;
    private static final int NOT_FOUND = 404;

    private static MyMongoDB myMongoDB;

    public static void main(String[] args) {
        LogManager.getLogManager().reset();
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        LOGGER.log(Level.INFO, "args={0}", Arrays.asList(args).stream().collect(Collectors.joining(", ")));
        if ((args.length == 2) && ("--mongoHost".equals(args[0]))) {
            myMongoDB = new MyMongoDB(args[1]);
        } else {
            myMongoDB = new MyMongoDB();
        }

        staticFileLocation("/public");
        path("/api", () -> {
            get("/consumption", ConsumptionApp::readAvailableAutomobiles, new JsonTransformer());
            get("/consumption/:automobileId", ConsumptionApp::readBills, new JsonTransformer());
            post("/consumption/:automobileId", ConsumptionApp::saveBill, new JsonTransformer());
            delete("/consumption/:automobileId/:mileage", ConsumptionApp::deleteBill, new JsonTransformer());
        });
        get("/greeting", (req, res) -> "Hello World");
        get("*", (req, res) -> {
            String msg = new StringBuilder()
                    .append("url=").append(req.url())
                    .toString();
            LOGGER.log(Level.INFO, "Not found: {0}", msg);
            res.status(NOT_FOUND);
            res.redirect("/404.html");
            return "";
        });
    }

    private static List<Automobile> readAvailableAutomobiles(Request req, Response res) {
        return myMongoDB.getDatastore().find(Automobile.class).project("automobileId", true).project("displayName", true).project("_id", false).asList();
    }

    private static List<Bill> readBills(Request req, Response res) {
        String automobileName = req.params("automobileId");
        Automobile automobile = myMongoDB.getDatastore().find(Automobile.class).filter("automobileId", automobileName).get();
        if (automobile == null) {
            res.status(NOT_FOUND);
            return null;
        }
        return automobile.getBills();
    }

    private static Map<String, Object> saveBill(Request req, Response res) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        String automobileId = req.params("automobileId");
        final Query<Automobile> query = myMongoDB.getDatastore().find(Automobile.class).filter("automobileId", automobileId);
        Automobile auto = query.get();
        if (auto == null) {
            res.status(BAD_REQUEST);
            return createErrorResponseMap("Automobile does not exist.");
        }

        Bill bill = null;
        try {
            bill = mapper.readValue(req.body(), Bill.class);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            res.status(BAD_REQUEST);
            return createErrorResponseMap("Invalid data format.");
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
            res.status(BAD_REQUEST);
            return createErrorResponseMap(e.getMessage());
        }

        String message = createPostMessage(bill);
        LOGGER.info(message);

        UpdateOperations<Automobile> updateOperations = myMongoDB.getDatastore().createUpdateOperations(Automobile.class).push("bills", bill);
        myMongoDB.getDatastore().update(query, updateOperations);
        res.status(CREATED);
        return createSuccessResponseMap();
    }

    private static Map<String, Object> deleteBill(Request req, Response res) {
        String automobileId = req.params("automobileId");
        int mileage = Integer.parseInt(req.params("mileage"));

        Datastore datastore = myMongoDB.getDatastore();
        Automobile automobile = datastore.find(Automobile.class).field("automobileId").equal(automobileId).get();
        automobile.getBills().stream()
                .filter(bill -> bill.getMileage() == mileage)
                .findAny()
                .ifPresent(bill -> {
                    LOGGER.log(Level.INFO, "About to delete bill: [mileage={0}, quantity={1}, price={2}]",
                            new Object[]{bill.getMileage(), bill.getQuantity(), bill.getPrice()});
                });

        Query<Automobile> updateQuery = datastore.createQuery(Automobile.class)
                .field("automobileId").equal(automobileId);

        UpdateOperations<Automobile> updateOperations = datastore.createUpdateOperations(Automobile.class)
                .removeAll("bills", new BasicDBObject("mileage", mileage));
        UpdateResults updateResults = datastore.update(updateQuery, updateOperations);

        if (!updateResults.getWriteResult().wasAcknowledged()) {
            throw new RuntimeException("Deletion failed");
        } else if (updateResults.getUpdatedCount() != 1) {
            res.status(BAD_REQUEST);
            return createErrorResponseMap("Found no such entry.");
        }
        return createSuccessResponseMap();
    }

    private static String createPostMessage(Bill bill) {
        String message = new StringBuilder()
                .append("Pushing bill: [")
                .append("date=").append(bill.getDate().format(DateTimeFormatter.ISO_DATE))
                .append(", mileage=").append(bill.getMileage())
                .append(", quantity=").append(bill.getQuantity())
                .append(", price=")
                .append(bill.getPrice())
                .append("] to MongoDB").toString();
        return message;
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
