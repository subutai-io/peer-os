package org.safehaus.uspto.dtd;

import java.util.List;

import org.jdom2.Content;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.mongodb.BasicDBObject;

public class DocumentId implements Converter{

	private static final String title = "DocumentId";
	
	protected Logger logger;
	
	public String country;
	public String documentNumber;
	public String kind;
	public String name;
	public String date;

	public DocumentId(Logger logger) {
		this.logger = logger;
	}

	public DocumentId(Element element, Logger logger)
	{
		this.logger = logger;
		
		NodeList nodeList = element.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element childElement = (Element) node;
				if (childElement.getNodeName().equals("country")) {
					country = childElement.getTextContent();
				} else if (childElement.getNodeName().equals("doc-number")) {
					documentNumber = childElement.getTextContent();
				} else if (childElement.getNodeName().equals("kind")) {
					kind = childElement.getTextContent();
				} else if (childElement.getNodeName().equals("name")) {
					name = childElement.getTextContent();
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

	public DocumentId(org.jdom2.Element element, Logger logger)
	{
		this.logger = logger;
		
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
				} else if (childElement.getName().equals("kind")) {
					kind = childElement.getValue();
				} else if (childElement.getName().equals("name")) {
					name = childElement.getValue();
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

	public String getCountry() {
		return country;
	}

	public String getDocumentNumber() {
		return documentNumber;
	}

	public String getKind() {
		return kind;
	}

	public String getName() {
		return name;
	}

	public String getDate() {
		return date;
	}

	@Override
	public String toString() {
		StringBuffer toStringBuffer = new StringBuffer(title+":");
		if (documentNumber != null)
		{
			toStringBuffer.append(" No: ");
			toStringBuffer.append(documentNumber);
		}
		if (date != null)
		{
			toStringBuffer.append(" Date: ");
			toStringBuffer.append(date);
		}
		if (country != null)
		{
			toStringBuffer.append(" Country: ");
			toStringBuffer.append(country);
		}
		if (name != null)
		{
			toStringBuffer.append(" Name: ");
			toStringBuffer.append(name);
		}
		if (kind != null)
		{
			toStringBuffer.append(" Kind: ");
			toStringBuffer.append(kind);
		}
		return toStringBuffer.toString();
	}

	public JSONObject toJSon() {
		JSONObject jsonObject = new JSONObject();
		if (documentNumber != null)
		{
			jsonObject.put("No", documentNumber);
		}
		if (date != null)
		{
			jsonObject.put("Date", date);
		}
		if (country != null)
		{
			jsonObject.put("Country", country);
		}
		if (name != null)
		{
			jsonObject.put("Name", name);
		}
		if (kind != null)
		{
			jsonObject.put("Kind", kind);
		}
		return jsonObject;
	}

	public BasicDBObject toBasicDBObject() {
		BasicDBObject basicDBObject = new BasicDBObject();
		if (documentNumber != null)
		{
			basicDBObject.put("No", documentNumber);
		}
		if (date != null)
		{
			basicDBObject.put("Date", date);
		}
		if (country != null)
		{
			basicDBObject.put("Country", country);
		}
		if (name != null)
		{
			basicDBObject.put("Name", name);
		}
		if (kind != null)
		{
			basicDBObject.put("Kind", kind);
		}
		return basicDBObject;
	}
	
	public String getTitle() {
		return title;
	}
	
}
