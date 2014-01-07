package org.safehaus.uspto.dtd;

import java.util.List;

import org.jdom2.Attribute;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.mongodb.BasicDBObject;

public class UsIssuedOnContinuedProsecutionApplication implements Converter{

	private static final String title = "UsIssuedOnContinuedProsecutionApplication";
	
	protected Logger logger;
	
	private String grantCpaText;
	
	public UsIssuedOnContinuedProsecutionApplication(Logger logger) {
		this.logger = logger;
	}
	
	public UsIssuedOnContinuedProsecutionApplication(Element element, Logger logger)
	{
		this.logger = logger;
		
		NamedNodeMap nodemap = element.getAttributes();
		for (int i=0; i < nodemap.getLength(); i++)
		{
			Node childNode = nodemap.item(i);
			
			if (childNode.getNodeType() == Node.ATTRIBUTE_NODE) {
				Attr attribute = (Attr) childNode;
				if (attribute.getNodeName().equals("grant-cpa-text")) {
					grantCpaText = attribute.getNodeValue();
				}
				else
				{
					logger.warn("Unknown Attribute {} in {} node", attribute.getNodeName(), title);
				}
			}
		}
	}

	public UsIssuedOnContinuedProsecutionApplication(org.jdom2.Element element, Logger logger)
	{
		this.logger = logger;
		
		List<Attribute> attributes = element.getAttributes();
		for (int i=0; i < attributes.size(); i++)
		{
			Attribute attribute = attributes.get(i);
			if (attribute.getName().equals("grant-cpa-text")) {
				grantCpaText = attribute.getValue();
			}
			else
			{
				logger.warn("Unknown Attribute {} in {} node", attribute.getName(), title);
			}
		}
	}

	public String getGrantCpaText() {
		return grantCpaText;
	}

	@Override
	public String toString() {
		StringBuffer toStringBuffer = new StringBuffer(title+":");
		if (grantCpaText != null)
		{
			toStringBuffer.append(" GrantCpaText: ");
			toStringBuffer.append(grantCpaText);
		}
		return toStringBuffer.toString();
	}

	public JSONObject toJSon() {
		JSONObject jsonObject = new JSONObject();
		if (grantCpaText != null)
		{
			jsonObject.put("GrantCpaText", grantCpaText);
		}
		return jsonObject;
	}

	public BasicDBObject toBasicDBObject() {
		BasicDBObject basicDBObject = new BasicDBObject();
		if (grantCpaText != null)
		{
			basicDBObject.put("GrantCpaText", grantCpaText);
		}
		return basicDBObject;
	}
	
	public String getTitle() {
		return title;
	}

}
