package org.javlo.utils.gson;

import java.io.IOException;
import java.time.LocalDate;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

public class LocalDateAdapterGson extends TypeAdapter<LocalDate> {
    @Override
    public LocalDate read(final JsonReader jsonReader) throws IOException {
        if (jsonReader.peek() == JsonToken.NULL) {
            jsonReader.nextNull();
            return null;
        } else {
            return LocalDate.parse(jsonReader.nextString());
        }
    }

	@Override
	public void write(com.google.gson.stream.JsonWriter out, LocalDate localDate) throws IOException {
		// TODO Auto-generated method stub
		 if (localDate == null) {
			 out.nullValue();
	        } else {
	        	out.value(localDate.toString());
	        }
	}
}
