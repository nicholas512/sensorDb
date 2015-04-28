package ca.carleton.gcrc.sensorDb.html;

import javax.servlet.http.HttpServlet;

public class HtmlServletFactory {

	public HttpServlet create(String pathPrefix) {
		HtmlServlet servlet = new HtmlServlet();
		
		servlet.setPathPrefix(pathPrefix);
		
		return servlet;
	}
}
