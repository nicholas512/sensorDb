package ca.carleton.gcrc.sensorDb.dbapi;

import java.io.StringWriter;

import junit.framework.TestCase;

public class ObservationWriterCsvTest extends TestCase {

	public void testWriteObservation() throws Exception {
		StringWriter sw = new StringWriter();
		
		ObservationWriterCsv csvWriter = new ObservationWriterCsv(sw);

		Observation obs = new Observation();
		obs.setId("abc123");
		obs.setAccuracy(1.0);
		obs.setImportKey("aaa");
		
		csvWriter.write(obs);
		csvWriter.flush();
	}
}
