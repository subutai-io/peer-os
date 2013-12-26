package org.safehaus.uspto.dtd;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.mongodb.BasicDBObject;

public class Abstract extends SingleCollection<Paragraph>{

	private static final String title = "Abstract";
	
	protected Logger logger;
	
	private AbstractProblem abstractProblem;
	private AbstractSolution abstractSolution;
	
	public Abstract(Logger logger) {
		super();
		this.logger = logger;
	}
	
	public Abstract(Element element, Logger logger)
	{
		super(element);
		this.logger = logger;
		
		NodeList nodeList = element.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element childElement = (Element) node;
				if (childElement.getNodeName().equals("abst-problem")) {
					abstractProblem = new AbstractProblem(childElement, logger);
				}
				if (childElement.getNodeName().equals("abst-solution")) {
					abstractSolution = new AbstractSolution(childElement, logger);
				}
				if (childElement.getNodeName().equals("p")) {
					elements.add(new Paragraph(childElement, logger));
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

	public AbstractProblem getAbstractProblem() {
		return abstractProblem;
	}

	public AbstractSolution getAbstractSolution() {
		return abstractSolution;
	}

	@Override
	public String toString() {
		StringBuffer toStringBuffer = new StringBuffer(title+":");
		if (abstractProblem != null)
		{
			toStringBuffer.append("\n");
			toStringBuffer.append(abstractProblem);
		}
		if (abstractSolution != null)
		{
			toStringBuffer.append("\n");
			toStringBuffer.append(abstractSolution);
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
		if (abstractProblem != null)
		{
			jsonObject.put(abstractProblem.getTitle(), abstractProblem.toJSon());
		}
		if (abstractSolution != null)
		{
			jsonObject.put(abstractSolution.getTitle(), abstractSolution.toJSon());
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
		if (abstractProblem != null)
		{
			basicDBObject.put(abstractProblem.getTitle(), abstractProblem.toBasicDBObject());
		}
		if (abstractSolution != null)
		{
			basicDBObject.put(abstractSolution.getTitle(), abstractSolution.toBasicDBObject());
		}
		return basicDBObject;
	}

	@Override
	public String getTitle() {
		return title;
	}
	
}
