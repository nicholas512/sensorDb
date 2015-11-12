package ca.carleton.gcrc.sensorDb.upload.observations;

import java.io.File;

public class ConversionRequest {

	private File fileToConvert;
	private String originalFileName;
	private int initialOffset = 0;
	private int finalOffset = 0;
	private String importerName;
	private String notes;

	public File getFileToConvert() {
		return fileToConvert;
	}
	public void setFileToConvert(File fileToConvert) {
		this.fileToConvert = fileToConvert;
	}

	public String getOriginalFileName() {
		return originalFileName;
	}
	public void setOriginalFileName(String originalFileName) {
		this.originalFileName = originalFileName;
	}

	public int getInitialOffset() {
		return initialOffset;
	}
	public void setInitialOffset(int initialOffset) {
		this.initialOffset = initialOffset;
	}

	public int getFinalOffset() {
		return finalOffset;
	}
	public void setFinalOffset(int finalOffset) {
		this.finalOffset = finalOffset;
	}

	public String getImporterName() {
		return importerName;
	}
	public void setImporterName(String importerName) {
		this.importerName = importerName;
	}

	public String getNotes() {
		return notes;
	}
	public void setNotes(String notes) {
		this.notes = notes;
	}

	public String toString(){
		if( null == fileToConvert ){
			return "ConversionRequest without a specified file";
		}
		
		return fileToConvert.getName();
	}
}
