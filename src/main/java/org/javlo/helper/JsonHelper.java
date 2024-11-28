package org.javlo.helper;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.javlo.helper.json.JavaTimeModule;

import java.io.IOException;
import java.io.Writer;
import java.util.Date;

public class JsonHelper {

    public static String toJson(Object obj) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper.writeValueAsString(obj);
    }

    public static void toJson(Object obj, Writer writer) throws JsonProcessingException, IOException {
        String text = toJson(obj);
        writer.write(text);
    }

    // Convert a JSON string to an object of the specified type
    public static <T> T fromJson(String json, Class<T> valueType) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule()); // Support for Java 8 Time API
        return mapper.readValue(json, valueType);
    }

    private static final class bean {
        private Date now = new Date();

        public Date getNow() {
            return now;
        }

        public void setNow(Date now) {
            this.now = now;
        }
    }


    public static void main(String[] args) throws JsonProcessingException {
        System.out.println(toJson(new bean()));
    }

}
