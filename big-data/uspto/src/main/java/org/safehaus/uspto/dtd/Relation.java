package org.safehaus.uspto.dtd;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.mongodb.BasicDBObject;

public class Relation implements Converter{

	private static final String title = "Relation";
	
	protected Logger logger;
	
	private ParentDocument parentDocument;
	private ChildDocument childDocument;
	
	public Relation(Logger logger) {
		this.logger = logger;
	}

	public Relation(Element element, Logger logger)
	{
		this.logger = logger;
		
		NodeList nodes = element.getChildNodes();

		for (int j=0; j < nodes.getLength(); j++)
		{
			Node node = nodes.item(j);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element childElement = (Element) node;
				if (childElement.getNodeName().equals("parent-doc"))
				{
					parentDocument = new ParentDocument(childElement, logger);
				}
				else if (childElement.getNodeName().equals("child-doc"))
				{
					childDocument = new ChildDocument(childElement, logger);
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
	
	public ParentDocument getParentDocument() {
		return parentDocument;
	}

	public ChildDocument getChildDocument() {
		return childDocument;
	}
	
	@Override
	public String toString() {
		StringBuffer toStringBuffer = new StringBuffer(title+":");
		if (parentDocument != null)
		{
			toStringBuffer.append("\n");
			toStringBuffer.append(parentDocument);
		}
		if (childDocument != null)
		{
			toStringBuffer.append("\n");
			toStringBuffer.append(childDocument);
		}
		return toStringBuffer.toString();
	}
	
	public JSONObject toJSon() {
		JSONObject jsonObject = new JSONObject();
		if (parentDocument != null)
		{
			jsonObject.put(parentDocument.getTitle(), parentDocument.toJSon());
		}
		if (childDocument != null)
		{
			jsonObject.put(childDocument.getTitle(), childDocument.toJSon());
		}
		return jsonObject;
	}

	public BasicDBObject toBasicDBObject() {
		BasicDBObject basicDBObject = new BasicDBObject();
		if (parentDocument != null)
		{
			basicDBObject.put(parentDocument.getTitle(), parentDocument.toBasicDBObject());
		}
		if (childDocument != null)
		{
			basicDBObject.put(childDocument.getTitle(), childDocument.toBasicDBObject());
		}
		return basicDBObject;
	}
	
	public String getTitle() {
		return title;
	}
	
}
