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

public class PatentCitation implements Converter{

	private static final String title = "PatentCitation";
	
	protected Logger logger;
	
	private String number;
	private DocumentId documentId;
	
	public PatentCitation(Logger logger) {
		this.logger = logger;
	}

	public PatentCitation(Element element, Logger logger)
	{
		this.logger = logger;
		
		NamedNodeMap nodemap = element.getAttributes();
		for (int i=0; i < nodemap.getLength(); i++)
		{
			Node childNode = nodemap.item(i);
			
			//logger.info("Nodetype: {}", childNode.getNodeType());
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
		for (int j=0; j < nodeList.getLength(); j++)
		{
			Node node = nodeList.item(j);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element referenceElement = (Element) node;
				if (referenceElement.getNodeName().equals("document-id"))
				{
					documentId = new DocumentId(referenceElement, logger);
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

	public PatentCitation(org.jdom2.Element element, Logger logger)
	{
		this.logger = logger;
		
		List<Attribute> attributes = element.getAttributes();
		for (int i=0; i < attributes.size(); i++)
		{
			Attribute attribute = attributes.get(i);
			if (attribute.getName().equals("num")) {
				number = attribute.getValue();
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
				if (childElement.getName().equals("document-id"))
				{
					documentId = new DocumentId(childElement, logger);
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

	public String getNumber() {
		return number;
	}
	
	public DocumentId getDocumentId() {
		return documentId;
	}

	@Override
	public String toString() {
		StringBuffer toStringBuffer = new StringBuffer(title+":");
		if (number != null)
		{
			toStringBuffer.append(" Num: ");
			toStringBuffer.append(number);
		}
		if (documentId != null)
		{
			toStringBuffer.append(" ");
			toStringBuffer.append(documentId);
		}
		toStringBuffer.append(super.toString());
		return toStringBuffer.toString();
	}

	public JSONObject toJSon() {
		JSONObject jsonObject = new JSONObject();
		if (number != null)
		{
			jsonObject.put("Num", number);
		}
		if (documentId != null)
		{
			jsonObject.put(documentId.getTitle(), documentId.toJSon());
		}
		return jsonObject;
	}

	public BasicDBObject toBasicDBObject() {
		BasicDBObject basicDBObject = new BasicDBObject();
		if (number != null)
		{
			basicDBObject.put("Num", number);
		}
		if (documentId != null)
		{
			basicDBObject.put(documentId.getTitle(), documentId.toBasicDBObject());
		}
		return basicDBObject;
	}
	
	public String getTitle() {
		return title;
	}
	
	
}
