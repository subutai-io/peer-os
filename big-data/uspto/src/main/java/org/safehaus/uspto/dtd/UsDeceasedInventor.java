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

public class UsDeceasedInventor implements Converter{

	private static final String title = "UsDeceasedInventor";
	
	protected Logger logger;
	
	private String sequence;
	private AddressBook addressBook;
	
	public UsDeceasedInventor(Logger logger) {
		this.logger = logger;
	}
	
	public UsDeceasedInventor(Element element, Logger logger)
	{
		this.logger = logger;

		NamedNodeMap nodeMap = element.getAttributes();
		for (int i=0; i < nodeMap.getLength(); i++)
		{
			Node node = nodeMap.item(i);
			
			if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
				Attr attribute = (Attr) node;
				if (attribute.getNodeName().equals("sequence")) {
					sequence = attribute.getNodeValue();
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
				if (childElement.getNodeName().equals("addressbook")) {
					addressBook = new AddressBook(childElement, logger);
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
	
	public UsDeceasedInventor(org.jdom2.Element element, Logger logger)
	{
		this.logger = logger;

		List<Attribute> attributes = element.getAttributes();
		for (int i=0; i < attributes.size(); i++)
		{
			Attribute attribute = attributes.get(i);
			if (attribute.getName().equals("sequence")) {
				sequence = attribute.getValue();
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
				if (childElement.getName().equals("addressbook")) {
					addressBook = new AddressBook(childElement, logger);
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

	public String getSequence() {
		return sequence;
	}

	public AddressBook getAddressBook() {
		return addressBook;
	}
	
	@Override
	public String toString() {
		StringBuffer toStringBuffer = new StringBuffer(title+":");
		if (sequence != null)
		{
			toStringBuffer.append(" Sequence: ");
			toStringBuffer.append(sequence);
		}
		if (addressBook != null)
		{
			toStringBuffer.append(" ");
			toStringBuffer.append(addressBook);
		}
		return toStringBuffer.toString();
	}

	public JSONObject toJSon() {
		JSONObject jsonObject = new JSONObject();
		if (sequence != null)
		{
			jsonObject.put("Sequence", sequence);
		}
		if (addressBook != null)
		{
			jsonObject.put(addressBook.getTitle(), addressBook.toJSon());
		}
		return jsonObject;
	}

	public BasicDBObject toBasicDBObject() {
		BasicDBObject basicDBObject = new BasicDBObject();
		if (sequence != null)
		{
			basicDBObject.put("Sequence", sequence);
		}
		if (addressBook != null)
		{
			basicDBObject.put(addressBook.getTitle(), addressBook.toBasicDBObject());
		}
		return basicDBObject;
	}
	
	public String getTitle() {
		return title;
	}

}
