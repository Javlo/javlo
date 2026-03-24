package org.javlo.helper;

import com.fasterxml.jackson.annotation.JsonInclude;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;
import org.javlo.helper.json.JavaTimeModule;

import java.io.IOException;
import java.io.Writer;
import java.util.Date;

public class JsonHelper {

    private static JsonMapper buildMapper() {
        return JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .changeDefaultPropertyInclusion(v -> v.withValueInclusion(JsonInclude.Include.NON_NULL))
                .build();
    }

    public static String toJson(Object obj) throws JacksonException {
        return buildMapper().writeValueAsString(obj);
    }

    public static void toJson(Object obj, Writer writer) throws JacksonException, IOException {
        String text = toJson(obj);
        writer.write(text);
    }

    // Convert a JSON string to an object of the specified type
    public static <T> T fromJson(String json, Class<T> valueType) throws JacksonException {
        return buildMapper().readValue(json, valueType);
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


    public static void main(String[] args) throws JacksonException {
        System.out.println(toJson(new bean()));
    }

}
