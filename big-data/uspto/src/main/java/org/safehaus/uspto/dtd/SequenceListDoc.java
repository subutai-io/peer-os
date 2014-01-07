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
import org.w3c.dom.Text;

import com.mongodb.BasicDBObject;

public class SequenceListDoc extends SingleCollection<Converter>{

	private static final String title = "SequenceListDoc";
	
	protected Logger logger;
	
	private String id;
	private String lang;
	private String status;
	
	public SequenceListDoc(Logger logger) {
		super();
		this.logger = logger;
	}
	
	public SequenceListDoc(Element element, Logger logger)
	{
		super();
		this.logger = logger;
		
		NamedNodeMap nodemap = element.getAttributes();
		for (int i=0; i < nodemap.getLength(); i++)
		{
			Node childNode = nodemap.item(i);
			
			if (childNode.getNodeType() == Node.ATTRIBUTE_NODE) {
				Attr attribute = (Attr) childNode;
				if (attribute.getNodeName().equals("id")) {
					id = attribute.getNodeValue();
				}
				else if (attribute.getNodeName().equals("lang")) {
					lang = attribute.getNodeValue();
				}
				else if (attribute.getNodeName().equals("status")) {
					status = attribute.getNodeValue();
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
				if (childElement.getNodeName().equals("sequence-list")) {
					elements.add(new SequenceList(childElement, logger));
				}
				else
				{
					logger.warn("Unknown Element {} in {} node", childElement.getNodeName(), title);
				}
			}
			else if (node.getNodeType() == Node.TEXT_NODE) {
				Text text = (Text)node;
				elements.add(new TextNode(text));
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

	public SequenceListDoc(org.jdom2.Element element, Logger logger)
	{
		super();
		this.logger = logger;
		
		List<Attribute> attributes = element.getAttributes();
		for (int i=0; i < attributes.size(); i++)
		{
			Attribute attribute = attributes.get(i);
			if (attribute.getName().equals("id")) {
				id = attribute.getValue();
			}
			else if (attribute.getName().equals("lang")) {
				lang = attribute.getValue();
			}
			else if (attribute.getName().equals("status")) {
				status = attribute.getValue();
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
				if (childElement.getName().equals("sequence-list")) {
					elements.add(new SequenceList(childElement, logger));
				}
				else
				{
					logger.warn("Unknown Element {} in {} node", childElement.getName(), title);
				}
			}
			else if (node.getCType() == Content.CType.Text) {
				org.jdom2.Text text = (org.jdom2.Text)node;
				elements.add(new TextNode(text));
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

	public String getId() {
		return id;
	}

	public String getLang() {
		return lang;
	}

	public String getStatus() {
		return status;
	}

	@Override
	public String toString() {
		StringBuffer toStringBuffer = new StringBuffer(title+":");
		if (id != null)
		{
			toStringBuffer.append(" Id: ");
			toStringBuffer.append(id);
		}
		if (lang != null)
		{
			toStringBuffer.append(" Lang: ");
			toStringBuffer.append(lang);
		}
		if (status != null)
		{
			toStringBuffer.append(" Status: ");
			toStringBuffer.append(status);
		}
		toStringBuffer.append(super.toString());
		return toStringBuffer.toString();
	}

	public JSONObject toJSon() {
		JSONObject jsonObject = super.toJSon();
		if (jsonObject == null)
		{
			jsonObject = new JSONObject();
		}
		if (id != null)
		{
			jsonObject.put("Id", id);
		}
		if (lang != null)
		{
			jsonObject.put("Lang", lang);
		}
		if (status != null)
		{
			jsonObject.put("Status", status);
		}
		return jsonObject;
	}

	public BasicDBObject toBasicDBObject() {
		BasicDBObject basicDBObject = super.toBasicDBObject();
		if (basicDBObject == null)
		{
			basicDBObject = new BasicDBObject();
		}
		if (id != null)
		{
			basicDBObject.put("Id", id);
		}
		if (lang != null)
		{
			basicDBObject.put("Lang", lang);
		}
		if (status != null)
		{
			basicDBObject.put("Status", status);
		}
		return basicDBObject;
	}
	
	public String getTitle() {
		return title;
	}

}
