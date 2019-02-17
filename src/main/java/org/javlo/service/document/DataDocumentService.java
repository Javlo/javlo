package org.javlo.service.document;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.javlo.context.GlobalContext;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;

public class DataDocumentService {
	
	private File docFolder = null;
	
	private static final String KEY = DataDocumentService.class.getName();
	
	public static final DataDocumentService getInstance(GlobalContext globalContext) {
		
		/** test **/
		if (globalContext == null) {
			DataDocumentService outService = new DataDocumentService();
			outService.docFolder = new File("\\tmp\\data-doc");
			return outService;
		}
		
		DataDocumentService outService = (DataDocumentService)globalContext.getAttribute(KEY);
		if (outService == null) {
			outService = new DataDocumentService();
			outService.docFolder = new File(URLHelper.mergePath(globalContext.getStaticFolder(), "data-document"));
			globalContext.setAttribute(KEY, outService);
		}
		return outService;
	}
	
	private synchronized long getNextId(String category) throws NumberFormatException, FileNotFoundException, IOException {
		File file = new File(URLHelper.mergePath(docFolder.getAbsolutePath(), StringHelper.createFileName(category), "id.txt"));
		long id;
		if (!file.exists()) {
			id = 1;
		} else {
			id = Long.parseLong(ResourceHelper.loadStringFromFile(file).trim())+1;
		}
		ResourceHelper.writeStringToFile(file,  ""+id);
		return id;
	}
	
	public Map<Object,Object> updateDocumentData(DataDocument doc) throws IOException {
		File file = new File(URLHelper.mergePath(docFolder.getAbsolutePath(), StringHelper.createFileName(doc.getCategory()), ""+doc.getId()+".properties"));
		Properties properties;
		if (file.exists()) {
			properties = ResourceHelper.loadProperties(file);
		} else {
			properties = new Properties();
		}
		properties.putAll(doc.getData());
		ResourceHelper.storeProperties(properties, file);
		return properties;
	}
	
	/**
	 * create new document 
	 * @param category
	 * @param data
	 * @return return a new id
	 * @throws IOException
	 */
	public DataDocument createDocumentData(DataDocument doc) throws IOException {
		long id = getNextId(doc.getCategory());
		doc.setId(id);
		updateDocumentData(doc);
		return doc;
	}
	
	public DataDocument getDocumentData(String category, long id, String token) throws IOException {
		File file = new File(URLHelper.mergePath(docFolder.getAbsolutePath(), StringHelper.createFileName(category), ""+id+".properties"));
		Properties properties;
		if (file.exists()) {
			properties = ResourceHelper.loadProperties(file);
		} else {
			properties = new Properties();
		}
		Map<String, String> data = new HashMap<>();
		for (Map.Entry<Object, Object> entry : properties.entrySet()) {
			data.put(""+entry.getKey(), ""+entry.getValue());
		}
		DataDocument out = new DataDocument(category,  id, data);
		if (out.getToken() != null && !out.getToken().equals(token)) {
			return null;
		} else {
			return out;
		}
	}

}
