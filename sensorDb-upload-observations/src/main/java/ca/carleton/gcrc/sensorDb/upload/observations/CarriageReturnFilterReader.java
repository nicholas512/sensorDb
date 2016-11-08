package ca.carleton.gcrc.sensorDb.upload.observations;

import java.io.IOException;
import java.io.Reader;

/**
 * This reader converts filters out \r characters
 *
 */
public class CarriageReturnFilterReader extends Reader {

	private Reader in;
	
	public CarriageReturnFilterReader(Reader in){
		this.in = in;
	}
	
	@Override
	public void close() throws IOException {
		this.in.close();
	}

	@Override
	public int read() throws IOException {
		int c = in.read();
		while( '\r' == c ){
			c = in.read();
		}
		return c;
	}

	@Override
	public int read(char[] cbuf, int offset, int count) throws IOException {
		int len = 0;
		
		while( len < count ){
			int c = read();
			if( c < 0 ){
				// Reached end of stream
				if( 0 == len ){
					return -1;
				} else {
					return len;
				}
			}
			cbuf[offset] = (char)c;
			++offset;
			++len;
		}
		
		return len;
	}

}
