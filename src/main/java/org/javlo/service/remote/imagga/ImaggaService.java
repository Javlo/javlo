package org.javlo.service.remote.imagga;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.image.ImageEngine;
import org.javlo.utils.JSONMap;
import org.javlo.utils.NeverEmptyMap;

public class ImaggaService {

	private static final int MAX_IMAGE_SIZE = 1024;

	private static Logger logger = Logger.getLogger(ImaggaService.class.getName());

	public static Map<String, List<String>> getImageTags(ImaggaConfig config, File file, Collection<String> langs) throws IOException {
		return getImageTags(config.getApiKey(), config.getApiSecret(), file, langs);
	}

	public static Map<String, List<String>> getImageTags(String apiKey, String apiSecret, File file, Collection<String> langs) throws IOException {
		if (!file.exists()) {
			logger.warning("file not found : " + file);
			return null;
		}

		if (!StringHelper.isImage(file.getName())) {
			logger.warning("not image file : " + file);
			return null;
		}
		
		if (file.length() == 0) {
			logger.warning("no content in file : " + file);
			return null;
		}

		InputStream inputStream;
		if (StringHelper.getFileExtension(file.getName()).equalsIgnoreCase("webp") || file.length() > 1024 * 200) {
			BufferedImage image = ImageIO.read(file);
			BufferedImage finalImage = null;
			
			boolean resize = false;
			if (image.getWidth() > MAX_IMAGE_SIZE) {
				finalImage = ImageEngine.resizeWidth(image, MAX_IMAGE_SIZE, true);
				resize = true;
			}
			if (image.getHeight() > MAX_IMAGE_SIZE) {
				finalImage = ImageEngine.resizeHeight(image, MAX_IMAGE_SIZE, true);
				resize = true;
			}
			
			if (resize) {
				image.flush();
				image = finalImage;
				System.gc();
			}
			
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ImageEngine.storeImage(image, "jpeg", out);
			
			inputStream = new ByteArrayInputStream(out.toByteArray());
			logger.info("convert to jpg [resize:"+resize+" #:"+StringHelper.renderSize(out.toByteArray().length)+"] : "+file);
		} else {
			logger.info("no convert to jpg [#:"+StringHelper.renderSize(file.length())+"] : "+file);
			inputStream = new FileInputStream(file);
		}

		HttpURLConnection connection = null;
		try {

			String credentialsToEncode = apiKey + ":" + apiSecret;
			String basicAuth = Base64.getEncoder().encodeToString(credentialsToEncode.getBytes(StandardCharsets.UTF_8));

			String endpoint = "/tags";
			endpoint += "?limit=10";
			if (langs != null && langs.size() > 0) {
				endpoint += "&language=" + StringHelper.collectionToString(langs, ",");
			}

			String crlf = "\r\n";
			String twoHyphens = "--";
			String boundary = "Image Upload";

			URL urlObject = new URL("https://api.imagga.com/v2" + endpoint);
			connection = (HttpURLConnection) urlObject.openConnection();
			connection.setRequestProperty("Authorization", "Basic " + basicAuth);
			connection.setUseCaches(false);
			connection.setDoOutput(true);

			connection.setRequestMethod("POST");
			connection.setRequestProperty("Connection", "Keep-Alive");
			connection.setRequestProperty("Cache-Control", "no-cache");
			connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

			DataOutputStream request = new DataOutputStream(connection.getOutputStream());

			request.writeBytes(twoHyphens + boundary + crlf);
			request.writeBytes("Content-Disposition: form-data; name=\"image\";filename=\"" + file.getName() + "\"" + crlf);
			request.writeBytes(crlf);

			int bytesRead;
			byte[] dataBuffer = new byte[1024];
			while ((bytesRead = inputStream.read(dataBuffer)) != -1) {
				request.write(dataBuffer, 0, bytesRead);
			}

			request.writeBytes(crlf);
			request.writeBytes(twoHyphens + boundary + twoHyphens + crlf);
			request.flush();
			request.close();

			InputStream responseStream = new BufferedInputStream(connection.getInputStream());

			BufferedReader responseStreamReader = new BufferedReader(new InputStreamReader(responseStream, "UTF-8"));

			String line = "";
			StringBuilder stringBuilder = new StringBuilder();

			while ((line = responseStreamReader.readLine()) != null) {
				stringBuilder.append(line).append("\n");
			}
			responseStreamReader.close();

			Map<String, List<String>> out = new NeverEmptyMap<>(LinkedList.class);
			String response = stringBuilder.toString();
			JSONMap map = JSONMap.parseMap(response);
			int countTags = ((Collection) map.getItem("result.tags")).size();
			for (int i = 0; i < countTags; i++) {
				int confidence = (int) Math.round(Double.parseDouble("" + map.getItem("result.tags." + i + ".confidence")));
				for (String lang : langs) {
					String tag = "" + map.getItem("result.tags." + i + ".tag." + lang);
					out.get(lang).add(tag);
				}
			}
			return out;

		} finally {
			ResourceHelper.closeResource(inputStream);
			if (connection != null) {
				connection.disconnect();
			}
		}

	}

	public static Point getImageFacesPoint(ImaggaConfig config, File file) throws IOException {
		return getImageFacesPoint(config.getApiKey(), config.getApiSecret(), file);
	}

	private static Point getImageFacesPoint(String apiKey, String apiSecret, File file) throws IOException {

		if (!file.exists()) {
			logger.warning("file not found : " + file);
			return null;
		}

		if (!StringHelper.isImage(file.getName())) {
			logger.warning("not image file : " + file);
			return null;
		}

		InputStream inputStream;
		if (StringHelper.getFileExtension(file.getName()).equalsIgnoreCase("webp") || StringHelper.getFileExtension(file.getName()).equalsIgnoreCase("png")) {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			BufferedImage image = ImageIO.read(file);
			ImageIO.write(image, "jpg", out);
			inputStream = new ByteArrayInputStream(out.toByteArray());
		} else {
			inputStream = new FileInputStream(file);
		}

		String credentialsToEncode = apiKey + ":" + apiSecret;
		String basicAuth = Base64.getEncoder().encodeToString(credentialsToEncode.getBytes(StandardCharsets.UTF_8));

		String endpoint = "/faces/detections";

		String crlf = "\r\n";
		String twoHyphens = "--";
		String boundary = "Image Upload";

		URL urlObject = new URL("https://api.imagga.com/v2" + endpoint);
		HttpURLConnection connection = (HttpURLConnection) urlObject.openConnection();
		try {
			connection.setRequestProperty("Authorization", "Basic " + basicAuth);
			connection.setUseCaches(false);
			connection.setDoOutput(true);

			connection.setRequestMethod("POST");
			connection.setRequestProperty("Connection", "Keep-Alive");
			connection.setRequestProperty("Cache-Control", "no-cache");
			connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

			DataOutputStream request = new DataOutputStream(connection.getOutputStream());

			request.writeBytes(twoHyphens + boundary + crlf);
			request.writeBytes("Content-Disposition: form-data; name=\"image\";filename=\"" + file.getName() + "\"" + crlf);
			request.writeBytes(crlf);

			int bytesRead;
			byte[] dataBuffer = new byte[1024];
			while ((bytesRead = inputStream.read(dataBuffer)) != -1) {
				request.write(dataBuffer, 0, bytesRead);
			}

			request.writeBytes(crlf);
			request.writeBytes(twoHyphens + boundary + twoHyphens + crlf);
			request.flush();
			request.close();

			try (InputStream responseStream = new BufferedInputStream(connection.getInputStream())) {
				BufferedReader responseStreamReader = new BufferedReader(new InputStreamReader(responseStream));
				String line = "";
				StringBuilder stringBuilder = new StringBuilder();
				while ((line = responseStreamReader.readLine()) != null) {
					stringBuilder.append(line).append("\n");
				}
				responseStreamReader.close();
				String jsonResult = stringBuilder.toString();
				JSONMap map = JSONMap.parseMap(jsonResult);
				int countFaces = ((Collection) map.getItem("result.faces")).size();
				if (countFaces == 0) {
					return null;
				}
				int x = 0;
				int y = 0;
				for (int i = 0; i < countFaces; i++) {
					int x1 = (int) Math.round(Double.parseDouble("" + map.getItem("result.faces." + i + ".coordinates.xmin")));
					int x2 = (int) Math.round(Double.parseDouble("" + map.getItem("result.faces." + i + ".coordinates.xmax")));
					int y1 = (int) Math.round(Double.parseDouble("" + map.getItem("result.faces." + i + ".coordinates.ymin")));
					int y2 = (int) Math.round(Double.parseDouble("" + map.getItem("result.faces." + i + ".coordinates.ymax")));
					x += (x1 + (x2 - x1) / 2);
					y += (y1 + (y2 - y1) / 2);
				}
				x = x / countFaces;
				y = y / countFaces;
				return new Point(x, y);
			}
		} finally {
			ResourceHelper.closeResource(inputStream);
			if (connection != null) {
				connection.disconnect();
			}
		}
	}

	public static void main(String[] args) throws IOException {
		File testFile = new File("C:\\trans\\wedding7.jpg");
		Map<String, List<String>> tags = getImageTags("acc_2aed156efa150ae", "bb6abc81ec9d18e6c377002144b305b6", testFile, Arrays.asList(new String[] { "fr", "en", "nl" }));

		for (String lg : tags.keySet()) {
			System.out.println(lg + " = " + StringHelper.collectionToString(tags.get(lg)));
		}

		String jsonResultCrop = "{\"result\":{\"croppings\":[{\"target_height\":629,\"target_width\":325,\"x1\":222,\"x2\":546,\"y1\":74,\"y2\":702}]},\"status\":{\"text\":\"\",\"type\":\"success\"}}";

		// JSONMap map = JSONMap.parseMap(jsonResultCrop);
		// int x1 = (int)
		// Math.round(Double.parseDouble(""+map.getItem("result.croppings.0.x1")));
		// int x2 = (int)
		// Math.round(Double.parseDouble(""+map.getItem("result.croppings.0.x2")));
		// int y1 = (int)
		// Math.round(Double.parseDouble(""+map.getItem("result.croppings.0.y1")));
		// int y2 = (int)
		// Math.round(Double.parseDouble(""+map.getItem("result.croppings.0.y2")));
		//
		// int x = (x2 - x1) / 2;
		// int y = (y2 - y1) / 2;
		// System.out.println("" + x + "," + y);

		// String json1face =
		// "{\"result\":{\"faces\":[{\"attributes\":[],\"confidence\":99.9988708496094,\"coordinates\":{\"height\":98,\"width\":98,\"xmax\":415,\"xmin\":317,\"ymax\":214,\"ymin\":116},\"face_id\":\"\",\"landmarks\":[]}]},\"status\":{\"text\":\"\",\"type\":\"success\"}}";
		//
		// String json2face =
		// "{\"result\":{\"faces\":[{\"attributes\":[],\"confidence\":99.9244232177734,\"coordinates\":{\"height\":204,\"width\":204,\"xmax\":1603,\"xmin\":1399,\"ymax\":865,\"ymin\":661},\"face_id\":\"\",\"landmarks\":[]},{\"attributes\":[],\"confidence\":99.746955871582,\"coordinates\":{\"height\":190,\"width\":190,\"xmax\":1508,\"xmin\":1318,\"ymax\":358,\"ymin\":168},\"face_id\":\"\",\"landmarks\":[]}]},\"status\":{\"text\":\"\",\"type\":\"success\"}}";
		//
		// String json5faces =
		// "{\"result\":{\"faces\":[{\"attributes\":[],\"confidence\":99.9927062988281,\"coordinates\":{\"height\":116,\"width\":116,\"xmax\":1144,\"xmin\":1028,\"ymax\":177,\"ymin\":61},\"face_id\":\"\",\"landmarks\":[]},{\"attributes\":[],\"confidence\":99.9924163818359,\"coordinates\":{\"height\":126,\"width\":126,\"xmax\":919,\"xmin\":793,\"ymax\":132,\"ymin\":6},\"face_id\":\"\",\"landmarks\":[]},{\"attributes\":[],\"confidence\":99.9906387329102,\"coordinates\":{\"height\":115,\"width\":115,\"xmax\":205,\"xmin\":90,\"ymax\":194,\"ymin\":79},\"face_id\":\"\",\"landmarks\":[]},{\"attributes\":[],\"confidence\":99.9870910644531,\"coordinates\":{\"height\":108,\"width\":108,\"xmax\":449,\"xmin\":341,\"ymax\":187,\"ymin\":79},\"face_id\":\"\",\"landmarks\":[]},{\"attributes\":[],\"confidence\":99.6327438354492,\"coordinates\":{\"height\":126,\"width\":126,\"xmax\":671,\"xmin\":545,\"ymax\":140,\"ymin\":14},\"face_id\":\"\",\"landmarks\":[]}]},\"status\":{\"text\":\"\",\"type\":\"success\"}}";
		//
		// map = JSONMap.parseMap(json5faces);
		// int countFaces = ((Collection) map.getItem("result.faces")).size();
		//
		// int x = 0;
		// int y = 0;
		// for (int i = 0; i < countFaces; i++) {
		// System.out.println(">>>>>>>>> ImaggaService.main : i=" + i); // TODO: remove
		// debug trace
		// int x1 = (int) Math.round(Double.parseDouble("" + map.getItem("result.faces."
		// + i + ".coordinates.xmin")));
		// int x2 = (int) Math.round(Double.parseDouble("" + map.getItem("result.faces."
		// + i + ".coordinates.xmax")));
		// int y1 = (int) Math.round(Double.parseDouble("" + map.getItem("result.faces."
		// + i + ".coordinates.ymin")));
		// int y2 = (int) Math.round(Double.parseDouble("" + map.getItem("result.faces."
		// + i + ".coordinates.ymax")));
		// System.out.println("" + x1 + "," + y1);
		// System.out.println("" + x2 + "," + y2);
		// x += (x1 + (x2 - x1) / 2);
		// y += (y1 + (y2 - y1) / 2);
		// }
		//
		// x = x / countFaces;
		// y = y / countFaces;
		//
		// System.out.println("x = " + x);
		// System.out.println("y = " + y);

		// String jsonTag =
		// "{\"result\":{\"tags\":[{\"confidence\":100,\"tag\":{\"fr\":\"Bikini\"}},{\"confidence\":100,\"tag\":{\"fr\":\"maillot
		// de
		// bain\"}},{\"confidence\":87.991943359375,\"tag\":{\"fr\":\"vêtement\"}},{\"confidence\":80.7298355102539,\"tag\":{\"fr\":\"vêtements\"}},{\"confidence\":50.9351043701172,\"tag\":{\"fr\":\"plage\"}}]},\"status\":{\"text\":\"\",\"type\":\"success\"}}";
		//
		// JSONMap map = JSONMap.parseMap(jsonTag);
		// int countTags = ((Collection) map.getItem("result.tags")).size();
		// System.out.println("#tags = "+countTags);
		// for (int i = 0; i < countTags; i++) {
		// System.out.println(">>>>>>>>> ImaggaService.main : i=" + i); // TODO: remove
		// debug trace
		// int confidence = (int) Math.round(Double.parseDouble("" +
		// map.getItem("result.tags." + i + ".confidence")));
		// String tag = ""+map.getItem("result.tags." + i + ".tag.fr");
		// System.out.println("tag="+tag+" confidence:"+confidence);
		// }
	}
}
