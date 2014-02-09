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

import com.mongodb.BasicDBObject;

public class ClassificationCpc implements Converter{

	private static final String title = "ClassificationCpc";
	
	protected Logger logger;
	
	private String id;
	private String sequence;
	private CpcVersionIndicator cpcVersionIndicator;
	private String section;
	private String ipcClass;
	private String ipcSubClass;
	private String mainGroup;
	private String subGroup;
	private String symbolPosition;
	private String classificationValue;
	private ActionDate actionDate;
	private GeneratingOffice generatingOffice;
	private String classificationStatus;
	private String classificationDataSource;
	private String schemeOriginationCode;
	
	public ClassificationCpc(Logger logger) {
		this.logger = logger;
	}
	
	public ClassificationCpc(Element element, Logger logger)
	{
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
				else if (attribute.getNodeName().equals("sequence")) {
					sequence = attribute.getNodeValue();
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
				if (childElement.getNodeName().equals("cpc-version-indicator")) {
					cpcVersionIndicator = new CpcVersionIndicator(childElement, logger);
				}
				else if (childElement.getNodeName().equals("section")) {
					section = childElement.getTextContent();
				}
				else if (childElement.getNodeName().equals("class")) {
					ipcClass = childElement.getTextContent();
				}
				else if (childElement.getNodeName().equals("subclass")) {
					ipcSubClass = childElement.getTextContent();
				}
				else if (childElement.getNodeName().equals("main-group")) {
					mainGroup = childElement.getTextContent();
				}
				else if (childElement.getNodeName().equals("subgroup")) {
					subGroup = childElement.getTextContent();
				}
				else if (childElement.getNodeName().equals("symbol-position")) {
					symbolPosition = childElement.getTextContent();
				}
				else if (childElement.getNodeName().equals("classification-value")) {
					classificationValue = childElement.getTextContent();
				}
				else if (childElement.getNodeName().equals("action-date")) {
					actionDate = new ActionDate(childElement, logger);
				}
				else if (childElement.getNodeName().equals("generating-office")) {
					generatingOffice = new GeneratingOffice(childElement, logger);
				}
				else if (childElement.getNodeName().equals("classification-status")) {
					classificationStatus = childElement.getTextContent();
				}
				else if (childElement.getNodeName().equals("classification-data-source")) {
					classificationDataSource = childElement.getTextContent();
				}
				else if (childElement.getNodeName().equals("scheme-origination-code")) {
					schemeOriginationCode = childElement.getTextContent();
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

	public ClassificationCpc(org.jdom2.Element element, Logger logger)
	{
		this.logger = logger;
		
		List<Attribute> attributes = element.getAttributes();
		for (int i=0; i < attributes.size(); i++)
		{
			Attribute attribute = attributes.get(i);
			if (attribute.getName().equals("id")) {
				id = attribute.getValue();
			}
			else if (attribute.getName().equals("sequence")) {
				sequence = attribute.getValue();
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
				if (childElement.getName().equals("cpc-version-indicator")) {
					cpcVersionIndicator = new CpcVersionIndicator(childElement, logger);
				}
				else if (childElement.getName().equals("section")) {
					section = childElement.getValue();
				}
				else if (childElement.getName().equals("class")) {
					ipcClass = childElement.getValue();
				}
				else if (childElement.getName().equals("subclass")) {
					ipcSubClass = childElement.getValue();
				}
				else if (childElement.getName().equals("main-group")) {
					mainGroup = childElement.getValue();
				}
				else if (childElement.getName().equals("subgroup")) {
					subGroup = childElement.getValue();
				}
				else if (childElement.getName().equals("symbol-position")) {
					symbolPosition = childElement.getValue();
				}
				else if (childElement.getName().equals("classification-value")) {
					classificationValue = childElement.getValue();
				}
				else if (childElement.getName().equals("action-date")) {
					actionDate = new ActionDate(childElement, logger);
				}
				else if (childElement.getName().equals("generating-office")) {
					generatingOffice = new GeneratingOffice(childElement, logger);
				}
				else if (childElement.getName().equals("classification-status")) {
					classificationStatus = childElement.getValue();
				}
				else if (childElement.getName().equals("classification-data-source")) {
					classificationDataSource = childElement.getValue();
				}
				else if (childElement.getName().equals("scheme-origination-code")) {
					schemeOriginationCode = childElement.getValue();
				}
				else
				{
					logger.warn("Unknown Element {} in {} node", childElement.getName(), title);
				}
			}
			else if (node.getCType() == Content.CType.Text) {
				//ignore
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

	@Override
	public String toString() {
		StringBuffer toStringBuffer = new StringBuffer(title+":");
		if (id != null)
		{
			toStringBuffer.append(" Id: ");
			toStringBuffer.append(id);
		}
		if (sequence != null)
		{
			toStringBuffer.append(" Sequence: ");
			toStringBuffer.append(sequence);
		}
		if (cpcVersionIndicator != null)
		{
			toStringBuffer.append(" ");
			toStringBuffer.append(cpcVersionIndicator);
		}
		if (section != null)
		{
			toStringBuffer.append(" Section: ");
			toStringBuffer.append(section);
		}
		if (ipcClass != null)
		{
			toStringBuffer.append(" Class: ");
			toStringBuffer.append(ipcClass);
		}
		if (ipcSubClass != null)
		{
			toStringBuffer.append(" Subclass: ");
			toStringBuffer.append(ipcSubClass);
		}
		if (mainGroup != null)
		{
			toStringBuffer.append(" MainGroup: ");
			toStringBuffer.append(mainGroup);
		}
		if (subGroup != null)
		{
			toStringBuffer.append(" SubGroup: ");
			toStringBuffer.append(subGroup);
		}
		if (symbolPosition != null)
		{
			toStringBuffer.append(" SymbolPosition: ");
			toStringBuffer.append(symbolPosition);
		}
		if (classificationValue != null)
		{
			toStringBuffer.append(" ClassificationValue: ");
			toStringBuffer.append(classificationValue);
		}
		if (actionDate != null)
		{
			toStringBuffer.append(" ");
			toStringBuffer.append(actionDate);
		}
		if (generatingOffice != null)
		{
			toStringBuffer.append(" ");
			toStringBuffer.append(generatingOffice);
		}
		if (classificationStatus != null)
		{
			toStringBuffer.append(" ClassificationStatus: ");
			toStringBuffer.append(classificationStatus);
		}
		if (classificationDataSource != null)
		{
			toStringBuffer.append(" ClassificationDataSource: ");
			toStringBuffer.append(classificationDataSource);
		}
		if (schemeOriginationCode != null)
		{
			toStringBuffer.append(" SchemeOriginationCode: ");
			toStringBuffer.append(schemeOriginationCode);
		}
		return toStringBuffer.toString();
	}

	public JSONObject toJSon() {
		JSONObject jsonObject = new JSONObject();
		if (id != null)
		{
			jsonObject.put("Id", id);
		}
		if (sequence != null)
		{
			jsonObject.put("Sequence", sequence);
		}
		if (cpcVersionIndicator != null)
		{
			jsonObject.put(cpcVersionIndicator.getTitle(), cpcVersionIndicator.toJSon());
		}
		if (section != null)
		{
			jsonObject.put("Section", section);
		}
		if (ipcClass != null)
		{
			jsonObject.put("Class", ipcClass);
		}
		if (ipcSubClass != null)
		{
			jsonObject.put("SubClass", ipcSubClass);
		}
		if (mainGroup != null)
		{
			jsonObject.put("MainGroup", mainGroup);
		}
		if (subGroup != null)
		{
			jsonObject.put("SubGroup", subGroup);
		}
		if (symbolPosition != null)
		{
			jsonObject.put("SymbolPosition", symbolPosition);
		}
		if (classificationValue != null)
		{
			jsonObject.put("ClassificationValue", classificationValue);
		}
		if (actionDate != null)
		{
			jsonObject.put(actionDate.getTitle(), actionDate.toJSon());
		}
		if (generatingOffice != null)
		{
			jsonObject.put(generatingOffice.getTitle(), generatingOffice.toJSon());
		}
		if (classificationStatus != null)
		{
			jsonObject.put("ClassificationStatus", classificationStatus);
		}
		if (classificationDataSource != null)
		{
			jsonObject.put("ClassificationDataSource", classificationDataSource);
		}
		if (schemeOriginationCode != null)
		{
			jsonObject.put("SchemeOriginationCode", schemeOriginationCode);
		}
		return jsonObject;
	}

	public BasicDBObject toBasicDBObject() {
		BasicDBObject basicDBObject = new BasicDBObject();
		if (id != null)
		{
			basicDBObject.put("Id", id);
		}
		if (sequence != null)
		{
			basicDBObject.put("Sequence", sequence);
		}
		if (cpcVersionIndicator != null)
		{
			basicDBObject.put(cpcVersionIndicator.getTitle(), cpcVersionIndicator.toBasicDBObject());
		}
		if (section != null)
		{
			basicDBObject.put("Section", section);
		}
		if (ipcClass != null)
		{
			basicDBObject.put("Class", ipcClass);
		}
		if (ipcSubClass != null)
		{
			basicDBObject.put("SubClass", ipcSubClass);
		}
		if (mainGroup != null)
		{
			basicDBObject.put("MainGroup", mainGroup);
		}
		if (subGroup != null)
		{
			basicDBObject.put("SubGroup", subGroup);
		}
		if (symbolPosition != null)
		{
			basicDBObject.put("SymbolPosition", symbolPosition);
		}
		if (classificationValue != null)
		{
			basicDBObject.put("ClassificationValue", classificationValue);
		}
		if (actionDate != null)
		{
			basicDBObject.put(actionDate.getTitle(), actionDate.toBasicDBObject());
		}
		if (generatingOffice != null)
		{
			basicDBObject.put(generatingOffice.getTitle(), generatingOffice.toBasicDBObject());
		}
		if (classificationStatus != null)
		{
			basicDBObject.put("ClassificationStatus", classificationStatus);
		}
		if (classificationDataSource != null)
		{
			basicDBObject.put("ClassificationDataSource", classificationDataSource);
		}
		if (schemeOriginationCode != null)
		{
			basicDBObject.put("SchemeOriginationCode", schemeOriginationCode);
		}
		return basicDBObject;
	}
	
	public String getTitle() {
		return title;
	}

}
