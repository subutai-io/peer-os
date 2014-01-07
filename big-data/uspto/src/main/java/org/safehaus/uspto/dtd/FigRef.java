package org.safehaus.uspto.dtd;

import java.util.List;

import org.jdom2.Attribute;
import org.jdom2.Content;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.mongodb.BasicDBObject;

public class FigRef extends SingleCollection<Converter>{

	private static final String title = "FigRef";
	
	protected Logger logger;
	
	private String idref;
	private String number;
	
	public FigRef(Logger logger) {
		super();
		this.logger = logger;
	}
	
	public FigRef(Element element, Logger logger)
	{
		super(element);
		this.logger = logger;
		
		NamedNodeMap nodeMap = element.getAttributes();
		for (int i=0; i < nodeMap.getLength(); i++)
		{
			Node node = nodeMap.item(i);
			
			if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
				Attr attribute = (Attr) node;
				if (attribute.getNodeName().equals("idref")) {
					idref = attribute.getNodeValue();
				}
				else if (attribute.getNodeName().equals("num")) {
					number = attribute.getNodeValue();
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
				if (childElement.getNodeName().equals("b")) {
					elements.add(new Bold(childElement, logger));
				}
				else if (childElement.getNodeName().equals("i")) {
					elements.add(new Italic(childElement, logger));
				}
				else if (childElement.getNodeName().equals("o")) {
					elements.add(new Overscore(childElement, logger));
				}
				else if (childElement.getNodeName().equals("u")) {
					elements.add(new Underscore(childElement, logger));
				}
				else if (childElement.getNodeName().equals("sup")) {
					elements.add(new Superscript(childElement, logger));
				}
				else if (childElement.getNodeName().equals("sub")) {
					elements.add(new Subscript(childElement, logger));
				}
				else if (childElement.getNodeName().equals("smallcaps")) {
					elements.add(new Smallcaps(childElement, logger));
				}
				else
				{
					logger.warn("Unknown Element {} in {} node", childElement.getNodeName(), title);
				}
			}
			else if (node.getNodeType() == Node.TEXT_NODE) {
				Text childText = (Text) node;
				elements.add(new TextNode(childText));
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

	public FigRef(org.jdom2.Element element, Logger logger)
	{
		super(element);
		this.logger = logger;
		
		List<Attribute> attributes = element.getAttributes();
		for (int i=0; i < attributes.size(); i++)
		{
			Attribute attribute = attributes.get(i);
			if (attribute.getName().equals("idref")) {
				idref = attribute.getValue();
			}
			else if (attribute.getName().equals("num")) {
				number = attribute.getValue();
			}
			else
			{
				logger.warn("Unknown Attribute {} in {} node", attribute.getName(), title);
			}
		}

		List<Content> nodes = element.getContent();
		for (int i=0; i < nodes.size(); i++)
		{
			Content node = nodes.get(i);
			if (node.getCType() == Content.CType.Element) {
				org.jdom2.Element childElement = (org.jdom2.Element) node;
				if (childElement.getName().equals("b")) {
					elements.add(new Bold(childElement, logger));
				}
				else if (childElement.getName().equals("i")) {
					elements.add(new Italic(childElement, logger));
				}
				else if (childElement.getName().equals("o")) {
					elements.add(new Overscore(childElement, logger));
				}
				else if (childElement.getName().equals("u")) {
					elements.add(new Underscore(childElement, logger));
				}
				else if (childElement.getName().equals("sup")) {
					elements.add(new Superscript(childElement, logger));
				}
				else if (childElement.getName().equals("sub")) {
					elements.add(new Subscript(childElement, logger));
				}
				else if (childElement.getName().equals("smallcaps")) {
					elements.add(new Smallcaps(childElement, logger));
				}
				else
				{
					logger.warn("Unknown Element {} in {} node", childElement.getName(), title);
				}
			}
			else if (node.getCType() == Content.CType.Text) {
				org.jdom2.Text text = (org.jdom2.Text)node;
				elements.add(new TextNode(text));
			}
			else if (node.getCType() == Content.CType.ProcessingInstruction) {
				//ignore
			}
			else
			{
				logger.warn("Unknown Node {} in {} node", node.getCType(), title);
			}
		}

	}

	public String getIdref() {
		return idref;
	}

	public String getNumber() {
		return number;
	}

	@Override
	public String toString() {
		StringBuffer toStringBuffer = new StringBuffer(title+":");
		if (idref != null)
		{
			toStringBuffer.append(" Idref: ");
			toStringBuffer.append(idref);
		}
		if (number != null)
		{
			toStringBuffer.append(" Num: ");
			toStringBuffer.append(number);
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
		if (idref != null)
		{
			jsonObject.put("Idref", idref);
		}
		if (number != null)
		{
			jsonObject.put("Num", number);
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
		if (idref != null)
		{
			basicDBObject.put("Idref", idref);
		}
		if (number != null)
		{
			basicDBObject.put("Num", number);
		}
		return basicDBObject;
	}

	@Override
	public String getTitle() {
		return title;
	}
	
}
