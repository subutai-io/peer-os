package org.safehaus.uspto.dtd;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.mongodb.BasicDBObject;

public class UsMath implements Converter{

	private static final String title = "UsMath";
	
	protected Logger logger;
	
	private String idrefs;
	private String nbFile;
	private Image image;
	
	public UsMath(Logger logger) {
		this.logger = logger;
	}
	
	public UsMath(Element element, Logger logger)
	{
		this.logger = logger;
		
		NamedNodeMap nodemap = element.getAttributes();
		for (int i=0; i < nodemap.getLength(); i++)
		{
			Node childNode = nodemap.item(i);
			
			if (childNode.getNodeType() == Node.ATTRIBUTE_NODE) {
				Attr attribute = (Attr) childNode;
				if (attribute.getNodeName().equals("idrefs")) {
					idrefs = attribute.getNodeValue();
				}
				else if (attribute.getNodeName().equals("nb-file")) {
					nbFile = attribute.getNodeValue();
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
				Element childElement = (Element) node;
				if (childElement.getNodeName().equals("img")) {
					image = new Image(childElement, logger);
				}
				else
				{
					logger.warn("Unknown Element {} in {} node", childElement.getNodeName(), title);
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

	public String getIdrefs() {
		return idrefs;
	}

	public String getNbFile() {
		return nbFile;
	}

	public Image getImage() {
		return image;
	}
	
	@Override
	public String toString() {
		StringBuffer toStringBuffer = new StringBuffer("UsMath:");
		if (idrefs != null)
		{
			toStringBuffer.append(" Idrefs: ");
			toStringBuffer.append(idrefs);
		}
		if (nbFile != null)
		{
			toStringBuffer.append(" NbFile: ");
			toStringBuffer.append(nbFile);
		}
		if (image != null)
		{
			toStringBuffer.append(" ");
			toStringBuffer.append(image);
		}
		return toStringBuffer.toString();
	}

	public JSONObject toJSon() {
		JSONObject jsonObject = new JSONObject();
		if (idrefs != null)
		{
			jsonObject.put("Idrefs", idrefs);
		}
		if (nbFile != null)
		{
			jsonObject.put("NbFile", nbFile);
		}
		if (image != null)
		{
			jsonObject.put(image.getTitle(), image.toJSon());
		}
		return jsonObject;
	}

	public BasicDBObject toBasicDBObject() {
		BasicDBObject basicDBObject = new BasicDBObject();
		if (idrefs != null)
		{
			basicDBObject.put("Idrefs", idrefs);
		}
		if (nbFile != null)
		{
			basicDBObject.put("NbFile", nbFile);
		}
		if (image != null)
		{
			basicDBObject.put(image.getTitle(), image.toBasicDBObject());
		}
		return basicDBObject;
	}
	
	public String getTitle() {
		return title;
	}

}
