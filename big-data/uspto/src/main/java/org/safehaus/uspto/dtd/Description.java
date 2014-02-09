package org.safehaus.uspto.dtd;

import java.util.List;

import org.jdom2.Content;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.mongodb.BasicDBObject;

public class Description extends SingleCollection<Converter>{

	protected Logger logger;
	
	private static final String title = "Description";
	
	private DescriptionOfDrawings descriptionOfDrawings;
	
	public Description(Logger logger) {
		super();
		this.logger = logger;
	}
	
	public Description(Element element, Logger logger)
	{
		super(element);
		this.logger = logger;

		NodeList nodeList = element.getChildNodes();
		for (int i=0; i < nodeList.getLength(); i++)
		{
			Node node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element childElement = (Element) node;
				if (childElement.getNodeName().equals("description-of-drawings"))
				{
					elements.add(new DescriptionOfDrawings(childElement, logger));			
					//logger.info("Ref: {}", publicationReference);
				}
				else if (childElement.getNodeName().equals("p"))
				{
					elements.add(new Paragraph(childElement, logger));			
					//logger.info("Ref: {}", publicationReference);
				}
				else if (childElement.getNodeName().equals("heading"))
				{
					elements.add(new Heading(childElement, logger));			
					//logger.info("Ref: {}", publicationReference);
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

	public Description(org.jdom2.Element element, Logger logger)
	{
		super(element);
		this.logger = logger;

		List<Content> nodes = element.getContent();
		for (int i=0; i < nodes.size(); i++)
		{
			Content node = nodes.get(i);
			if (node.getCType() == Content.CType.Element) {
				org.jdom2.Element childElement = (org.jdom2.Element) node;
				if (childElement.getName().equals("description-of-drawings"))
				{
					elements.add(new DescriptionOfDrawings(childElement, logger));			
					//logger.info("Ref: {}", publicationReference);
				}
				else if (childElement.getName().equals("p"))
				{
					elements.add(new Paragraph(childElement, logger));			
					//logger.info("Ref: {}", publicationReference);
				}
				else if (childElement.getName().equals("heading"))
				{
					elements.add(new Heading(childElement, logger));			
					//logger.info("Ref: {}", publicationReference);
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

	public DescriptionOfDrawings getDescriptionOfDrawings() {
		return descriptionOfDrawings;
	}

	@Override
	public String toString() {
		StringBuffer toStringBuffer = new StringBuffer(title+":");
		if (descriptionOfDrawings != null)
		{
			toStringBuffer.append(" DescriptionOfDrawings: ");
			toStringBuffer.append(descriptionOfDrawings);
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
		if (descriptionOfDrawings != null)
		{
			jsonObject.put("DescriptionOfDrawings", descriptionOfDrawings);
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
		if (descriptionOfDrawings != null)
		{
			basicDBObject.put("DescriptionOfDrawings", descriptionOfDrawings);
		}
		return basicDBObject;
	}
	
	@Override
	public String getTitle() {
		return title;
	}

}
