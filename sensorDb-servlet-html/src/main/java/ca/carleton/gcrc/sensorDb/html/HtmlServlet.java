package ca.carleton.gcrc.sensorDb.html;

import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.util.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class HtmlServlet extends DefaultServlet {

	final protected Logger logger = LoggerFactory.getLogger(this.getClass());

	private String pathPrefix = "/";
	
	public String getPathPrefix() {
		return pathPrefix;
	}

	public void setPathPrefix(String pathPrefix) {
		this.pathPrefix = pathPrefix;
	}

	@Override
	public Resource getResource(String path) {
		String effectivePath = null;
		if( path.length() <= pathPrefix.length() ){
			effectivePath = "index.html";
		} else {
			effectivePath = path.substring(pathPrefix.length());
		}
		
		logger.debug("resource path: "+path+" effective: "+effectivePath);
		
		return Resource.newClassPathResource(effectivePath);
	}

}
