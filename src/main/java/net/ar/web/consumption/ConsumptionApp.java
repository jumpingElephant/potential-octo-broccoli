package net.ar.web.consumption;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mongodb.WriteResult;
import net.ar.persistence.mongodb.MyMongoDB;
import net.ar.persistence.mongodb.morphia.ConsumptionData;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static spark.Spark.*;


/**
 * Created by alexander on 01.03.17.
 */
public class ConsumptionApp {

    final static Logger logger = Logger.getLogger(ConsumptionApp.class.getName());

    private static MyMongoDB myMongoDB;

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
                return myMongoDB.getDatastore().find(ConsumptionData.class).asList();
            }, new JsonTransformer());

            post("/consumption", (req, res) -> {
                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new JavaTimeModule());
                ConsumptionData data = null;
                try {
                    data = mapper.readValue(req.body(), ConsumptionData.class);
                } catch (Exception e) {
                    e.printStackTrace();
                    res.status(400);
                    return createErrorResponseMap("Wrong data format.");
                }
                try {
                    validateData(data);
                } catch (NullPointerException e) {
                    res.status(400);
                    return createErrorResponseMap(e.getMessage());
                }
                if (myMongoDB.getDatastore().find(ConsumptionData.class, "mileage", data.getMileage())
                        .count() > 0) {
                    res.status(400);
                    return createErrorResponseMap("A Entry with this mileage exists already.");
                }
                myMongoDB.getDatastore().save(data);
                return createSuccessResponseMap();
            }, new JsonTransformer());

            delete("/consumption/:mileage", (req, res) -> {
                int mileage = Integer.parseInt(req.params("mileage"));

                WriteResult deleteResult = myMongoDB.getDatastore()
                        .delete(myMongoDB.getDatastore().find(ConsumptionData.class, "mileage", mileage));
                if (!deleteResult.wasAcknowledged()) {
                    throw new RuntimeException("Deletion failed");
                } else if (deleteResult.getN() != 1) {
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

    private static void validateData(ConsumptionData data) {
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
