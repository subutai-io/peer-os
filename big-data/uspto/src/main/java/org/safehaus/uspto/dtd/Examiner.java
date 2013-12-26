package org.safehaus.uspto.dtd;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.mongodb.BasicDBObject;

public class Examiner implements Converter{

	private static final String title = "Examiner";
	
	protected Logger logger;

	private String firstName;
	private String lastName;
	private String department;
	
	public Examiner(Logger logger) {
		this.logger = logger;
	}
	
	public Examiner(Element element, Logger logger)
	{
		this.logger = logger;
		
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
				else if (childElement.getNodeName().equals("department")) {
					department = childElement.getTextContent();
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

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public String getDepartment() {
		return department;
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
		if (department != null)
		{
			toStringBuffer.append(" Department: ");
			toStringBuffer.append(department);
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
		if (department != null)
		{
			jsonObject.put("Department", department);
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
		if (department != null)
		{
			basicDBObject.put("Department", department);
		}
		return basicDBObject;
	}
	
	public String getTitle() {
		return title;
	}

}

