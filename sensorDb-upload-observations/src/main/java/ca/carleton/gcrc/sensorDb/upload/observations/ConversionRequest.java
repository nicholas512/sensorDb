package ca.carleton.gcrc.sensorDb.upload.observations;

import java.io.File;

public class ConversionRequest {

	private File fileToConvert;
	private int initialOffset = 0;
	private int finalOffset = 0;

	public File getFileToConvert() {
		return fileToConvert;
	}
	public void setFileToConvert(File fileToConvert) {
		this.fileToConvert = fileToConvert;
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

	public String toString(){
		if( null == fileToConvert ){
			return "ConversionRequest without a specified file";
		}
		
		return fileToConvert.getName();
	}
}
