package org.safehaus.uspto.dtd;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.mongodb.BasicDBObject;

public class NplCitation implements Converter{

	private static final String title = "NplCitation";
	
	protected Logger logger;
	
	private String number;
	private OtherCit otherCitation;
	
	public NplCitation(Logger logger) {
		this.logger = logger;
	}

	public NplCitation(Element element, Logger logger)
	{
		this.logger = logger;
		
		NamedNodeMap nodemap = element.getAttributes();
		for (int i=0; i < nodemap.getLength(); i++)
		{
			Node childNode = nodemap.item(i);
			
			if (childNode.getNodeType() == Node.ATTRIBUTE_NODE) {
				Attr attribute = (Attr) childNode;
				if (attribute.getNodeName().equals("num")) {
					number = attribute.getNodeValue();
				}
				else
				{
					logger.warn("Unknown Attribute {} in {} node", attribute.getNodeName(), title);
				}
			}
		}

		NodeList nodeList = element.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element referenceElement = (Element) node;
				if (referenceElement.getNodeName().equals("othercit"))
				{
					otherCitation = new OtherCit(referenceElement, logger);
				}
				else
				{
					logger.warn("Unknown Element {} in {} node", referenceElement.getNodeName(), title);
				}
			}
			else if (node.getNodeType() == Node.TEXT_NODE) {
				//ignore
			}
			else if (node.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE) {
				//ignore
			}
			else
			{
				logger.warn("Unknown Node {} in {} node", node.getNodeName(), title);
			}
		}

	}

	public String getNumber() {
		return number;
	}
	
	public OtherCit getOtherCit() {
		return otherCitation;
	}

	@Override
	public String toString() {
		StringBuffer toStringBuffer = new StringBuffer(title+":");
		if (number != null)
		{
			toStringBuffer.append(" num: ");
			toStringBuffer.append(number);
		}
		if (otherCitation != null)
		{
			toStringBuffer.append(" ");
			toStringBuffer.append(otherCitation);
		}
		return toStringBuffer.toString();
	}
	
	public JSONObject toJSon() {
		JSONObject jsonObject = new JSONObject();
		if (number != null)
		{
			jsonObject.put("Num", number);
		}
		if (otherCitation != null)
		{
			jsonObject.put(otherCitation.getTitle(), otherCitation.toJSon());
		}
		return jsonObject;
	}

	public BasicDBObject toBasicDBObject() {
		BasicDBObject basicDBObject = new BasicDBObject();
		if (number != null)
		{
			basicDBObject.put("Num", number);
		}
		if (otherCitation != null)
		{
			basicDBObject.put(otherCitation.getTitle(), otherCitation.toBasicDBObject());
		}
		return basicDBObject;
	}
	
	public String getTitle() {
		return title;
	}
}
