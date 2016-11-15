package ca.carleton.gcrc.sensorDb.upload.observations;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.carleton.gcrc.sensorDb.jdbc.DbConnection;
import ca.carleton.gcrc.upload.LoadedFile;
import ca.carleton.gcrc.upload.OnUploadedListener;
import ca.carleton.gcrc.upload.OnUploadedRequiresShutdown;

public class SensorFileUploaded implements OnUploadedListener,OnUploadedRequiresShutdown {

	final protected Logger logger = LoggerFactory.getLogger(this.getClass());

	private ConversionThread conversionThread = null;
	
	public SensorFileUploaded(DbConnection dbConn) throws Exception{
		SensorFileImporter importer = new SensorFileImporter(dbConn);
		this.conversionThread = new ConversionThread(importer);
		this.conversionThread.start();
	}
	
	@Override
	public JSONObject onLoad(
			String progressId, 
			List<LoadedFile> uploadedFiles,
			Map<String, List<String>> parameters, 
			Principal userPrincipal,
			Cookie[] cookies
			) throws Exception {

		// Parse the files
		for(LoadedFile loadedFile : uploadedFiles){
			logger.info("Uploaded file: "+loadedFile.getFile().getAbsolutePath());

			conversionThread.addFileToConvert(loadedFile, parameters);
		}

		JSONObject result = new JSONObject();
		result.put("ok", true);
		return result;
	}

	@Override
	public void onError(
			String progressId, 
			List<LoadedFile> uploadedFiles,
			Map<String,List<String>> parameters, 
			Principal userPrincipal,
			Cookie[] cookies
			) {
		
	}

	@Override
	public void shutdown() {
		if( null != conversionThread ){
			conversionThread.shutdown();
		}
		conversionThread = null;
	}
}
