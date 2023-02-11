package org.javlo.utils.gson;

import java.io.IOException;
import java.time.LocalDateTime;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

public class LocalDateTimeAdapterGson extends TypeAdapter<LocalDateTime> {
    @Override
    public LocalDateTime read(final JsonReader jsonReader) throws IOException {
        if (jsonReader.peek() == JsonToken.NULL) {
            jsonReader.nextNull();
            return null;
        } else {
            return LocalDateTime.parse(jsonReader.nextString());
        }
    }

	@Override
	public void write(com.google.gson.stream.JsonWriter out, LocalDateTime localDateTime) throws IOException {
		// TODO Auto-generated method stub
		 if (localDateTime == null) {
			 out.nullValue();
	        } else {
	        	out.value(localDateTime.toString());
	        }
	}
}
