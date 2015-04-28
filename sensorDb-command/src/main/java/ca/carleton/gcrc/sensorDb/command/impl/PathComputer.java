package ca.carleton.gcrc.sensorDb.command.impl;

import java.io.File;
import java.net.URL;

import ca.carleton.gcrc.sensorDb.command.Main;

public class PathComputer {

	/**
	 * Computes the directory where the server resides given a command-line
	 * argument provided by the user. If the argument is not given, then
	 * this method should be called with a null argument.
	 * @param name Path given by user at the command-line to refer to the server
	 * @return Directory where server resides, based on the given argument
	 * */
	static public File computeServerDir(String name) {
		File serverDir = null;
		
		if( null == name ) {
			// Current dir
			serverDir = new File(".");
		} else {
			serverDir = new File(name);
		}
		
		// Force absolute
		if( false == serverDir.isAbsolute() ){
			serverDir = serverDir.getAbsoluteFile();
		}
		
		return serverDir;
	}
	
	/**
	 * Computes the installation directory for the command line tool.
	 * This is done by looking for a known resource in a JAR file that
	 * ships with the command-line tool. When the resource is found, the
	 * location of the associated JAR file is derived. From there, the
	 * root directory of the installation is deduced.
	 * If the command-line tool is used in a development environment, then
	 * the known resource is found either as a file or within a JAR that lives
	 * within the project directory. In that case, return the root
	 * directory of the project.
	 * @return Directory of the command-line installed packaged or the root directory
	 * of the nunaliit2 project. If neither can be computed, return null.
	 * */
	static public File computeInstallDir() {
		File installDir = null;
		
		// Try to find the path of a known resource file
		File knownResourceFile = null;
		{
			URL url = Main.class.getClassLoader().getResource("commandResourceDummy.txt");
			if( null == url ){
				// Nothing we can do since the resource is not found
				
			} else if( "jar".equals( url.getProtocol() ) ) {
				// The tool is called from an "app assembly". Find the
				// parent of the "repo" directory
				String path = url.getPath();
				if( path.startsWith("file:") ) {
					int bangIndex = path.indexOf('!');
					if( bangIndex >= 0 ) {
						String jarFileName = path.substring("file:".length(), bangIndex);
						knownResourceFile = new File(jarFileName);
					}
				}
				
			} else if( "file".equals( url.getProtocol() ) ) {
				knownResourceFile = new File( url.getFile() );
			}
		}
		
		// Try to find the package installation directory. This should be the parent
		// of a sub-directory called "repo". This is the directory where all the
		// JAR files are stored in the command-line tool
		if( null == installDir && null != knownResourceFile ){
			File tempFile = knownResourceFile;
			boolean found = false;
			while( !found && null != tempFile ){
				if( "repo".equals( tempFile.getName() ) ){
					found = true;
					
					// Parent of "repo" is where the command-line tool is installed
					installDir = tempFile.getParentFile();
					
				} else {
					// Go to parent
					tempFile = tempFile.getParentFile();
				}
			}
		}
		
		// If the "repo" directory is not found, then look for the root
		// of the sensorDb project. In a development environment, this is what
		// we use to look for other directories.
		if( null == installDir && null != knownResourceFile ){
			installDir = computeSensorDbDir(knownResourceFile);
		}
		
		return installDir;
	}
	
	/**
	 * Given an installation directory, find the root directory
	 * for the sensorDb project. This makes sense only in the
	 * context that the command-line tool is run from a development
	 * environment.
	 * @param installDir Computed install directory where command-line is run
	 * @return Root directory where sensorDb project is located, or null
	 * if not found.
	 */
	static public File computeSensorDbDir(File installDir) {
		while( null != installDir ){
			// The root of the sensorDb project contains "sensorDb-command"
			boolean commandExists = (new File(installDir, "sensorDb-command")).exists();
			
			if( commandExists ){
				return installDir;
			} else {
				// Go to parent
				installDir = installDir.getParentFile();
			}
		}

		return null;
	}
	
}
