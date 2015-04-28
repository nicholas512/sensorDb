package ca.carleton.gcrc.sensorDb.command;

import java.io.BufferedReader;
import java.io.File;
import java.io.PrintStream;
import java.security.SecureRandom;
import java.util.Properties;
import java.util.Stack;

import org.apache.commons.codec.binary.Base64;

import ca.carleton.gcrc.sensorDb.command.impl.RngFactory;

public class CommandConfig implements Command {
	
	@Override
	public String getCommandString() {
		return "config";
	}

	@Override
	public boolean matchesKeyword(String keyword) {
		if( getCommandString().equalsIgnoreCase(keyword) ) {
			return true;
		}
		return false;
	}

	@Override
	public boolean isDeprecated() {
		return false;
	}

	@Override
	public boolean requiresServerDir() {
		return true;
	}

	@Override
	public void reportHelp(PrintStream ps) {
		ps.println("SensorDb - Configuration Command");
		ps.println();
		ps.println("The configuration command allows a user to changes the parameters");
		ps.println("used to access a server.");
		ps.println();
		ps.println("Command Syntax:");
		ps.println("  sensorDb [<global-options>] config");
		ps.println();
		ps.println("Global Options");
		CommandHelp.reportGlobalSettingServerDir(ps);
	}

	@Override
	public void runCommand(
		GlobalSettings gs
		,Stack<String> argumentStack
		) throws Exception {
		
		// Check that server directory exists
		File serverDir = gs.getServerDir();
		
		// Load up properties
		Properties props = getDefaultProperties();
		ServerProperties.readProperties(serverDir, props);
		
		// Create a server key, if one does not exist
		String serverKey = props.getProperty("server.key",null);
		if( null == serverKey ){
			SecureRandom rng = (new RngFactory()).createRng();
			byte[] key = new byte[16];
			rng.nextBytes(key);
			
			serverKey = Base64.encodeBase64String(key);
			props.setProperty("server.key", serverKey);
		}
		
		// Get user to enter properties
		userInputProperties(gs, props);
		
		// Write properties
		ServerProperties.writeProperties(serverDir, props);
	}
	
	private void userInputProperties(GlobalSettings gs, Properties props) throws Exception {
		
		// Servlet port
		{
			String portString = null;
			while( null == portString ) {
				portString = getUserStringInput(gs, "Enter the port for the server", props, "servlet.url.port");
				if( null == portString ){
					gs.getErrStream().println("A service port must be provided");
				} else {
					try {
						int port = Integer.parseInt(portString);
						if( 0 == port || port > 65535 ) {
							portString = null;
						}
					} catch(Exception e){
						portString = null;
					}
					
					if( null == portString ) {
						gs.getErrStream().println("Invalid port. It must be a positive integer up to 65535");
					}
				}
			}
			props.put("servlet.url.port", portString);
		}
	}
	
	private String getUserStringInput(GlobalSettings gs, String prompt, Properties props, String propName) throws Exception {
		String defaultValue = props.getProperty(propName);
		return getUserStringInput(gs, prompt, defaultValue);
	}

	private String getUserStringInput(GlobalSettings gs, String prompt, String defaultValue) throws Exception {
		BufferedReader reader = gs.getInReader();

		// Prompt user
		gs.getOutStream().print(prompt);
		if( null != defaultValue ){
			gs.getOutStream().print(" [");
			gs.getOutStream().print(defaultValue);
			gs.getOutStream().print("]");
		}
		gs.getOutStream().print(": ");
		
		// Read answer
		String line = null;
		try {
			line = reader.readLine();
		} catch(Exception e) {
			throw new Exception("Error while reading configuration information from user",e);
		}
		String answer = null;
		if( null == line ) {
			// End of stream reached
			throw new Exception("End of input stream reached");
		} else {
			line = line.trim();
			if( "".equals(line) ){
				answer = defaultValue;
			} else {
				answer = line;
			}
		}
		
		return answer;
	}

	@SuppressWarnings("unused")
	private boolean getUserBooleanInput(GlobalSettings gs, String prompt, boolean defaultValue) throws Exception {
		BufferedReader reader = gs.getInReader();

		// Read answer
		boolean response = false;
		boolean validResponse = false;
		while( false == validResponse ) {
			// Prompt user
			gs.getOutStream().print(prompt);
			gs.getOutStream().print("(Y/N)");
			if( defaultValue ) {
				gs.getOutStream().print(" [Y]");
			} else {
				gs.getOutStream().print(" [N]");
			}
			gs.getOutStream().print(": ");
			
			String line = null;
			try {
				line = reader.readLine();
			} catch(Exception e) {
				throw new Exception("Error while reading configuration information from user",e);
			}
			if( null == line ) {
				// End of stream reached
				throw new Exception("End of input stream reached");
			} else {
				line = line.trim();
				if( "".equals(line) ){
					response = defaultValue;
					validResponse = true;
				} else {
					// Analyze response
					if( "y".equalsIgnoreCase(line) ) {
						response = true;
						validResponse = true;
					} else if( "yes".equalsIgnoreCase(line) ) {
						response = true;
						validResponse = true;
					} else if( "n".equalsIgnoreCase(line) ) {
						response = false;
						validResponse = true;
					} else if( "no".equalsIgnoreCase(line) ) {
						response = false;
						validResponse = true;
					}
				}
			}
			
			if( !validResponse ){
				gs.getErrStream().println("A valid response must be provided: Y, N or blank to accept previous value.");
			}
		}
		
		return response;
	}

	private Properties getDefaultProperties() {
		Properties props = new Properties();
		
		return props;
	}
}
