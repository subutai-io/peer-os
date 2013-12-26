package org.safehaus.uspto.dtd;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.mongodb.BasicDBObject;

public class Figures implements Converter{

	private static final String title = "Figures";
	
	protected Logger logger;
	
	private String numberOfDrawingSheets;
	private String numberOfFigures;
	
	public Figures(Logger logger) {
		this.logger = logger;
	}
	
	public Figures(Element element, Logger logger)
	{
		this.logger = logger;
		
		NodeList nodeList = element.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element childElement = (Element) node;
				if (childElement.getNodeName().equals("number-of-drawing-sheets")) {
					numberOfDrawingSheets = childElement.getTextContent();
				}
				else if (childElement.getNodeName().equals("number-of-figures")) {
					numberOfFigures = childElement.getTextContent();
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

	public String getNumberOfDrawingSheets() {
		return numberOfDrawingSheets;
	}

	public String getNumberOfFigures() {
		return numberOfFigures;
	}

	@Override
	public String toString() {
		StringBuffer toStringBuffer = new StringBuffer(title+":");
		if (numberOfFigures != null)
		{
			toStringBuffer.append(" NumberOfFigures: ");
			toStringBuffer.append(numberOfFigures);
		}
		if (numberOfDrawingSheets != null)
		{
			toStringBuffer.append(" NumberOfDrawingSheets: ");
			toStringBuffer.append(numberOfDrawingSheets);
		}
		return toStringBuffer.toString();
	}
	
	public JSONObject toJSon() {
		JSONObject jsonObject = new JSONObject();
		if (numberOfFigures != null)
		{
			jsonObject.put("NumberOfFigures", numberOfFigures);
		}
		if (numberOfDrawingSheets != null)
		{
			jsonObject.put("NumberOfDrawingSheets", numberOfDrawingSheets);
		}
		return jsonObject;
	}

	public BasicDBObject toBasicDBObject() {
		BasicDBObject basicDBObject = new BasicDBObject();
		if (numberOfFigures != null)
		{
			basicDBObject.put("NumberOfFigures", numberOfFigures);
		}
		if (numberOfDrawingSheets != null)
		{
			basicDBObject.put("NumberOfDrawingSheets", numberOfDrawingSheets);
		}
		return basicDBObject;
	}
	
	public String getTitle() {
		return title;
	}

}
