package org.javlo.service.mapdb;

import java.io.File;
import java.util.Map;

import org.javlo.context.GlobalContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.user.MapDbUserFactory;
import org.javlo.user.UserInfo;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

public class MapDBService {

	private static final String KEY = MapDBService.class.getName();

	private File storageDB = null;
	private DB db = null;

	public static MapDBService getInstance(GlobalContext globalContext) {
		MapDBService outService = (MapDBService) globalContext.getAttribute(KEY);
		if (outService == null) {
			outService = new MapDBService();
			outService.storageDB = new File(URLHelper.mergePath(globalContext.getDataBaseFolder().getAbsolutePath(), "maps.db"));
			if (!outService.storageDB.exists()) {
				outService.storageDB.getParentFile().mkdirs();
			}
			outService.db = DBMaker.fileDB(outService.storageDB).checksumHeaderBypass().closeOnJvmShutdown().make();
			globalContext.setAttribute(KEY, outService);
		}
		return outService;
	}

	public static MapDBService getInstance(File file) {
		MapDBService outService = new MapDBService();
		outService.storageDB = file;
		if (!outService.storageDB.exists()) {
			outService.storageDB.getParentFile().mkdirs();
		}
		outService.db = DBMaker.fileDB(outService.storageDB).checksumHeaderBypass().closeOnJvmShutdown().make();
		return outService;
	}

	public Map<char[], String> getDb(String name) {
		return db.hashMap("map", Serializer.CHAR_ARRAY, Serializer.STRING).createOrOpen();
	}

	public Map<char[], String> subDb(String name, String keyPrefix) {
		BTreeMap<char[], String> treeMap = db.treeMap(name, Serializer.CHAR_ARRAY, Serializer.STRING).createOrOpen();
		return treeMap.prefixSubMap(keyPrefix.toCharArray());
	}

	@Override
	protected void finalize() throws Throwable {
//		if (!db.isClosed()) {
//			db.close();
//		}
	}

	public static void main(String[] args) {		
		File dbFile = new File("C:\\Users\\user\\data\\javlo\\data-ctx\\data-sexy\\db\\maps.db");
		Map<char[], String> persons = MapDBService.getInstance(dbFile).getDb(MapDbUserFactory.class.getName());
		
		System.out.println(">>>>>>>>> MapDBService.main : #, = "+StringHelper.stringToArray(",", ",").length); //TODO: remove debug trace
		
		String fieldsValue = persons.get(MapDbUserFactory.FIELDS_KEY);
		for (Map.Entry<char[], String> entry : persons.entrySet()) {
			System.out.println(">>>>>>>>> MapDBService.main : entry.key = "+(entry.getValue().equals(fieldsValue))); //TODO: remove debug trace
		}
		UserInfo userInfo = new UserInfo();
		System.out.println(">>>>>>>>> MapDBService.main : #persons = " + persons.size()); // TODO: remove debug trace		
		System.out.println(">>>>>>>>> MapDBService.main : value = " + StringHelper.stringToArray(persons.get(MapDbUserFactory.FIELDS_KEY), ",").length); // TODO: remove debug trace
		System.out.println(">>>>>>>>> MapDBService.main : #value = " + StringHelper.stringToArray(persons.get("pvandermaesen@noctis.be".toCharArray()), ",").length); // TODO: remove debug trace
		System.out.println(">>>>>>>>> MapDBService.main : value = " + persons.get("pvandermaesen@noctis.be".toCharArray())); // TODO: remove debug trace
		System.out.println(">>>>>>>>> MapDBService.main : #value = " + StringHelper.stringToArray(persons.get("catherine.dirckx@gmail.com".toCharArray()), ",").length); // TODO: remove debug trace		
		System.out.println(">>>>>>>>> MapDBService.main : value = " + persons.get("catherine.dirckx@gmail.com".toCharArray())); // TODO: remove debug trace
		System.out.println(">>>>>>>>> MapDBService.main : #test = " + StringHelper.stringToArray(persons.get("testsize".toCharArray()), ",").length); // TODO: remove debug trace

	}

}

