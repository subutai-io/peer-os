package org.safehaus.uspto.dtd;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.mongodb.BasicDBObject;

public class UsProvisionalApplication implements Converter{

	private static final String title = "UsProvisionalApplication";
	
	protected Logger logger;
	
	private DocumentId documentId;
	private String usProvisionalApplicationStatus;
	
	public UsProvisionalApplication(Logger logger) {
		this.logger = logger;
	}

	public UsProvisionalApplication(Element element, Logger logger)
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
				else if (childElement.getNodeName().equals("us-provisional-application-status"))
				{
					usProvisionalApplicationStatus = childElement.getTextContent();
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

	public String getUsProvisionalApplicationStatus() {
		return usProvisionalApplicationStatus;
	}
	
	@Override
	public String toString() {
		StringBuffer toStringBuffer = new StringBuffer(title+":");
		if (documentId != null)
		{
			toStringBuffer.append(" ");
			toStringBuffer.append(documentId);
		}
		if (usProvisionalApplicationStatus != null)
		{
			toStringBuffer.append(" UsProvisionalApplicationStatus: ");
			toStringBuffer.append(usProvisionalApplicationStatus);
		}
		return toStringBuffer.toString();
	}

	public JSONObject toJSon() {
		JSONObject jsonObject = new JSONObject();
		if (documentId != null)
		{
			jsonObject.put(documentId.getTitle(), documentId.toJSon());
		}
		if (usProvisionalApplicationStatus != null)
		{
			jsonObject.put("UsProvisionalApplicationStatus", usProvisionalApplicationStatus);
		}
		return jsonObject;
	}

	public BasicDBObject toBasicDBObject() {
		BasicDBObject basicDBObject = new BasicDBObject();
		if (documentId != null)
		{
			basicDBObject.put(documentId.getTitle(), documentId.toBasicDBObject());
		}
		if (usProvisionalApplicationStatus != null)
		{
			basicDBObject.put("UsProvisionalApplicationStatus", usProvisionalApplicationStatus);
		}
		return basicDBObject;
	}
	
	public String getTitle() {
		return title;
	}
	
}
