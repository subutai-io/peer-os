package org.safehaus.uspto.dtd;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.mongodb.BasicDBObject;

public class Assignee implements Converter{

	private static final String title = "Assignee";
	
	protected Logger logger;
	
	private String firstName;
	private String lastName;
	private String organizationName;
	private String role;
	private AddressBook addressBook;
	
	public Assignee(Logger logger) {
		this.logger = logger;
	}
	
	public Assignee(Element element, Logger logger)
	{
		this.logger = logger;

		NodeList nodeList = element.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element childElement = (Element) node;
				if (childElement.getNodeName().equals("addressbook")) {
					addressBook = new AddressBook(childElement, logger);
				}
				else if (childElement.getNodeName().equals("orgname")) {
					organizationName = childElement.getTextContent();
				}
				else if (childElement.getNodeName().equals("role")) {
					role = childElement.getTextContent();
				}
				else if (childElement.getNodeName().equals("first-name")) {
					firstName = childElement.getTextContent();
				}
				else if (childElement.getNodeName().equals("last-name")) {
					lastName = childElement.getTextContent();
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
	

	public String getOrganizationName() {
		return organizationName;
	}

	public String getRole() {
		return role;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public AddressBook getAddressBook() {
		return addressBook;
	}
	
	@Override
	public String toString() {
		StringBuffer toStringBuffer = new StringBuffer(title+":");
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
		if (addressBook != null)
		{
			toStringBuffer.append(" ");
			toStringBuffer.append(addressBook);
		}
		return toStringBuffer.toString();
	}

	public JSONObject toJSon() {
		JSONObject jsonObject = new JSONObject();
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
			jsonObject.put("OrganizationName", organizationName);
		}
		if (role != null)
		{
			jsonObject.put("Role", role);
		}
		if (addressBook != null)
		{
			jsonObject.put(addressBook.getTitle(), addressBook.toJSon());
		}
		return jsonObject;
	}

	public BasicDBObject toBasicDBObject() {
		BasicDBObject basicDBObject = new BasicDBObject();
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
			basicDBObject.put("OrganizationName", organizationName);
		}
		if (role != null)
		{
			basicDBObject.put("Role", role);
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
