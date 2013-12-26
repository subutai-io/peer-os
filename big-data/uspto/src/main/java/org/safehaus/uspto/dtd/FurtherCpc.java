package org.safehaus.uspto.dtd;

import java.util.ArrayList;
import java.util.Collection;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

public class FurtherCpc implements Converter{

	private static final String title = "FurtherCpc";
	
	protected Logger logger;
	
	private String id;
	private Collection<ClassificationCpc> classificationCpcs;
	private Collection<CombinationSet> combinationSets;
	
	public FurtherCpc(Logger logger) {
		this.logger = logger;
		classificationCpcs = new ArrayList<ClassificationCpc>();
		combinationSets = new ArrayList<CombinationSet>();
	}
	
	public FurtherCpc(Element element, Logger logger)
	{
		this.logger = logger;
		classificationCpcs = new ArrayList<ClassificationCpc>();
		combinationSets = new ArrayList<CombinationSet>();
		
		NamedNodeMap nodeMap = element.getAttributes();
		for (int i=0; i < nodeMap.getLength(); i++)
		{
			Node node = nodeMap.item(i);
			
			if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
				Attr attribute = (Attr) node;
				if (attribute.getNodeName().equals("id")) {
					id = attribute.getNodeValue();
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
				if (childElement.getNodeName().equals("classification-cpc")) {
					classificationCpcs.add(new ClassificationCpc(childElement, logger));
				}
				else if (childElement.getNodeName().equals("combination-set")) {
					combinationSets.add(new CombinationSet(childElement, logger));
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

	public Collection<ClassificationCpc> getClassificationCpcs() {
		return classificationCpcs;
	}

	public Collection<CombinationSet> getCombinationSets() {
		return combinationSets;
	}
	
	@Override
	public String toString() {
		StringBuffer toStringBuffer = new StringBuffer(title+":");
		if (id != null)
		{
			toStringBuffer.append(" Id: ");
			toStringBuffer.append(id);
		}
		for (ClassificationCpc classificationCpc : classificationCpcs)
		{
			toStringBuffer.append("\n");
			toStringBuffer.append(classificationCpc);
		}
		for (CombinationSet combinationSet : combinationSets)
		{
			toStringBuffer.append("\n");
			toStringBuffer.append(combinationSet);				
		}
		return toStringBuffer.toString();
	}

	public JSONObject toJSon() {
		JSONObject jsonObject = new JSONObject();
		if (id != null)
		{
			jsonObject.put("Id", id);
		}
		if (classificationCpcs.size() > 0)
		{
			JSONArray jsonArray = new JSONArray();
			jsonObject.put("ClassificationCpcs", jsonArray);
			for (ClassificationCpc classificationCpc : classificationCpcs)
			{
				JSONObject elementJSon = new JSONObject();
				elementJSon.put(classificationCpc.getTitle(), classificationCpc.toJSon());
				jsonArray.put(elementJSon);
			}
		}
		if (combinationSets.size() > 0)
		{
			JSONArray jsonArray = new JSONArray();
			jsonObject.put("CombinationSets", jsonArray);
			for (CombinationSet combinationSet : combinationSets)
			{
				JSONObject elementJSon = new JSONObject();
				elementJSon.put(combinationSet.getTitle(), combinationSet.toJSon());
				jsonArray.put(elementJSon);
			}
		}
		return jsonObject;
	}

	public BasicDBObject toBasicDBObject() {
		BasicDBObject basicDBObject = new BasicDBObject();
		if (id != null)
		{
			basicDBObject.put("Id", id);
		}
		if (classificationCpcs.size() > 0)
		{
			BasicDBList basicDBList = new BasicDBList();
			basicDBObject.put("ClassificationCpcs", basicDBList);
			for (ClassificationCpc classificationCpc : classificationCpcs)
			{
				BasicDBObject elementDBObject = new BasicDBObject();
				elementDBObject.put(classificationCpc.getTitle(), classificationCpc.toBasicDBObject());
				basicDBList.add(elementDBObject);
			}
			return basicDBObject;
		}
		if (combinationSets.size() > 0)
		{
			BasicDBList basicDBList = new BasicDBList();
			basicDBObject.put("ClassificationCpcs", basicDBList);
			for (CombinationSet combinationSet : combinationSets)
			{
				BasicDBObject elementDBObject = new BasicDBObject();
				elementDBObject.put(combinationSet.getTitle(), combinationSet.toBasicDBObject());
				basicDBList.add(elementDBObject);
			}
			return basicDBObject;
		}
		return basicDBObject;
	}
	
	public String getTitle() {
		return title;
	}


}
