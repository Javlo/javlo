package org.javlo.visualtesting.helper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JsonHelper {

	public static final String JSON_DATE_TIME_PATTERN = "yyyy/MM/dd-HH:mm:ss.SSS";

	public static final Gson GSON = new GsonBuilder()
			.setDateFormat(JSON_DATE_TIME_PATTERN)
			.setPrettyPrinting()
			.create();

	private static final Charset CS = StandardCharsets.UTF_8;

	public static <T> T load(Path dataFile, Class<T> dataClass) {
		try (BufferedReader read = Files.newBufferedReader(dataFile, CS)) {
			return GSON.fromJson(read, dataClass);
		} catch (IOException e) {
			throw new ForwardException(e);
		}
	}

	public static <T> void save(Path dataFile, T data) {
		try (BufferedWriter write = Files.newBufferedWriter(dataFile, CS,
				StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
			GSON.toJson(data, write);
		} catch (IOException e) {
			throw new ForwardException(e);
		}
	}

}
