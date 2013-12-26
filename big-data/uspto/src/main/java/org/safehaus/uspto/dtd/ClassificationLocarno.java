package org.safehaus.uspto.dtd;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.mongodb.BasicDBObject;

public class ClassificationLocarno  implements Converter{

	private static final String title = "ClassificationLocarno";
	
	protected Logger logger;

	private String edition;
	private String mainClassification;
	
	public ClassificationLocarno(Logger logger) {
		this.logger = logger;
	}
	
	public ClassificationLocarno(Element element, Logger logger)
	{
		this.logger = logger;
		
		NodeList nodeList = element.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element childElement = (Element) node;
				if (childElement.getNodeName().equals("edition")) {
					edition = childElement.getTextContent();
				}
				else if (childElement.getNodeName().equals("main-classification")) {
					mainClassification = childElement.getTextContent();
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

	
	public String getEdition() {
		return edition;
	}

	public String getMainClassification() {
		return mainClassification;
	}
	
	@Override
	public String toString() {
		StringBuffer toStringBuffer = new StringBuffer(title+":");
		if (edition != null)
		{
			toStringBuffer.append(" Edition: ");
			toStringBuffer.append(edition);
		}
		if (mainClassification != null)
		{
			toStringBuffer.append(" MainClassification: ");
			toStringBuffer.append(mainClassification);
		}
		return toStringBuffer.toString();
	}
	
	public JSONObject toJSon() {
		JSONObject jsonObject = new JSONObject();
		if (edition != null)
		{
			jsonObject.put("Edition", edition);
		}
		if (mainClassification != null)
		{
			jsonObject.put("MainClassification", mainClassification);
		}
		return jsonObject;
	}

	public BasicDBObject toBasicDBObject() {
		BasicDBObject basicDBObject = new BasicDBObject();
		if (edition != null)
		{
			basicDBObject.put("Edition", edition);
		}
		if (mainClassification != null)
		{
			basicDBObject.put("MainClassification", mainClassification);
		}
		return basicDBObject;
	}
	
	public String getTitle() {
		return title;
	}

}
