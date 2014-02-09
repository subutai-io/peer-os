package org.safehaus.uspto.dtd;

import java.util.List;

import org.jdom2.Content;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.mongodb.BasicDBObject;

public class Address implements Converter{

	private static final String title = "Address";
	
	protected Logger logger;
	
	private String city;
	private String country;
	private String state;
	private String postcode;
	private String street;
	private String mailcode;
	
	public Address(Logger logger) {
		this.logger = logger;
	}
	
	public Address(Element element, Logger logger)
	{
		this.logger = logger;
		
		NodeList nodeList = element.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element childElement = (Element) node;
				if (childElement.getNodeName().equals("city")) {
					city = childElement.getTextContent();
				}
				else if (childElement.getNodeName().equals("country")) {
					country = childElement.getTextContent();
				}
				else if (childElement.getNodeName().equals("state")) {
					state = childElement.getTextContent();
				}
				else if (childElement.getNodeName().equals("postcode")) {
					postcode = childElement.getTextContent();
				}
				else if (childElement.getNodeName().equals("street")) {
					street = childElement.getTextContent();
				}
				else if (childElement.getNodeName().equals("mailcode")) {
					mailcode = childElement.getTextContent();
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

	public Address(org.jdom2.Element element, Logger logger)
	{
		this.logger = logger;
		
		List<Content> nodes = element.getContent();
		for (int i=0; i < nodes.size(); i++)
		{
			Content node = nodes.get(i);
			if (node.getCType() == Content.CType.Element) {
				org.jdom2.Element childElement = (org.jdom2.Element) node;
				if (childElement.getName().equals("city")) {
					city = childElement.getValue();
				}
				else if (childElement.getName().equals("country")) {
					country = childElement.getValue();
				}
				else if (childElement.getName().equals("state")) {
					state = childElement.getValue();
				}
				else if (childElement.getName().equals("postcode")) {
					postcode = childElement.getValue();
				}
				else if (childElement.getName().equals("street")) {
					street = childElement.getValue();
				}
				else if (childElement.getName().equals("mailcode")) {
					mailcode = childElement.getValue();
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

	public String getCity() {
		return city;
	}

	public String getCountry() {
		return country;
	}

	public String getState() {
		return state;
	}
	
	public String getPostcode() {
		return postcode;
	}

	public String getStreet() {
		return street;
	}
	
	public String getMailcode() {
		return mailcode;
	}
	
	@Override
	public String toString() {
		StringBuffer toStringBuffer = new StringBuffer(title+":");
		if (city != null)
		{
			toStringBuffer.append(" City: ");
			toStringBuffer.append(city);
		}
		if (country != null)
		{
			toStringBuffer.append(" Country: ");
			toStringBuffer.append(country);
		}
		if (state != null)
		{
			toStringBuffer.append(" State: ");
			toStringBuffer.append(state);
		}
		if (postcode != null)
		{
			toStringBuffer.append(" Postcode: ");
			toStringBuffer.append(postcode);
		}
		if (street != null)
		{
			toStringBuffer.append(" Street: ");
			toStringBuffer.append(street);
		}
		if (mailcode != null)
		{
			toStringBuffer.append(" Mailcode: ");
			toStringBuffer.append(mailcode);
		}
		return toStringBuffer.toString();
	}
	
	public JSONObject toJSon() {
		JSONObject jsonObject = new JSONObject();
		if (city != null)
		{
			jsonObject.put("City", city);
		}
		if (country != null)
		{
			jsonObject.put("Country", country);
		}
		if (state != null)
		{
			jsonObject.put("State", state);
		}
		if (postcode != null)
		{
			jsonObject.put("Postcode", postcode);
		}
		if (street != null)
		{
			jsonObject.put("Street", street);
		}
		if (mailcode != null)
		{
			jsonObject.put("Mailcode", mailcode);
		}
		return jsonObject;
	}

	public BasicDBObject toBasicDBObject() {
		BasicDBObject basicDBObject = new BasicDBObject();
		if (city != null)
		{
			basicDBObject.put("City", city);
		}
		if (country != null)
		{
			basicDBObject.put("Country", country);
		}
		if (state != null)
		{
			basicDBObject.put("State", state);
		}
		if (postcode != null)
		{
			basicDBObject.put("Postcode", postcode);
		}
		if (street != null)
		{
			basicDBObject.put("Street", street);
		}
		if (mailcode != null)
		{
			basicDBObject.put("Mailcode", mailcode);
		}
		return basicDBObject;
	}
	
	public String getTitle() {
		return title;
	}

}
