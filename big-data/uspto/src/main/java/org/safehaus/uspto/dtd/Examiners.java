package org.safehaus.uspto.dtd;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.mongodb.BasicDBObject;

public class Examiners implements Converter{

	private static final String title = "Examiners";
	
	protected Logger logger;

	private Examiner primaryExaminer;
	private Examiner assistantExaminer;
	private Examiner authorizedOfficer;
	
	public Examiners(Logger logger) {
		this.logger = logger;
	}
	
	public Examiners(Element element, Logger logger)
	{
		this.logger = logger;
		
		NodeList nodeList = element.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element childElement = (Element) node;
				if (childElement.getNodeName().equals("primary-examiner")) {
					primaryExaminer  = new Examiner(childElement, logger);
				}
				else if (childElement.getNodeName().equals("assistant-examiner")) {
					assistantExaminer  = new Examiner(childElement, logger);
				}
				else if (childElement.getNodeName().equals("authorized-officer")) {
					authorizedOfficer  = new Examiner(childElement, logger);
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
	
	public Examiner getPrimaryExaminer()
	{
		return primaryExaminer;
	}
	
	public Examiner getAssistantExaminer()
	{
		return assistantExaminer;
	}
	
	public Examiner getAuthorizedOfficer()
	{
		return authorizedOfficer;
	}

	@Override
	public String toString() {
		StringBuffer toStringBuffer = new StringBuffer(title+":");
		if (primaryExaminer != null)
		{
			toStringBuffer.append(" Primary: ");
			toStringBuffer.append(primaryExaminer);
		}
		if (assistantExaminer != null)
		{
			toStringBuffer.append(" Assistant: ");
			toStringBuffer.append(assistantExaminer);
		}
		if (authorizedOfficer != null)
		{
			toStringBuffer.append(" Officer: ");
			toStringBuffer.append(authorizedOfficer);
		}
		return toStringBuffer.toString();
	}
	
	public JSONObject toJSon() {
		JSONObject jsonObject = new JSONObject();
		if (primaryExaminer != null)
		{
			jsonObject.put("Primary", primaryExaminer.toJSon());
		}
		if (assistantExaminer != null)
		{
			jsonObject.put("Assistant", assistantExaminer.toJSon());
		}
		if (authorizedOfficer != null)
		{
			jsonObject.put("Officer", authorizedOfficer.toJSon());
		}
		return jsonObject;
	}

	public BasicDBObject toBasicDBObject() {
		BasicDBObject basicDBObject = new BasicDBObject();
		if (primaryExaminer != null)
		{
			basicDBObject.put("Primary", primaryExaminer.toBasicDBObject());
		}
		if (assistantExaminer != null)
		{
			basicDBObject.put("Assistant", assistantExaminer.toBasicDBObject());
		}
		if (authorizedOfficer != null)
		{
			basicDBObject.put("Officer", authorizedOfficer.toBasicDBObject());
		}
		return basicDBObject;
	}
	
	public String getTitle() {
		return title;
	}

}
