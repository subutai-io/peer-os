package org.safehaus.uspto.dtd;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.mongodb.BasicDBObject;

public class ClassificationNational implements Converter{

	private static final String title = "ClassificationNational";
	
	protected Logger logger;

	private String country;
	private String mainClassification;
	private String furtherClassification;
	private String additionalInfo;
	
	public ClassificationNational(Logger logger) {
		this.logger = logger;
	}
	
	public ClassificationNational(Element element, Logger logger)
	{
		this.logger = logger;
		
		NodeList nodeList = element.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element childElement = (Element) node;
				if (childElement.getNodeName().equals("country")) {
					country = childElement.getTextContent();
				}
				else if (childElement.getNodeName().equals("main-classification")) {
					mainClassification = childElement.getTextContent();
				}
				else if (childElement.getNodeName().equals("further-classification")) {
					furtherClassification = childElement.getTextContent();
				}
				else if (childElement.getNodeName().equals("additional-info")) {
					additionalInfo = childElement.getTextContent();
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

	
	public String getCountry() {
		return country;
	}

	public String getMainClassification() {
		return mainClassification;
	}
	
	public String getFurtherClassification() {
		return furtherClassification;
	}

	public String getAdditionalInfo() {
		return additionalInfo;
	}
	
	@Override
	public String toString() {
		StringBuffer toStringBuffer = new StringBuffer(title+":");
		if (country != null)
		{
			toStringBuffer.append(" Country: ");
			toStringBuffer.append(country);
		}
		if (mainClassification != null)
		{
			toStringBuffer.append(" MainClassification: ");
			toStringBuffer.append(mainClassification);
		}
		if (furtherClassification != null)
		{
			toStringBuffer.append(" FurtherClassification: ");
			toStringBuffer.append(furtherClassification);
		}
		if (additionalInfo != null)
		{
			toStringBuffer.append(" AdditionalInfo: ");
			toStringBuffer.append(additionalInfo);
		}
		return toStringBuffer.toString();
	}

	public JSONObject toJSon() {
		JSONObject jsonObject = new JSONObject();
		if (country != null)
		{
			jsonObject.put("Country", country);
		}
		if (mainClassification != null)
		{
			jsonObject.put("MainClassification", mainClassification);
		}
		if (furtherClassification != null)
		{
			jsonObject.put("FurtherClassification", furtherClassification);
		}
		if (additionalInfo != null)
		{
			jsonObject.put("AdditionalInfo", additionalInfo);
		}
		return jsonObject;
	}

	public BasicDBObject toBasicDBObject() {
		BasicDBObject basicDBObject = new BasicDBObject();
		if (country != null)
		{
			basicDBObject.put("Country", country);
		}
		if (mainClassification != null)
		{
			basicDBObject.put("MainClassification", mainClassification);
		}
		if (furtherClassification != null)
		{
			basicDBObject.put("FurtherClassification", furtherClassification);
		}
		if (additionalInfo != null)
		{
			basicDBObject.put("AdditionalInfo", additionalInfo);
		}
		return basicDBObject;
	}
	
	public String getTitle() {
		return title;
	}

}
