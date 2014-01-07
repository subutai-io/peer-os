package org.safehaus.uspto.dtd;

import java.util.List;

import org.jdom2.Content;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.mongodb.BasicDBObject;

public class ParentDocument implements Converter{

	private static final String title = "ParentDocument";
	
	protected Logger logger;
	
	private String parentStatus;
	private DocumentId documentId;
	private ParentGrantDocument parentGrantDocument;
	private ParentPctDocument parentPctDocument;
	
	public ParentDocument(Logger logger) {
		this.logger = logger;
	}

	public ParentDocument(Element element, Logger logger)
	{
		this.logger = logger;
		
		NodeList nodes = element.getChildNodes();

		for (int j=0; j < nodes.getLength(); j++)
		{
			Node node = nodes.item(j);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element childElement = (Element) node;
				if (childElement.getNodeName().equals("document-id"))
				{
					documentId = new DocumentId(childElement, logger);
				}
				else if (childElement.getNodeName().equals("parent-grant-document"))
				{
					parentGrantDocument = new ParentGrantDocument(childElement, logger);
				}
				else if (childElement.getNodeName().equals("parent-pct-document"))
				{
					parentPctDocument = new ParentPctDocument(childElement, logger);
				}
				else if (childElement.getNodeName().equals("parent-status"))
				{
					parentStatus = childElement.getTextContent();
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

	public ParentDocument(org.jdom2.Element element, Logger logger)
	{
		this.logger = logger;
		
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
				else if (childElement.getName().equals("parent-grant-document"))
				{
					parentGrantDocument = new ParentGrantDocument(childElement, logger);
				}
				else if (childElement.getName().equals("parent-pct-document"))
				{
					parentPctDocument = new ParentPctDocument(childElement, logger);
				}
				else if (childElement.getName().equals("parent-status"))
				{
					parentStatus = childElement.getValue();
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

	public String getParentStatus() {
		return parentStatus;
	}
	
	public DocumentId getDocumentId() {
		return documentId;
	}

	public ParentGrantDocument getParentGrantDocument() {
		return parentGrantDocument;
	}
	
	public ParentPctDocument getParentPctDocument() {
		return parentPctDocument;
	}
	
	@Override
	public String toString() {
		StringBuffer toStringBuffer = new StringBuffer(title+":");
		if (documentId != null)
		{
			toStringBuffer.append(" ");
			toStringBuffer.append(documentId);
		}
		if (parentGrantDocument != null)
		{
			toStringBuffer.append(" ");
			toStringBuffer.append(parentGrantDocument);
		}	
		if (parentPctDocument != null)
		{
			toStringBuffer.append(" ");
			toStringBuffer.append(parentPctDocument);
		}	
		if (parentStatus != null)
		{
			toStringBuffer.append(" Status: ");
			toStringBuffer.append(parentStatus);
		}
		return toStringBuffer.toString();
	}
	
	public JSONObject toJSon() {
		JSONObject jsonObject = new JSONObject();
		if (documentId != null)
		{
			jsonObject.put(documentId.getTitle(), documentId.toJSon());
		}
		if (parentGrantDocument != null)
		{
			jsonObject.put(parentGrantDocument.getTitle(), parentGrantDocument.toJSon());
		}
		if (parentPctDocument != null)
		{
			jsonObject.put(parentPctDocument.getTitle(), parentPctDocument.toJSon());
		}
		if (parentStatus != null)
		{
			jsonObject.put("Status", parentStatus);
		}
		return jsonObject;
	}

	public BasicDBObject toBasicDBObject() {
		BasicDBObject basicDBObject = new BasicDBObject();
		if (documentId != null)
		{
			basicDBObject.put(documentId.getTitle(), documentId.toBasicDBObject());
		}
		if (parentGrantDocument != null)
		{
			basicDBObject.put(parentGrantDocument.getTitle(), parentGrantDocument.toBasicDBObject());
		}
		if (parentPctDocument != null)
		{
			basicDBObject.put(parentPctDocument.getTitle(), parentPctDocument.toBasicDBObject());
		}
		if (parentStatus != null)
		{
			basicDBObject.put("Status", parentStatus);
		}
		return basicDBObject;
	}
	
	public String getTitle() {
		return title;
	}
	
}
