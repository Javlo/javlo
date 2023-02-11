package org.javlo.utils.gson;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonFactory {
	
	public static Gson getGson() {
		return new GsonBuilder().setDateFormat("yyyy-MM-dd").registerTypeAdapter(LocalDate.class, new LocalDateAdapterGson()).registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapterGson()).create();
	}

}
