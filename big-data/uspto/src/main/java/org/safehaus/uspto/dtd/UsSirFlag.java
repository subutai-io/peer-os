package org.safehaus.uspto.dtd;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.mongodb.BasicDBObject;

public class UsSirFlag implements Converter{

	private static final String title = "UsSirFlag";
	
	protected Logger logger;
	
	private String sirText;
	
	public UsSirFlag(Logger logger) {
		this.logger = logger;
	}
	
	public UsSirFlag(Element element, Logger logger)
	{
		this.logger = logger;
		
		NamedNodeMap nodemap = element.getAttributes();
		for (int i=0; i < nodemap.getLength(); i++)
		{
			Node childNode = nodemap.item(i);
			
			if (childNode.getNodeType() == Node.ATTRIBUTE_NODE) {
				Attr attribute = (Attr) childNode;
				if (attribute.getNodeName().equals("sir-text")) {
					sirText = attribute.getNodeValue();
				}
				else
				{
					logger.warn("Unknown Attribute {} in {} node", attribute.getNodeName(), title);
				}
			}
		}
	}

	public String getSirText() {
		return sirText;
	}
	
	@Override
	public String toString() {
		StringBuffer toStringBuffer = new StringBuffer(title+":");
		if (sirText != null)
		{
			toStringBuffer.append(" SirText: ");
			toStringBuffer.append(sirText);
		}
		return toStringBuffer.toString();
	}
	
	public JSONObject toJSon() {
		JSONObject jsonObject = new JSONObject();
		if (sirText != null)
		{
			jsonObject.put("SirText", sirText);
		}
		return jsonObject;
	}

	public BasicDBObject toBasicDBObject() {
		BasicDBObject basicDBObject = new BasicDBObject();
		if (sirText != null)
		{
			basicDBObject.put("SirText", sirText);
		}
		return basicDBObject;
	}
	
	public String getTitle() {
		return title;
	}
	
}
