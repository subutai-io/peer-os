package org.safehaus.uspto.dtd;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.mongodb.BasicDBObject;

public class UsRights implements Converter{

	private static final String title = "UsRights";
	
	protected Logger logger;
	
	private String text;
	private String toDeadInventor;
	private String kind;
	
	public UsRights(Logger logger) {
		this.logger = logger;
	}
	
	public UsRights(Element element, Logger logger)
	{
		this.logger = logger;
		
		NamedNodeMap nodemap = element.getAttributes();
		for (int i=0; i < nodemap.getLength(); i++)
		{
			Node childNode = nodemap.item(i);
			
			if (childNode.getNodeType() == Node.ATTRIBUTE_NODE) {
				Attr attribute = (Attr) childNode;
				if (attribute.getNodeName().equals("to-dead-inventor")) {
					toDeadInventor = attribute.getNodeValue();
				}
				else if (attribute.getNodeName().equals("kind")) {
					kind = attribute.getNodeValue();
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
			if (node.getNodeType() == Node.TEXT_NODE) {
				Text childText = (Text) node;
				text = childText.getNodeValue();
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


	public String getText() {
		return text;
	}

	public String getToDeadInventor() {
		return toDeadInventor;
	}

	public String getKind() {
		return kind;
	}
	
	@Override
	public String toString() {
		StringBuffer toStringBuffer = new StringBuffer(title+":");
		if (toDeadInventor != null)
		{
			toStringBuffer.append(" ToDeadInventor: ");
			toStringBuffer.append(toDeadInventor);
		}
		if (kind != null)
		{
			toStringBuffer.append(" Kind: ");
			toStringBuffer.append(kind);
		}
		if (text != null)
		{
			toStringBuffer.append(" Text: ");
			toStringBuffer.append(text);
		}
		return toStringBuffer.toString();
	}

	public JSONObject toJSon() {
		JSONObject jsonObject = new JSONObject();
		if (toDeadInventor != null)
		{
			jsonObject.put("ToDeadInventor", toDeadInventor);
		}
		if (kind != null)
		{
			jsonObject.put("Kind", kind);
		}
		if (text != null)
		{
			jsonObject.put("Text", text);
		}
		return jsonObject;
	}

	public BasicDBObject toBasicDBObject() {
		BasicDBObject basicDBObject = new BasicDBObject();
		if (toDeadInventor != null)
		{
			basicDBObject.put("ToDeadInventor", toDeadInventor);
		}
		if (kind != null)
		{
			basicDBObject.put("Kind", kind);
		}
		if (text != null)
		{
			basicDBObject.put("Text", text);
		}
		return basicDBObject;
	}
	
	public String getTitle() {
		return title;
	}
	
}
