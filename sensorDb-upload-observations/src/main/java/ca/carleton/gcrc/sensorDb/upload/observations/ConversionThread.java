package ca.carleton.gcrc.sensorDb.upload.observations;

import java.io.File;
import java.util.List;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConversionThread extends Thread {

	final protected Logger logger = LoggerFactory.getLogger(this.getClass());

	private boolean isShuttingDown = false;
	private String threadName = null;
	private List<File> filesToConvert = new Vector<File>();
	private ObservationFileImporter importer = null;
	
	public ConversionThread(ObservationFileImporter importer){
		this.importer = importer;
	}
	
	public void shutdown() {
		
		logger.info("Shutting down thread "+this.getClass().getName());

		synchronized(this) {
			isShuttingDown = true;
			this.notifyAll();
		}
	}
	
	public void addFileToConvert(File fileToConvert){
		synchronized(this){
			filesToConvert.add(fileToConvert);
			this.notify();
		}
	}
	
	@Override
	public void run() {
		
		long threadId = Thread.currentThread().getId();
		threadName = this.getClass().getSimpleName()+" ["+threadId+"]";
		
		logger.info("Start thread "+threadName);
		
		boolean done = false;
		do {
			synchronized(this) {
				done = isShuttingDown;
			}
			if( false == done ) {
				activity();
			}
		} while( false == done );

		logger.info("Thread exiting "+threadName);
	}
	
	private void activity() {
		// Get work
		File fileToConvert = null;
		synchronized(this){
			if( filesToConvert.size() > 0 ){
				fileToConvert = filesToConvert.remove(0);	
			} else {
				// Wait for work
				try {
					this.wait();
				} catch (Exception e) {
					logger.error("Waiting thread interrupted "+threadName,e);
				}
			}
		}
		
		// Check if we need to convert a file
		if( null != fileToConvert ){
			try {
				logger.info("Start file conversion "+ fileToConvert.getName() +" "+threadName);
				importer.importFile(fileToConvert);
				logger.info("End file conversion "+ fileToConvert.getName() +" "+threadName);
			} catch (Exception e) {
				logger.error("Work error on thread "+threadName,e);
			}
		}
	}
}
