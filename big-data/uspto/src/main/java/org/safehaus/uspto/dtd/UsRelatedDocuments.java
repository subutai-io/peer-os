package org.safehaus.uspto.dtd;

import java.util.List;

import org.jdom2.Content;
import org.slf4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class UsRelatedDocuments  extends SingleCollection<Converter>{

	private static final String title = "UsRelatedDocuments";
	
	protected Logger logger;
	
	public UsRelatedDocuments(Logger logger) {
		super();
		this.logger = logger;
	}
	
	public UsRelatedDocuments(Element element, Logger logger)
	{
		super(element);
		this.logger = logger;
		
		NodeList nodeList = element.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element childElement = (Element) node;
				if (childElement.getNodeName().equals("continuation-in-part")) {
					elements.add(new ContinuationInPart(childElement, logger));
				}
				else if (childElement.getNodeName().equals("division")) {
					elements.add(new Division(childElement, logger));
				}
				else if (childElement.getNodeName().equals("continuation")) {
					elements.add(new Continuation(childElement, logger));
				}
				else if (childElement.getNodeName().equals("us-provisional-application")) {
					elements.add(new UsProvisionalApplication(childElement, logger));
				}
				else if (childElement.getNodeName().equals("reissue")) {
					elements.add(new Reissue(childElement, logger));
				}
				else if (childElement.getNodeName().equals("substitution")) {
					elements.add(new Substitution(childElement, logger));
				}
				else if (childElement.getNodeName().equals("related-publication")) {
					elements.add(new RelatedPublication(childElement, logger));
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

	public UsRelatedDocuments(org.jdom2.Element element, Logger logger)
	{
		super(element);
		this.logger = logger;
		
		List<Content> nodes = element.getContent();
		for (int i=0; i < nodes.size(); i++)
		{
			Content node = nodes.get(i);
			if (node.getCType() == Content.CType.Element) {
				org.jdom2.Element childElement = (org.jdom2.Element) node;
				if (childElement.getName().equals("continuation-in-part")) {
					elements.add(new ContinuationInPart(childElement, logger));
				}
				else if (childElement.getName().equals("division")) {
					elements.add(new Division(childElement, logger));
				}
				else if (childElement.getName().equals("continuation")) {
					elements.add(new Continuation(childElement, logger));
				}
				else if (childElement.getName().equals("us-provisional-application")) {
					elements.add(new UsProvisionalApplication(childElement, logger));
				}
				else if (childElement.getName().equals("reissue")) {
					elements.add(new Reissue(childElement, logger));
				}
				else if (childElement.getName().equals("substitution")) {
					elements.add(new Substitution(childElement, logger));
				}
				else if (childElement.getName().equals("related-publication")) {
					elements.add(new RelatedPublication(childElement, logger));
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
		toStringBuffer.append(super.toString());
		return toStringBuffer.toString();
	}
	
	@Override
	public String getTitle() {
		return title;
	}
	
}
