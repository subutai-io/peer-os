package org.safehaus.uspto.dtd;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.mongodb.BasicDBObject;

public class PctRegionalFilingData implements Converter{

	private static final String title = "PctRegionalFilingData";
	
	protected Logger logger;
	
	private DocumentId documentId;
	private Us371c124Date us371c124Date;
	
	public PctRegionalFilingData(Logger logger) {
		this.logger = logger;
	}

	public PctRegionalFilingData(Element element, Logger logger)
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
					//logger.info("Ref: {}", documentId);
				}
				else if (childElement.getNodeName().equals("us-371c124-date"))
				{
					us371c124Date = new Us371c124Date(childElement, logger);
					//logger.info("Ref: {}", documentId);
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

	public Us371c124Date getUs371c124Date() {
		return us371c124Date;
	}
	
	@Override
	public String toString() {
		StringBuffer toStringBuffer = new StringBuffer(title+":");
		if (documentId != null)
		{
			toStringBuffer.append(" ");
			toStringBuffer.append(documentId);
		}
		if (us371c124Date != null)
		{
			toStringBuffer.append(" ");
			toStringBuffer.append(us371c124Date);
		}
		return toStringBuffer.toString();
	}
	
	public JSONObject toJSon() {
		JSONObject jsonObject = new JSONObject();
		if (documentId != null)
		{
			jsonObject.put(documentId.getTitle(), documentId.toJSon());
		}
		if (us371c124Date != null)
		{
			jsonObject.put(us371c124Date.getTitle(), us371c124Date.toJSon());
		}
		return jsonObject;
	}

	public BasicDBObject toBasicDBObject() {
		BasicDBObject basicDBObject = new BasicDBObject();
		if (documentId != null)
		{
			basicDBObject.put(documentId.getTitle(), documentId.toBasicDBObject());
		}
		if (us371c124Date != null)
		{
			basicDBObject.put(us371c124Date.getTitle(), us371c124Date.toBasicDBObject());
		}
		return basicDBObject;
	}
	
	public String getTitle() {
		return title;
	}

}
