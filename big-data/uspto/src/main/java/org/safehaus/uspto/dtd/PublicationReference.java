package org.safehaus.uspto.dtd;

import org.slf4j.Logger;
import org.w3c.dom.Element;

public class PublicationReference extends BaseReference {

	private static final String title = "PublicationReference";
	
	public PublicationReference(Logger logger) {
		super(logger);
		this.logger = logger;
	}

	public PublicationReference(Element element, Logger logger)
	{
		super(element, BaseReference.ReferenceType.PUBLICATION, logger);
	}

	public PublicationReference(org.jdom2.Element element, Logger logger)
	{
		super(element, BaseReference.ReferenceType.PUBLICATION, logger);
	}

	@Override
	public String toString() {
		StringBuffer toStringBuffer = new StringBuffer(title+":");
		toStringBuffer.append(super.toString());
		return toStringBuffer.toString();
	}
	
	@Override
	public String getTitle() {
		return title;
	}
	
}
