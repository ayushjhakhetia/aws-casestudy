package com.impetus.database;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/*import org.slf4j.//logger;
import org.slf4j.//loggerFactory;
*/
public class CreateConnection {

//	static final //logger //logger = //loggerFactory.get//logger(CreateConnection.class);

	public static Connection getRemoteConnection() {
		Properties prop = null;
			try {
				Class.forName("com.mysql.cj.jdbc.Driver");
				prop = new CreateConnection().loadProperties();
				String dbName = prop.getProperty("rds.dbname");
				String userName = prop.getProperty("rds.username");
				String password = prop.getProperty("rds.password");
				String hostname = prop.getProperty("rds.hostname");
				String port = prop.getProperty("rds.port");
				String jdbcUrl = "jdbc:mysql://" + hostname + ":" + port + "/" + dbName + "?user=" + userName
						+ "&password=" + password;
				//logger.trace("Getting remote connection with connection string from environment variables.");
				System.out.println("New Jar===============================");
				System.out.println("Getting remote connection with connection string from environment variables."+jdbcUrl);
				Connection con = DriverManager.getConnection(jdbcUrl);
				//logger.info("Remote connection successful.");
				System.out.println("Remote connection successful with jdbc url: " + jdbcUrl);
				return con;
			} catch (ClassNotFoundException e) {
				System.out.println("Found error connection issue");
				//logger.warn(e.toString());
				e.printStackTrace();
			} catch (SQLException e) {
				//logger.warn(e.toString());
				System.out.println("sql exception");
				e.printStackTrace();
			}catch (Exception e) {
				//logger.warn(e.toString());
				System.out.println("exception: "+e.getMessage());
				e.printStackTrace();
			}
			return null;
	}

	public Properties loadProperties() {
		Properties prop = new Properties();
		InputStream input = null;

		try {

			input = getClass().getClassLoader().getResourceAsStream("application.properties");

			// load a properties file
			prop.load(input);
			// get the property value and print it out
			System.out.println("dbname is: " + prop.getProperty("rds.dbname"));
			System.out.println("hostname is: " + prop.getProperty("rds.hostname"));
			System.out.println("username is: " + prop.getProperty("rds.username"));
			System.out.println("password is: " + prop.getProperty("rds.password"));
			System.out.println("port is: " + prop.getProperty("rds.port"));
			return prop;
		} catch (IOException ex) {
			System.out.println("Found error while initializing properties");
			ex.printStackTrace();
			return null;
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
