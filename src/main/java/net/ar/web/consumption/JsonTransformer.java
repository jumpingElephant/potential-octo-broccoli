package net.ar.web.consumption;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import spark.ResponseTransformer;

/**
 * Created by alexander on 01.03.17.
 */
public class JsonTransformer implements ResponseTransformer {
    @Override
    public String render(Object model) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        String s = mapper.writeValueAsString(model);
        return s;
    }
}
