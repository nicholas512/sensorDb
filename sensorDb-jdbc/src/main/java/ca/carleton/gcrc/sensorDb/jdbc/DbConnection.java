package ca.carleton.gcrc.sensorDb.jdbc;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException; 

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.carleton.gcrc.sensorDb.dbapi.DbAPI;

public class DbConnection {

	final static protected Logger logger = LoggerFactory.getLogger(DbConnection.class);
	private final String connectionString;
	private final String user;
	private final String password;
	
	private Connection connection;

	static public DbConnection fromParameters(
			String connectionString,
			String user,
			String password
			) throws Exception {
		
		Connection con = createNewSqlConnection(connectionString, user, password);
		return new DbConnection(con, connectionString, user, password);
	}

	private DbConnection(Connection connection, String connectionString, String user, String password) {
		this.connection = connection;
		this.connectionString = connectionString;
		this.user = user;
		this.password = password;
	}

	public synchronized Connection getConnection() {
		try {
			// Check if the connection is dead, using a 2-second timeout.
			if (this.connection == null || !this.connection.isValid(2)) {
				logger.warn("Database connection was stale or closed. Reconnecting...");
				this.connection = createNewSqlConnection(this.connectionString, this.user, this.password);
			}
		} catch (Exception e) {
			logger.error("Database connection validation failed. Reconnecting...", e);
			// If isValid() throws an error, the connection is definitely dead.
			try {
				this.connection = createNewSqlConnection(this.connectionString, this.user, this.password);
			} catch (Exception newConnectException) { 
				logger.error("Failed to reconnect to the database. Returning null connection", newConnectException);
				this.connection = null;
				}
		}

		return this.connection;
	}

	private static Connection createNewSqlConnection(String connectionString, String user, String password) throws Exception {
		try {
		    Class.forName("org.postgresql.Driver"); //load the driver
			Connection con = DriverManager.getConnection(
					"jdbc:postgresql:"+connectionString,
					user,
					password
				); //connect to the db
		    DatabaseMetaData dbmd = con.getMetaData(); //get MetaData to confirm connection
		    logger.info("Connection to "+dbmd.getDatabaseProductName()+" "+
		                       dbmd.getDatabaseProductVersion()+" successful.\n");
			return con;
		} catch (Exception e) {
			throw new Exception("Couldn't get db connection: "+connectionString,e);
		}
	}

	public DbAPI getAPI() throws Exception {
		return new DbApiJdbc(this);
	}
}
