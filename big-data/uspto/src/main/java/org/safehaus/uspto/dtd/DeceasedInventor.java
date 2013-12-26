package org.safehaus.uspto.dtd;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.mongodb.BasicDBObject;

public class DeceasedInventor implements Converter{

	private static final String title = "DeceasedInventor";
	
	protected Logger logger;
	
	private String sequence;
	private String language;
	private String firstName;
	private String lastName;
	private String organizationName;
	private String role;
	
	public DeceasedInventor(Logger logger) {
		this.logger = logger;
	}
	
	public DeceasedInventor(Element element, Logger logger)
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
				else if (attribute.getNodeName().equals("lang")) {
					language = attribute.getNodeValue();
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
				if (childElement.getNodeName().equals("first-name")) {
					firstName = childElement.getTextContent();
				}
				else if (childElement.getNodeName().equals("last-name")) {
					lastName = childElement.getTextContent();
				}
				else if (childElement.getNodeName().equals("orgname")) {
					organizationName = childElement.getTextContent();
				}
				else if (childElement.getNodeName().equals("role")) {
					role = childElement.getTextContent();
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

	public String getSequence() {
		return sequence;
	}
	
	public String getLanguage() {
		return language;
	}
	
	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public String getOrganizationName() {
		return organizationName;
	}
	
	public String getRole() {
		return role;
	}
	
	@Override
	public String toString() {
		StringBuffer toStringBuffer = new StringBuffer(title+":");
		if (sequence != null)
		{
			toStringBuffer.append(" Sequence: ");
			toStringBuffer.append(sequence);
		}
		if (language != null)
		{
			toStringBuffer.append(" Language: ");
			toStringBuffer.append(language);
		}
		if (firstName != null)
		{
			toStringBuffer.append(" Name: ");
			toStringBuffer.append(firstName);
		}
		if (lastName != null)
		{
			toStringBuffer.append(" Surname: ");
			toStringBuffer.append(lastName);
		}
		if (organizationName != null)
		{
			toStringBuffer.append(" Organization: ");
			toStringBuffer.append(organizationName);
		}
		if (role != null)
		{
			toStringBuffer.append(" Role: ");
			toStringBuffer.append(role);
		}
		return toStringBuffer.toString();
	}
	
	public JSONObject toJSon() {
		JSONObject jsonObject = new JSONObject();
		if (sequence != null)
		{
			jsonObject.put("Sequence", sequence);
		}
		if (language != null)
		{
			jsonObject.put("Language", language);
		}
		if (firstName != null)
		{
			jsonObject.put("Name", firstName);
		}
		if (lastName != null)
		{
			jsonObject.put("Surname", lastName);
		}
		if (organizationName != null)
		{
			jsonObject.put("Organization", organizationName);
		}
		if (role != null)
		{
			jsonObject.put("Role", role);
		}
		return jsonObject;
	}

	public BasicDBObject toBasicDBObject() {
		BasicDBObject basicDBObject = new BasicDBObject();
		if (sequence != null)
		{
			basicDBObject.put("Sequence", sequence);
		}
		if (language != null)
		{
			basicDBObject.put("Language", language);
		}
		if (firstName != null)
		{
			basicDBObject.put("Name", firstName);
		}
		if (lastName != null)
		{
			basicDBObject.put("Surname", lastName);
		}
		if (organizationName != null)
		{
			basicDBObject.put("Organization", organizationName);
		}
		if (role != null)
		{
			basicDBObject.put("Role", role);
		}
		return basicDBObject;
	}
	
	public String getTitle() {
		return title;
	}
	
}
