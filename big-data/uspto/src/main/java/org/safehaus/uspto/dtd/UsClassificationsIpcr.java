package org.safehaus.uspto.dtd;

import org.slf4j.Logger;
import org.w3c.dom.Element;

public class UsClassificationsIpcr implements Converter{

	private static final String title = "UsClassificationsIpcr";
	
	protected Logger logger;
	
	private String text;
	
	public UsClassificationsIpcr(Logger logger) {
		this.logger = logger;
	}
	
	public UsClassificationsIpcr(Element element, Logger logger)
	{
		this.logger = logger;
		
		text = element.getTextContent();
	}

	public String getText() {
		return text;
	}

	@Override
	public String toString() {
		StringBuffer toStringBuffer = new StringBuffer(title+":");
		if (text != null)
		{
			toStringBuffer.append(" ");
			toStringBuffer.append(text);
		}
		return toStringBuffer.toString();
	}

	public String toJSon() {
		return text;
	}

	public String toBasicDBObject() {
		return text;
	}

	public String getTitle() {
		return title;
	}

}
