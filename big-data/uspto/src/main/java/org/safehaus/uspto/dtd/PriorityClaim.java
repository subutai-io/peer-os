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

public class PriorityClaim implements Converter{

	private static final String title = "PriorityClaim";
	
	protected Logger logger;
	
	public String sequence;
	public String kind;
	public String country;
	public String documentNumber;
	public String date;

	public PriorityClaim(Logger logger) {
		this.logger = logger;
	}

	public PriorityClaim(Element element, Logger logger)
	{
		this.logger = logger;
		
		NamedNodeMap nodemap = element.getAttributes();
		for (int i=0; i < nodemap.getLength(); i++)
		{
			Node childNode = nodemap.item(i);
			
			if (childNode.getNodeType() == Node.ATTRIBUTE_NODE) {
				Attr attribute = (Attr) childNode;
				if (attribute.getNodeName().equals("sequence")) {
					sequence = attribute.getNodeValue();
				}
				else if (attribute.getNodeName().equals("kind")) {
					kind = attribute.getNodeValue();
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
				if (childElement.getNodeName().equals("country")) {
					country = childElement.getTextContent();
				} else if (childElement.getNodeName().equals("doc-number")) {
					documentNumber = childElement.getTextContent();
				} else if (childElement.getNodeName().equals("date")) {
					date = childElement.getTextContent();
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

	public PriorityClaim(org.jdom2.Element element, Logger logger)
	{
		this.logger = logger;
		
		List<Attribute> attributes = element.getAttributes();
		for (int i=0; i < attributes.size(); i++)
		{
			Attribute attribute = attributes.get(i);
			if (attribute.getName().equals("sequence")) {
				sequence = attribute.getValue();
			}
			else if (attribute.getName().equals("kind")) {
				kind = attribute.getValue();
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
				if (childElement.getName().equals("country")) {
					country = childElement.getValue();
				} else if (childElement.getName().equals("doc-number")) {
					documentNumber = childElement.getValue();
				} else if (childElement.getName().equals("date")) {
					date = childElement.getValue();
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


	public String getSequence() {
		return sequence;
	}

	public String getKind() {
		return kind;
	}

	public String getCountry() {
		return country;
	}

	public String getDocumentNumber() {
		return documentNumber;
	}

	public String getDate() {
		return date;
	}
	
	@Override
	public String toString() {
		StringBuffer toStringBuffer = new StringBuffer(title+":");
		if (sequence != null)
		{
			toStringBuffer.append(" Sequence: ");
			toStringBuffer.append(sequence);
		}
		if (kind != null)
		{
			toStringBuffer.append(" Kind: ");
			toStringBuffer.append(kind);
		}
		if (country != null)
		{
			toStringBuffer.append(" Country: ");
			toStringBuffer.append(country);
		}
		if (documentNumber != null)
		{
			toStringBuffer.append(" DocNumber: ");
			toStringBuffer.append(documentNumber);
		}
		if (date != null)
		{
			toStringBuffer.append(" Date: ");
			toStringBuffer.append(date);
		}
		return toStringBuffer.toString();
	}

	public JSONObject toJSon() {
		JSONObject jsonObject = new JSONObject();
		if (sequence != null)
		{
			jsonObject.put("Sequence", sequence);
		}
		if (kind != null)
		{
			jsonObject.put("Kind", kind);
		}
		if (country != null)
		{
			jsonObject.put("Country", country);
		}
		if (documentNumber != null)
		{
			jsonObject.put("DocNumber", documentNumber);
		}
		if (date != null)
		{
			jsonObject.put("Date", date);
		}
		return jsonObject;
	}

	public BasicDBObject toBasicDBObject() {
		BasicDBObject basicDBObject = new BasicDBObject();
		if (sequence != null)
		{
			basicDBObject.put("Sequence", sequence);
		}
		if (kind != null)
		{
			basicDBObject.put("Kind", kind);
		}
		if (country != null)
		{
			basicDBObject.put("Country", country);
		}
		if (documentNumber != null)
		{
			basicDBObject.put("DocNumber", documentNumber);
		}
		if (date != null)
		{
			basicDBObject.put("Date", date);
		}
		return basicDBObject;
	}
	
	public String getTitle() {
		return title;
	}

	
}
