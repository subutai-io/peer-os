package org.safehaus.uspto.dtd;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.mongodb.BasicDBObject;

public class UsSequenceListDoc implements Converter{

	private static final String title = "UsSequenceListDoc";
	
	protected Logger logger;
	
	private Paragraph paragraph;
	private SequenceList sequenceList;
	
	public UsSequenceListDoc(Logger logger) {
		this.logger = logger;
	}
	
	public UsSequenceListDoc(Element element, Logger logger)
	{
		this.logger = logger;
		
		NodeList nodeList = element.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element childElement = (Element) node;
				if (childElement.getNodeName().equals("p")) {
					paragraph = new Paragraph(childElement, logger);
				}
				else if (childElement.getNodeName().equals("sequence-list")) {
					sequenceList = new SequenceList(childElement, logger);
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

	public Paragraph getParagraph() {
		return paragraph;
	}

	public SequenceList getSequenceList() {
		return sequenceList;
	}
	
	@Override
	public String toString() {
		StringBuffer toStringBuffer = new StringBuffer(title+":");
		if (paragraph != null)
		{
			toStringBuffer.append("\n");
			toStringBuffer.append(paragraph);			
		}
		if (sequenceList != null)
		{
			toStringBuffer.append("\n");
			toStringBuffer.append(sequenceList);			
		}
		return toStringBuffer.toString();
	}

	public JSONObject toJSon() {
		JSONObject jsonObject = new JSONObject();
		if (paragraph != null)
		{
			jsonObject.put(paragraph.getTitle(), paragraph.toJSon());
		}
		if (sequenceList != null)
		{
			jsonObject.put(sequenceList.getTitle(), sequenceList.toJSon());
		}
		return jsonObject;
	}

	public BasicDBObject toBasicDBObject() {
		BasicDBObject basicDBObject = new BasicDBObject();
		if (paragraph != null)
		{
			basicDBObject.put(paragraph.getTitle(), paragraph.toBasicDBObject());
		}
		if (sequenceList != null)
		{
			basicDBObject.put(sequenceList.getTitle(), sequenceList.toBasicDBObject());
		}
		return basicDBObject;
	}
	
	public String getTitle() {
		return title;
	}

}
