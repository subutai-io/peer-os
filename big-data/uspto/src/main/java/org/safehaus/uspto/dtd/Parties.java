package org.safehaus.uspto.dtd;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.mongodb.BasicDBObject;

public class Parties implements Converter{

	private static final String title = "Parties";
	
	protected Logger logger;

	private Applicants applicants;
	private Inventors inventors;
	private Agents agents;
	
	public Parties(Logger logger) {
		this.logger = logger;
	}
	
	public Parties(Element element, Logger logger)
	{
		this.logger = logger;
		
		NodeList nodeList = element.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element childElement = (Element) node;
				if (childElement.getNodeName().equals("applicants")) {
					applicants  = new Applicants(childElement, logger);
				}
				else if (childElement.getNodeName().equals("inventors")) {
					inventors  = new Inventors(childElement, logger);
				}
				else if (childElement.getNodeName().equals("agents")) {
					agents  = new Agents(childElement, logger);
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


	public Applicants getUsApplicants() {
		return applicants;
	}

	public Inventors getInventors() {
		return inventors;
	}

	public Agents getAgents() {
		return agents;
	}
	
	
	@Override
	public String toString() {
		StringBuffer toStringBuffer = new StringBuffer(title+":");
		if (applicants != null)
		{
			toStringBuffer.append("\n");
			toStringBuffer.append(applicants);
		}
		if (inventors != null)
		{
			toStringBuffer.append("\n");
			toStringBuffer.append(inventors);
		}
		if (agents != null)
		{
			toStringBuffer.append("\n");
			toStringBuffer.append(agents);
		}
		return toStringBuffer.toString();
	}
	
	public JSONObject toJSon() {
		JSONObject jsonObject = new JSONObject();
		if (applicants != null)
		{
			jsonObject.put(applicants.getTitle(), applicants.toJSon());
		}
		if (inventors != null)
		{
			jsonObject.put(inventors.getTitle(), inventors.toJSon());
		}
		if (agents != null)
		{
			jsonObject.put(agents.getTitle(), agents.toJSon());
		}
		return jsonObject;
	}

	public BasicDBObject toBasicDBObject() {
		BasicDBObject basicDBObject = new BasicDBObject();
		if (applicants != null)
		{
			basicDBObject.put(applicants.getTitle(), applicants.toBasicDBObject());
		}
		if (inventors != null)
		{
			basicDBObject.put(inventors.getTitle(), inventors.toBasicDBObject());
		}
		if (agents != null)
		{
			basicDBObject.put(agents.getTitle(), agents.toBasicDBObject());
		}
		return basicDBObject;
	}
	
	public String getTitle() {
		return title;
	}

}
