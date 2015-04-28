package ca.carleton.gcrc.sensorDb.jdbc;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DbConnection {

	final static protected Logger logger = LoggerFactory.getLogger(DbConnection.class);
	
	static public DbConnection fromParameters(
			String connectionString,
			String user,
			String password
			) throws Exception {
		
		Connection con = null;
		try {
		    Class.forName("org.postgresql.Driver"); //load the driver
			con = DriverManager.getConnection(
					"jdbc:postgresql:"+connectionString,
					user,
					password
				); //connect to the db
		    DatabaseMetaData dbmd = con.getMetaData(); //get MetaData to confirm connection
		    logger.info("Connection to "+dbmd.getDatabaseProductName()+" "+
		                       dbmd.getDatabaseProductVersion()+" successful.\n");

		} catch(Exception e) {
			throw new Exception("Couldn't get db connection: "+connectionString,e);
		}
		
		return new DbConnection(con);
	}
	
	private Connection connection;
	
	private DbConnection(Connection connection) {
		this.connection = connection;
	}
	
	public Connection getConnection(){
		return this.connection;
	}
}
