package org.javlo.helper;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.javlo.helper.json.JavaTimeModule;

import java.io.IOException;
import java.io.Writer;

public class JsonHelper {

    public static final String toJson(Object obj) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper.writeValueAsString(obj);
    }

    public static final void toJson(Object obj, Writer writer) throws JsonProcessingException, IOException {
        String text = toJson(obj);
        writer.write(text);
    }

    // Convert a JSON string to an object of the specified type
    public static <T> T fromJson(String json, Class<T> valueType) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule()); // Support for Java 8 Time API
        return mapper.readValue(json, valueType);
    }

}
