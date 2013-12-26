package org.safehaus.uspto.dtd;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.mongodb.BasicDBObject;

public class ClassificationIpcr implements Converter{

	private static final String title = "ClassificationIpcr";
	
	protected Logger logger;
	
	private IpcVersionIndicator ipcVersionIndicator;
	private String classificationLevel;
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
	
	public ClassificationIpcr(Logger logger) {
		this.logger = logger;
	}
	
	public ClassificationIpcr(Element element, Logger logger)
	{
		this.logger = logger;
		
		NodeList nodeList = element.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element childElement = (Element) node;
				if (childElement.getNodeName().equals("ipc-version-indicator")) {
					ipcVersionIndicator = new IpcVersionIndicator(childElement, logger);
				}
				else if (childElement.getNodeName().equals("classification-level")) {
					classificationLevel = childElement.getTextContent();
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



	@Override
	public String toString() {
		StringBuffer toStringBuffer = new StringBuffer(title+":");
		if (ipcVersionIndicator != null)
		{
			toStringBuffer.append(" ");
			toStringBuffer.append(ipcVersionIndicator);
		}
		if (classificationLevel != null)
		{
			toStringBuffer.append(" Classification Level: ");
			toStringBuffer.append(classificationLevel);
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
			toStringBuffer.append(" Main Group: ");
			toStringBuffer.append(mainGroup);
		}
		if (subGroup != null)
		{
			toStringBuffer.append(" Sub Group: ");
			toStringBuffer.append(subGroup);
		}
		if (symbolPosition != null)
		{
			toStringBuffer.append(" Symbol Position: ");
			toStringBuffer.append(symbolPosition);
		}
		if (classificationValue != null)
		{
			toStringBuffer.append(" Classification Value: ");
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
			toStringBuffer.append(" Classification Status: ");
			toStringBuffer.append(classificationStatus);
		}
		if (classificationDataSource != null)
		{
			toStringBuffer.append(" Classification Data Source: ");
			toStringBuffer.append(classificationDataSource);
		}
		return toStringBuffer.toString();
	}

	public JSONObject toJSon() {
		JSONObject jsonObject = new JSONObject();
		if (ipcVersionIndicator != null)
		{
			jsonObject.put(ipcVersionIndicator.getTitle(), ipcVersionIndicator.toJSon());
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
		return jsonObject;
	}

	public BasicDBObject toBasicDBObject() {
		BasicDBObject basicDBObject = new BasicDBObject();
		if (ipcVersionIndicator != null)
		{
			basicDBObject.put(ipcVersionIndicator.getTitle(), ipcVersionIndicator.toBasicDBObject());
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
		return basicDBObject;
	}
	
	public String getTitle() {
		return title;
	}

}
