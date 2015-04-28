package ca.carleton.gcrc.sensorDb.command;

import java.io.BufferedReader;
import java.io.File;
import java.io.PrintStream;
import java.util.Stack;

import ca.carleton.gcrc.sensorDb.command.impl.PathComputer;

public class CommandCreate implements Command {

	@Override
	public String getCommandString() {
		return "create";
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
		return false;
	}

	@Override
	public void reportHelp(PrintStream ps) {
		ps.println("SensorDb - Creation Command");
		ps.println();
		ps.println("The creation command allows a user to create a new server.");
		ps.println();
		ps.println("Command Syntax:");
		ps.println("  sensorDb [<global-options>] create [<create-options>]");
		ps.println();
		ps.println("Global Options");
		CommandHelp.reportGlobalSettingServerDir(ps);
		ps.println();
		ps.println("Create Options");
		ps.println("  --no-config   Skips the configuration phase");
	}

	@Override
	public void runCommand(
		GlobalSettings gs
		,Stack<String> argumentStack
		) throws Exception {
		
		// Pick up options
		boolean noConfig = false;
		while( false == argumentStack.empty() ){
			String optionName = argumentStack.peek();
			if( "--no-config".equals(optionName) ){
				argumentStack.pop();
				noConfig = true;
			} else {
				break;
			}
		}

		// Figure out where server should be created
		File serverDir = gs.getServerDir();
		if( null == serverDir ){
			// Ask for directory
			BufferedReader reader = gs.getInReader();

			// Prompt user
			gs.getOutStream().print("Enter location where server should be created: ");
			
			// Read answer
			String line = null;
			try {
				line = reader.readLine();
			} catch(Exception e) {
				throw new Exception("Error while reading server directory from user",e);
			}
			
			serverDir = PathComputer.computeServerDir(line);
			gs.setServerDir(serverDir);
		}

		// Check if this is valid
		if( serverDir.exists() ) {
			throw new Exception("Directory or file already exists: "+serverDir.getAbsolutePath());
		}
		File parent = serverDir.getParentFile();
		if( false == parent.exists() ){
			throw new Exception("Parent directory does not exist: "+parent.getAbsolutePath());
		}
		if( false == parent.isDirectory() ){
			throw new Exception("Parent path is not a directory: "+parent.getAbsolutePath());
		}
		
		// Create directory
		try {
			boolean created = serverDir.mkdir();
			if( !created ){
				throw new Exception("Directory not created");
			}
		} catch(Exception e) {
			throw new Exception("Unable to create directory: "+serverDir,e);
		}
		gs.getOutStream().println("Created server directory at: "+serverDir.getAbsolutePath());
		
		// Create media directory
		{
			File mediaDir = new File(serverDir,"media");
			try {
				boolean created = mediaDir.mkdir();
				if( !created ){
					throw new Exception("Directory not created");
				}
			} catch(Exception e) {
				throw new Exception("Unable to create media directory: "+mediaDir,e);
			}
		}
		
		// Perform configuration, unless disabled
		if( false == noConfig ){
			CommandConfig config = new CommandConfig();
			Stack<String> configArgs = new Stack<String>();
			config.runCommand(gs, configArgs);
		}
	}

}
