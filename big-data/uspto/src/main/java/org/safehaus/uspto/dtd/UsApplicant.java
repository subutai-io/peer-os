package org.safehaus.uspto.dtd;

import java.util.ArrayList;
import java.util.Collection;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

public class UsApplicant implements Converter{

	private static final String title = "UsApplicant";
	
	protected Logger logger;
	
	private String sequence;
	private String applicantType;
	private String designation;
	private String applicantAuthorityCategory;
	private Collection<AddressBook> addressBooks;
	private Residence residence;
	private Collection<UsRights> usRightss;
	
	public UsApplicant(Logger logger) {
		this.logger = logger;
		addressBooks = new ArrayList<AddressBook>();
		usRightss = new ArrayList<UsRights>();
	}
	
	public UsApplicant(Element element, Logger logger)
	{
		this.logger = logger;
		addressBooks = new ArrayList<AddressBook>();
		usRightss = new ArrayList<UsRights>();
		
		NamedNodeMap nodemap = element.getAttributes();
		for (int i=0; i < nodemap.getLength(); i++)
		{
			Node childNode = nodemap.item(i);
			
			if (childNode.getNodeType() == Node.ATTRIBUTE_NODE) {
				Attr attribute = (Attr) childNode;
				if (attribute.getNodeName().equals("sequence")) {
					sequence = attribute.getNodeValue();
				}
				else if (attribute.getNodeName().equals("app-type")) {
					applicantType = attribute.getNodeValue();
				}
				else if (attribute.getNodeName().equals("designation")) {
					designation = attribute.getNodeValue();
				}
				else if (attribute.getNodeName().equals("applicant-authority-category")) {
					applicantAuthorityCategory = attribute.getNodeValue();
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
					addressBooks.add(new AddressBook(childElement, logger));
				}
				else if (childElement.getNodeName().equals("residence")) {
					residence = new Residence(childElement, logger);
				}
				else if (childElement.getNodeName().equals("us-rights")) {
					usRightss.add(new UsRights(childElement, logger));
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

	public String getApplicantType() {
		return applicantType;
	}

	public String getDesignation() {
		return designation;
	}

	public String getApplicantAuthorityCategory() {
		return applicantAuthorityCategory;
	}
	
	public Collection<AddressBook> getAddressBooks() {
		return addressBooks;
	}

	public Residence getResidence() {
		return residence;
	}

	public Collection<UsRights> getUsRightss() {
		return usRightss;
	}

	@Override
	public String toString() {
		StringBuffer toStringBuffer = new StringBuffer(title+":");
		if (sequence != null)
		{
			toStringBuffer.append(" Sequence: ");
			toStringBuffer.append(sequence);
		}
		if (applicantType != null)
		{
			toStringBuffer.append(" ApplicantType: ");
			toStringBuffer.append(applicantType);
		}
		if (designation != null)
		{
			toStringBuffer.append(" Designation: ");
			toStringBuffer.append(designation);
		}
		if (applicantAuthorityCategory != null)
		{
			toStringBuffer.append(" ApplicantAuthorityCategory: ");
			toStringBuffer.append(applicantAuthorityCategory);
		}
		for (AddressBook addressBook : addressBooks)
		{
			toStringBuffer.append("\n");
			toStringBuffer.append(addressBook);				
		}
		if (residence != null)
		{
			toStringBuffer.append(" ");
			toStringBuffer.append(residence);
		}
		for (UsRights usRights : usRightss)
		{
			toStringBuffer.append("\n");
			toStringBuffer.append(usRights);				
		}
		return toStringBuffer.toString();
	}

	public JSONObject toJSon() {
		JSONObject jsonObject = new JSONObject();
		if (sequence != null)
		{
			jsonObject.put("Sequence", sequence);
		}
		if (applicantType != null)
		{
			jsonObject.put("ApplicantType", applicantType);
		}
		if (designation != null)
		{
			jsonObject.put("Designation", designation);
		}
		if (applicantAuthorityCategory != null)
		{
			jsonObject.put("ApplicantAuthorityCategory", applicantAuthorityCategory);
		}
		if (addressBooks.size() > 0)
		{
			JSONArray jsonArray = new JSONArray();
			jsonObject.put("AddressBooks", jsonArray);
			for (AddressBook addressBook : addressBooks)
			{
				JSONObject elementJSon = new JSONObject();
				elementJSon.put(addressBook.getTitle(), addressBook.toJSon());
				jsonArray.put(elementJSon);
			}
		}
		if (residence != null)
		{
			jsonObject.put(residence.getTitle(), residence.toJSon());
		}
		if (usRightss.size() > 0)
		{
			JSONArray jsonArray = new JSONArray();
			jsonObject.put("UsRights", jsonArray);
			for (UsRights usRights : usRightss)
			{
				JSONObject elementJSon = new JSONObject();
				elementJSon.put(usRights.getTitle(), usRights.toJSon());
				jsonArray.put(elementJSon);
			}
		}
		return jsonObject;
	}

	public BasicDBObject toBasicDBObject() {
		BasicDBObject basicDBObject = new BasicDBObject();
		if (sequence != null)
		{
			basicDBObject.put("Sequence", sequence);
		}
		if (applicantType != null)
		{
			basicDBObject.put("ApplicantType", applicantType);
		}
		if (designation != null)
		{
			basicDBObject.put("Designation", designation);
		}
		if (applicantAuthorityCategory != null)
		{
			basicDBObject.put("ApplicantAuthorityCategory", applicantAuthorityCategory);
		}
		if (addressBooks.size() > 0)
		{
			BasicDBList basicDBList = new BasicDBList();
			basicDBObject.put("AddressBooks", basicDBList);
			for (AddressBook addressBook : addressBooks)
			{
				BasicDBObject elementDBObject = new BasicDBObject();
				elementDBObject.put(addressBook.getTitle(), addressBook.toBasicDBObject());
				basicDBList.add(elementDBObject);
			}
		}
		if (residence != null)
		{
			basicDBObject.put(residence.getTitle(), residence.toBasicDBObject());
		}
		if (usRightss.size() > 0)
		{
			BasicDBList basicDBList = new BasicDBList();
			basicDBObject.put("UsRights", basicDBList);
			for (UsRights usRights : usRightss)
			{
				BasicDBObject elementDBObject = new BasicDBObject();
				elementDBObject.put(usRights.getTitle(), usRights.toBasicDBObject());
				basicDBList.add(elementDBObject);
			}
		}
		return basicDBObject;
	}
	
	public String getTitle() {
		return title;
	}
	
}
