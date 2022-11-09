package org.javlo.service.database;

import java.io.File;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.io.FileUtils;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.utils.TimeMap;

public class DataBaseService {
	
	private static BasicDataSource ds = new BasicDataSource();

	private static Logger logger = Logger.getLogger(DataBaseService.class.getName());

	private static final String KEY = DataBaseService.class.getName();
	private File dbFolder = null;
	private String dbUrl = "sa";
	private String dbLogin = "sa";
	private String dbPassword = "";
	private boolean internalDb = true;
	
	private Map<String, BasicDataSource> basicDataSources = new TimeMap<>(60*60*24*30);

	public static DataBaseService getInstance(GlobalContext globalContext) {
		DataBaseService outService = null;
		if (globalContext != null) {
			outService = (DataBaseService) globalContext.getAttribute(KEY);
		}
		if (outService == null) {
			outService = new DataBaseService();
			if (globalContext != null) {
				if (StringHelper.isEmpty(globalContext.getDBURL())) {
					outService.internalDb = true;
					outService.dbFolder = new File(URLHelper.mergePath(globalContext.getDataBaseFolder().getAbsolutePath(), "h2"));
					outService.dbLogin = globalContext.getStaticConfig().getDBInternalLogin();
					outService.dbPassword = globalContext.getStaticConfig().getDBInternalPassword();
				} else {
					outService.internalDb = false;
					outService.dbUrl = globalContext.getDBURL();
					outService.dbLogin = globalContext.getDBLogin();
					outService.dbPassword = globalContext.getDBPassword();
				}
			} else {
				try {
					File dir = new File("/tmp/test_javlo_h2");
					if (dir.exists()) {
						try {
							FileUtils.deleteDirectory(dir);
						} catch (Throwable e) {
							// e.printStackTrace();
						}
					}
					outService.dbFolder = dir;
				} catch (Throwable t) {

				}
			}
			if (globalContext != null) {
				globalContext.setAttribute(KEY, outService);
			}
		}
		return outService;
	}
	
	public static String getDefaultDbName(ContentContext ctx) {
		return "javlo_"+ctx.getGlobalContext().getContextKey();
	}
	
	private BasicDataSource getDataSource(String dbname) {
		BasicDataSource out = basicDataSources.get(dbname);
		if (out == null) {
			synchronized(this) {
				out = basicDataSources.get(dbname);
				if (out == null) {
					BasicDataSource ds = new BasicDataSource();
					String url = dbUrl.replace("#DB#", dbname);
					ds.setUrl(url);
					ds.setUsername(dbLogin);
					ds.setPassword(dbPassword);
					ds.setMinIdle(2);
					ds.setMaxIdle(5);
					ds.setMaxOpenPreparedStatements(100);
					basicDataSources.put(dbname, ds);
					out = ds;
				}
			}
		}
		return out;
	}

	public Connection getConnection(String dbname) throws Exception {
		if (internalDb) {
			return getInternalConnection(dbname);
		} else {
			return getExternalConnection(dbname);
		}
	}

	private Connection getInternalConnection(String dbname) throws Exception {
		Connection conn;
		try {
			conn = DriverManager.getConnection("jdbc:h2:" + URLHelper.mergePath(dbFolder.getAbsolutePath(), dbname), dbLogin, dbPassword);
		} catch (Exception e) {
			loadDriver();
			conn = DriverManager.getConnection("jdbc:h2:" + URLHelper.mergePath(dbFolder.getAbsolutePath(), dbname), dbLogin, dbPassword);
		}
		return conn;
	}

	private Connection getExternalConnection(String dbname) throws Exception {
		try {
			return getDataSource(dbname).getConnection();
		} catch (Exception e) {
			loadDriver();
			return getDataSource(dbname).getConnection();
		}
	}

	public void releaseConnection(Connection conn) throws SQLException {
		conn.close();
	}

	public void releaseConnection(Statement st, Connection conn) throws SQLException {
		if (st != null) {
			try {
				st.close();
			} catch (Throwable t) {
			}
		}
		conn.close();
	}

	private void loadDriver() throws SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		
		String dbDriver = "org.h2.driver";
		if (!internalDb) { 
			if (dbUrl.contains("postgres")) {
				dbDriver = "org.postgresql.Driver";
			}
		}
		
		ClassLoader cl = DataBaseService.class.getClassLoader();
		if (cl != null) {
			logger.info("Loading custom class loader for "+dbDriver+" driver: " + cl.toString());
			Driver driver = (Driver) Class.forName(dbDriver, true, cl).newInstance();
			logger.info("Loaded driver: " + driver.toString() + " - " + driver.getMinorVersion() + " - " + driver.getMajorVersion());
			DriverManager.registerDriver(driver);
		} else {
			logger.info("Loading "+dbDriver+" driver.");
			Class.forName(dbDriver);
		}
	}

//	public static void main(String[] args) throws Exception {
//		Connection conn = DriverManager.getConnection("jdbc:h2:c:/trans/h2", "sa", "");
//		Statement st = conn.createStatement();
//		st.execute("drop table post");
//		st.execute("create table post (id int NOT NULL AUTO_INCREMENT PRIMARY KEY, author varchar(50), text varchar(1000), media varchar(255), parent int REFERENCES post(id), time TIMESTAMP)");
//		conn.close();
//		System.out.println("done.");
//	}
	
	public boolean isInternalDb() {
		return internalDb;
	}
	
	 public static void main (String[] args) {
		 Map<String, String> env = System.getenv();
	        for (String envName : env.keySet()) {
	            System.out.format("%s=%s%n",
	                              envName,
	                              env.get(envName));
	        }
	    }

}
