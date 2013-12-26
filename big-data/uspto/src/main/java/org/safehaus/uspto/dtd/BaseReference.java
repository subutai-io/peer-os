package org.safehaus.uspto.dtd;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.mongodb.BasicDBObject;

public class BaseReference implements Converter {

	public enum ReferenceType {
		PUBLICATION(1),
		APPLICATION(2);
		 
		 private int type;
		 
		 private ReferenceType(int type) {
		   this.type = type;
		 }
		 
		 public int getType() {
		   return type;
		 }
		   
		@Override
	    public String toString() {
			switch (type) {
			case 1:
				return "Publication";
			case 2:
				return "Application";
			default:
				return "Unknown";
			} 
		}
	}
	
	private static final String title = "BaseReference";
	
	protected Logger logger;
	
	private DocumentId documentId;
	private ReferenceType referenceType;
	
	public BaseReference(Logger logger) {
		this.logger = logger;
	}

	public BaseReference(Element element, ReferenceType referenceType, Logger logger)
	{
		this.logger = logger;
		this.referenceType = referenceType;
		
		NodeList publicationRefNodes = element.getChildNodes();

		for (int j=0; j < publicationRefNodes.getLength(); j++)
		{
			Node node = publicationRefNodes.item(j);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element childElement = (Element) node;
				if (childElement.getNodeName().equals("document-id"))
				{
					documentId = new DocumentId(childElement, logger);
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

	public DocumentId getDocumentId() {
		return documentId;
	}
	
	public ReferenceType getReferenceType() {
		return referenceType;
	}
	
	@Override
	public String toString() {
		StringBuffer toStringBuffer = new StringBuffer("");
		if (documentId != null)
		{
			toStringBuffer.append(" ");
			toStringBuffer.append(documentId);
		}
		return toStringBuffer.toString();
	}
	
	public JSONObject toJSon() {
		JSONObject jsonObject = new JSONObject();
		if (documentId != null)
		{
			jsonObject.put(documentId.getTitle(), documentId.toJSon());
		}
		return jsonObject;
	}

	public BasicDBObject toBasicDBObject() {
		BasicDBObject basicDBObject = new BasicDBObject();
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
