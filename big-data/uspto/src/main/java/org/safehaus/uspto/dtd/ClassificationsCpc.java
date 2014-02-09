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

public class ClassificationsCpc implements Converter{

	private static final String title = "ClassificationsCpc";
	
	protected Logger logger;
	
	private String id;
	private MainCpc mainCpc;
	private FurtherCpc furtherCpc;
	
	public ClassificationsCpc(Logger logger) {
		this.logger = logger;
	}
	
	public ClassificationsCpc(Element element, Logger logger)
	{
		this.logger = logger;
		
		NamedNodeMap nodeMap = element.getAttributes();
		for (int i=0; i < nodeMap.getLength(); i++)
		{
			Node node = nodeMap.item(i);
			
			if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
				Attr attribute = (Attr) node;
				if (attribute.getNodeName().equals("id")) {
					id = attribute.getNodeValue();
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
				if (childElement.getNodeName().equals("main-cpc")) {
					mainCpc = new MainCpc(childElement, logger);
				}
				else if (childElement.getNodeName().equals("further-cpc")) {
					furtherCpc = new FurtherCpc(childElement, logger);
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

	public ClassificationsCpc(org.jdom2.Element element, Logger logger)
	{
		this.logger = logger;
		
		List<Attribute> attributes = element.getAttributes();
		for (int i=0; i < attributes.size(); i++)
		{
			Attribute attribute = attributes.get(i);
			if (attribute.getName().equals("id")) {
				id = attribute.getValue();
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
				if (childElement.getName().equals("main-cpc")) {
					mainCpc = new MainCpc(childElement, logger);
				}
				else if (childElement.getName().equals("further-cpc")) {
					furtherCpc = new FurtherCpc(childElement, logger);
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
		if (mainCpc != null)
		{
			toStringBuffer.append(" ");
			toStringBuffer.append(mainCpc);
		}
		if (furtherCpc != null)
		{
			toStringBuffer.append(" ");
			toStringBuffer.append(furtherCpc);
		}
		return toStringBuffer.toString();
	}

	public JSONObject toJSon() {
		JSONObject jsonObject = new JSONObject();
		if (id != null)
		{
			jsonObject.put("Id", id);
		}
		if (mainCpc != null)
		{
			jsonObject.put(mainCpc.getTitle(), mainCpc.toJSon());
		}
		if (furtherCpc != null)
		{
			jsonObject.put(furtherCpc.getTitle(), furtherCpc.toJSon());
		}
		return jsonObject;
	}

	public BasicDBObject toBasicDBObject() {
		BasicDBObject basicDBObject = new BasicDBObject();
		if (id != null)
		{
			basicDBObject.put("Id", id);
		}
		if (mainCpc != null)
		{
			basicDBObject.put(mainCpc.getTitle(), mainCpc.toBasicDBObject());
		}
		if (furtherCpc != null)
		{
			basicDBObject.put(furtherCpc.getTitle(), furtherCpc.toBasicDBObject());
		}
		return basicDBObject;
	}
	
	public String getTitle() {
		return title;
	}

}
