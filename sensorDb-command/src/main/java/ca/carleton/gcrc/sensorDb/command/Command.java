package ca.carleton.gcrc.sensorDb.command;

import java.io.PrintStream;
import java.util.Stack;

public interface Command {
	
	boolean requiresServerDir();
	
	String getCommandString();
	
	boolean matchesKeyword(String keyword);
	
	boolean isDeprecated();
	
	void reportHelp(PrintStream ps);
	
	void runCommand(
		GlobalSettings gs
		,Stack<String> argumentStack
		) throws Exception;

}
