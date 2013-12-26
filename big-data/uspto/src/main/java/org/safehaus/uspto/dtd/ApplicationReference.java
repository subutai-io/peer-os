package org.safehaus.uspto.dtd;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.mongodb.BasicDBObject;

public class ApplicationReference extends BaseReference {

	private static final String title = "ApplicationReference";
	
	protected Logger logger;
	
	private String applicationType;

	public ApplicationReference(Logger logger) {
		super(logger);
		this.logger = logger;
	}

	public ApplicationReference(Element element, Logger logger)
	{
		super(element, BaseReference.ReferenceType.APPLICATION, logger);
		this.logger = logger;

		NamedNodeMap nodemap = element.getAttributes();
		for (int i=0; i < nodemap.getLength(); i++)
		{
			Node childNode = nodemap.item(i);
			if (childNode.getNodeType() == Node.ATTRIBUTE_NODE) {
				Attr attribute = (Attr) childNode;
				if (attribute.getNodeName().equals("appl-type")) {
					applicationType = attribute.getNodeValue();
				}
				else
				{
					logger.warn("Unknown Attribute {} in {} node", attribute.getNodeName(), title);
				}
			}
		}
	}

	public String getApplicationType() {
		return applicationType;
	}

	@Override
	public String toString() {
		StringBuffer toStringBuffer = new StringBuffer(title+":");
		toStringBuffer.append(super.toString());
		if (applicationType != null)
		{
			toStringBuffer.append(" ApplicationType: ");
			toStringBuffer.append(applicationType);
		}
		return toStringBuffer.toString();
	}

	@Override
	public JSONObject toJSon() {
		JSONObject jsonObject = super.toJSon();
		if (jsonObject == null)
		{
			jsonObject = new JSONObject();
		}
		if (applicationType != null)
		{
			jsonObject.put("ApplicationType", applicationType);
		}
		return jsonObject;
	}

	@Override
	public BasicDBObject toBasicDBObject() {
		BasicDBObject basicDBObject = super.toBasicDBObject();
		if (basicDBObject == null)
		{
			basicDBObject = new BasicDBObject();
		}
		if (applicationType != null)
		{
			basicDBObject.put("ApplicationType", applicationType);
		}
		return basicDBObject;
	}
	
	@Override
	public String getTitle() {
		return title;
	}
	
}
