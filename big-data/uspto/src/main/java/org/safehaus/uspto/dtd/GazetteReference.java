package org.safehaus.uspto.dtd;

import java.util.List;

import org.jdom2.Attribute;
import org.jdom2.Content;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.mongodb.BasicDBObject;

public class GazetteReference implements Converter{

	private static final String title = "GazetteReference";
	
	protected Logger logger;
	
	private String id;
	private String country;
	private String lang;
	private String gazetteNum;
	private String date;
	private String text;
	
	public GazetteReference(Logger logger) {
		this.logger = logger;
	}
	
	public GazetteReference(Element element, Logger logger)
	{
		this.logger = logger;
		
		NamedNodeMap nodemap = element.getAttributes();
		for (int i=0; i < nodemap.getLength(); i++)
		{
			Node childNode = nodemap.item(i);
			
			if (childNode.getNodeType() == Node.ATTRIBUTE_NODE) {
				Attr attribute = (Attr) childNode;
				if (attribute.getNodeName().equals("id")) {
					id = attribute.getNodeValue();
				}
				else if (attribute.getNodeName().equals("country")) {
					country = attribute.getNodeValue();
				}
				else if (attribute.getNodeName().equals("lang")) {
					lang = attribute.getNodeValue();
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
				if (childElement.getNodeName().equals("gazette-num")) {
					gazetteNum = childElement.getTextContent();
				}
				else if (childElement.getNodeName().equals("date")) {
					date = childElement.getTextContent();
				}
				else if (childElement.getNodeName().equals("text")) {
					text = childElement.getTextContent();
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

	public GazetteReference(org.jdom2.Element element, Logger logger)
	{
		this.logger = logger;
		
		List<Attribute> attributes = element.getAttributes();
		for (int i=0; i < attributes.size(); i++)
		{
			Attribute attribute = attributes.get(i);
			if (attribute.getName().equals("id")) {
				id = attribute.getValue();
			}
			else if (attribute.getName().equals("country")) {
				country = attribute.getValue();
			}
			else if (attribute.getName().equals("lang")) {
				lang = attribute.getValue();
			}
			else
			{
				logger.warn("Unknown Attribute {} in {} node", attribute.getName(), title);
			}
		}

		List<Content> nodes = element.getContent();
		for (int i=0; i < nodes.size(); i++)
		{
			Content node = nodes.get(i);
			if (node.getCType() == Content.CType.Element) {
				org.jdom2.Element childElement = (org.jdom2.Element) node;
				if (childElement.getName().equals("gazette-num")) {
					gazetteNum = childElement.getValue();
				}
				else if (childElement.getName().equals("date")) {
					date = childElement.getValue();
				}
				else if (childElement.getName().equals("text")) {
					text = childElement.getValue();
				}
				else
				{
					logger.warn("Unknown Element {} in {} node", childElement.getName(), title);
				}
			}
			else if (node.getCType() == Content.CType.Text) {
				//ignore
			}
			else if (node.getCType() == Content.CType.ProcessingInstruction) {
				//ignore
			}
			else
			{
				logger.warn("Unknown Node {} in {} node", node.getCType(), title);
			}
		}

	}


	@Override
	public String toString() {
		StringBuffer toStringBuffer = new StringBuffer(title+":");
		if (id != null)
		{
			toStringBuffer.append(" Id: ");
			toStringBuffer.append(id);
		}
		if (country != null)
		{
			toStringBuffer.append(" Country: ");
			toStringBuffer.append(country);
		}
		if (lang != null)
		{
			toStringBuffer.append(" Language: ");
			toStringBuffer.append(lang);
		}
		if (gazetteNum != null)
		{
			toStringBuffer.append(" GazetteNum: ");
			toStringBuffer.append(gazetteNum);
		}
		if (date != null)
		{
			toStringBuffer.append(" Date: ");
			toStringBuffer.append(date);
		}
		if (text != null)
		{
			toStringBuffer.append(" Text: ");
			toStringBuffer.append(text);
		}
		return toStringBuffer.toString();
	}

	public JSONObject toJSon() {
		JSONObject jsonObject = new JSONObject();
		if (id != null)
		{
			jsonObject.put("Id", id);
		}
		if (country != null)
		{
			jsonObject.put("Country", country);
		}
		if (lang != null)
		{
			jsonObject.put("Language", lang);
		}
		if (gazetteNum != null)
		{
			jsonObject.put("GazetteNum", gazetteNum);
		}
		if (date != null)
		{
			jsonObject.put("Date", date);
		}
		if (text != null)
		{
			jsonObject.put("Text", text);
		}
		return jsonObject;
	}

	public BasicDBObject toBasicDBObject() {
		BasicDBObject basicDBObject = new BasicDBObject();
		if (id != null)
		{
			basicDBObject.put("Id", id);
		}
		if (country != null)
		{
			basicDBObject.put("Country", country);
		}
		if (lang != null)
		{
			basicDBObject.put("Language", lang);
		}
		if (gazetteNum != null)
		{
			basicDBObject.put("GazetteNum", gazetteNum);
		}
		if (date != null)
		{
			basicDBObject.put("Date", date);
		}
		if (text != null)
		{
			basicDBObject.put("Text", text);
		}
		return basicDBObject;
	}
	
	public String getTitle() {
		return title;
	}
	
}
