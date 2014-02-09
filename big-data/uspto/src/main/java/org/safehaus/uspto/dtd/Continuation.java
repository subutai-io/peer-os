package org.safehaus.uspto.dtd;

import java.util.List;

import org.jdom2.Content;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.mongodb.BasicDBObject;

public class Continuation implements Converter{

	private static final String title = "Continuation";
	
	protected Logger logger;
	
	private Relation relation;
	
	public Continuation(Logger logger) {
		this.logger = logger;
	}

	public Continuation(Element element, Logger logger)
	{
		this.logger = logger;
		
		NodeList nodes = element.getChildNodes();

		for (int j=0; j < nodes.getLength(); j++)
		{
			Node node = nodes.item(j);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element childElement = (Element) node;
				if (childElement.getNodeName().equals("relation"))
				{
					relation = new Relation(childElement, logger);
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

	public Continuation(org.jdom2.Element element, Logger logger)
	{
		this.logger = logger;
		
		List<Content> nodes = element.getContent();
		for (int i=0; i < nodes.size(); i++)
		{
			Content node = nodes.get(i);
			if (node.getCType() == Content.CType.Element) {
				org.jdom2.Element childElement = (org.jdom2.Element) node;
				if (childElement.getName().equals("relation"))
				{
					relation = new Relation(childElement, logger);
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

	public Relation getRelation() {
		return relation;
	}
	
	@Override
	public String toString() {
		StringBuffer toStringBuffer = new StringBuffer(title+":");
		if (relation != null)
		{
			toStringBuffer.append(" ");
			toStringBuffer.append(relation);
		}
		return toStringBuffer.toString();
	}
	
	public JSONObject toJSon() {
		JSONObject jsonObject = new JSONObject();
		if (relation != null)
		{
			jsonObject.put(relation.getTitle(), relation.toJSon());
		}
		return jsonObject;
	}

	public BasicDBObject toBasicDBObject() {
		BasicDBObject basicDBObject = new BasicDBObject();
		if (relation != null)
		{
			basicDBObject.put(relation.getTitle(), relation.toBasicDBObject());
		}
		return basicDBObject;
	}
	
	public String getTitle() {
		return title;
	}

}
