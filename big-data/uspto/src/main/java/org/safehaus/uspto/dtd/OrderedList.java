package org.safehaus.uspto.dtd;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.mongodb.BasicDBObject;

public class OrderedList extends SingleCollection<ListItem>{

	private static final String title = "OrderedList";
	
	protected Logger logger;
	
	private String id;
	private String listStyle;
	private String compact;

	public OrderedList(Logger logger) {
		super();
		this.logger = logger;
	}
	
	public OrderedList(Element element, Logger logger)
	{
		super(element);
		this.logger = logger;
		
		NamedNodeMap nodeMap = element.getAttributes();
		for (int i=0; i < nodeMap.getLength(); i++)
		{
			Node node = nodeMap.item(i);
			
			if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
				Attr attribute = (Attr) node;
				if (attribute.getNodeName().equals("id")) {
					id = attribute.getNodeValue();
				}
				else if (attribute.getNodeName().equals("ol-style")) {
					listStyle = attribute.getNodeValue();
				}
				else if (attribute.getNodeName().equals("compact")) {
					compact = attribute.getNodeValue();
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
				if (childElement.getNodeName().equals("li")) {
						elements.add(new ListItem(childElement, logger));
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

	public String getId() {
		return id;
	}

	public String getListStyle() {
		return listStyle;
	}

	public String getCompact() {
		return compact;
	}

	@Override
	public String toString() {
		StringBuffer toStringBuffer = new StringBuffer(title+":");
		if (id != null)
		{
			toStringBuffer.append(" Id: ");
			toStringBuffer.append(id);
		}
		if (listStyle != null)
		{
			toStringBuffer.append(" ListStyle: ");
			toStringBuffer.append(listStyle);
		}
		if (compact != null)
		{
			toStringBuffer.append(" Compact: ");
			toStringBuffer.append(compact);
		}
		toStringBuffer.append(super.toString());
		return toStringBuffer.toString();
	}

	@Override
	public JSONObject toJSon() {
		JSONObject jsonObject = super.toJSon();
		if (jsonObject == null)
		{
			jsonObject = new JSONObject();
		}
		if (id != null)
		{
			jsonObject.put("Id", id);
		}
		if (listStyle != null)
		{
			jsonObject.put("ListStyle", listStyle);
		}
		if (compact != null)
		{
			jsonObject.put("Compact", compact);
		}
		return jsonObject;
	}

	@Override
	public BasicDBObject toBasicDBObject() {
		BasicDBObject basicDBObject = super.toBasicDBObject();
		if (basicDBObject == null)
		{
			basicDBObject = new BasicDBObject();
		}
		if (id != null)
		{
			basicDBObject.put("Idr", id);
		}
		if (listStyle != null)
		{
			basicDBObject.put("ListStyle", listStyle);
		}
		if (compact != null)
		{
			basicDBObject.put("Compact", compact);
		}
		return basicDBObject;
	}
	
	@Override
	public String getTitle() {
		return title;
	}
	
}
