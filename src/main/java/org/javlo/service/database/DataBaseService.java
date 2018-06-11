package org.javlo.service.database;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.javlo.context.GlobalContext;
import org.javlo.helper.URLHelper;

public class DataBaseService {
	
	private static Logger logger = Logger.getLogger(DataBaseService.class.getName());
	
	private static final String KEY = DataBaseService.class.getName();
	private File dbFolder = null;
	
	public static DataBaseService getInstance(GlobalContext globalContext) {
		DataBaseService outService = null;
		if (globalContext != null) {
			outService = (DataBaseService)globalContext.getAttribute(KEY);
		}
		if (outService == null) {
			outService = new DataBaseService();
			if (globalContext != null) {
				outService.dbFolder = new File(URLHelper.mergePath(globalContext.getDataBaseFolder().getAbsolutePath(), "h2"));
			} else {
				File dir = new File("/tmp/test_javlo_h2");
				if (dir.exists()) {
					try {
						FileUtils.deleteDirectory(dir);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				outService.dbFolder = dir;
			}
			if (globalContext != null) {
				globalContext.setAttribute(KEY, outService);
			}
		}
		return outService;
	}
	
	public Connection getConnection (String dbname) throws Exception {
		Connection conn;
		try {
			conn = DriverManager.getConnection("jdbc:h2:"+URLHelper.mergePath(dbFolder.getAbsolutePath(), dbname), "sa", "");
		} catch (Exception e) {
			loadDriver();
			conn = DriverManager.getConnection("jdbc:h2:"+URLHelper.mergePath(dbFolder.getAbsolutePath(), dbname), "sa", "");
			e.printStackTrace();
		}
		return conn;
	}
	
	public void releaseConnection(Connection conn) throws SQLException {
		conn.close();
	}
	
	private void loadDriver() throws SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		ClassLoader cl = DataBaseService.class.getClassLoader();
		if (cl != null) {
			logger.info("Loading custom class loader for H2 driver: " + cl.toString());
		    Driver driver = (Driver) Class.forName("org.h2.Driver", true, cl).newInstance();
		    logger.info("Loaded H2 driver: " + driver.toString() + " - " + driver.getMinorVersion() + " - " + driver.getMajorVersion());
		    DriverManager.registerDriver(driver);
		} else {
			logger.info("Loading H2 driver.");
		    Class.forName("org.h2.Driver");
		}
	}
	
	public static void main(String[] args) throws Exception {
		Connection conn = DriverManager.getConnection("jdbc:h2:c:/trans/h2", "sa", "");
		Statement st = conn.createStatement();
		st.execute("drop table post");
		st.execute("create table post (id int NOT NULL AUTO_INCREMENT PRIMARY KEY, author varchar(50), text varchar(1000), media varchar(255), parent int REFERENCES post(id), time TIMESTAMP)");
		conn.close();
		System.out.println("done.");
	}

}
