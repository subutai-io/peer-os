package org.safehaus.uspto.dtd;

import java.util.List;

import org.jdom2.Content;
import org.slf4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

public class ClaimText extends SingleCollection<Converter>{

	private static final String title = "ClaimText";
	
	protected Logger logger;

	public ClaimText(Logger logger) {
		super();
		this.logger = logger;
	}
	
	public ClaimText(Element element, Logger logger)
	{
		super(element);
		this.logger = logger;
		
		NodeList nodeList = element.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element childElement = (Element) node;
				if (childElement.getNodeName().equals("claim-text")) {
					elements.add(new ClaimText(childElement, logger));
				}
				else if (childElement.getNodeName().equals("claim-ref")) {
					elements.add(new ClaimRef(childElement, logger));
				}
				else if (childElement.getNodeName().equals("b")) {
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
				else if (childElement.getNodeName().equals("figref")) {
					elements.add(new FigRef(childElement, logger));
				}
				else if (childElement.getNodeName().equals("img")) {
					elements.add(new Image(childElement, logger));
				}
				else if (childElement.getNodeName().equals("chemistry")) {
					elements.add(new Chemistry(childElement, logger));
				}
				else if (childElement.getNodeName().equals("maths")) {
					elements.add(new Maths(childElement, logger));
				}
				else if (childElement.getNodeName().equals("tables")) {
					elements.add(new Tables(childElement, logger));
				}
				else if (childElement.getNodeName().equals("br")) {
					//ignore
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

	public ClaimText(org.jdom2.Element element, Logger logger)
	{
		super(element);
		this.logger = logger;
		
		List<Content> nodes = element.getContent();
		for (int i=0; i < nodes.size(); i++)
		{
			Content node = nodes.get(i);
			if (node.getCType() == Content.CType.Element) {
				org.jdom2.Element childElement = (org.jdom2.Element) node;
				if (childElement.getName().equals("claim-text")) {
					elements.add(new ClaimText(childElement, logger));
				}
				else if (childElement.getName().equals("claim-ref")) {
					elements.add(new ClaimRef(childElement, logger));
				}
				else if (childElement.getName().equals("b")) {
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
				else if (childElement.getName().equals("figref")) {
					elements.add(new FigRef(childElement, logger));
				}
				else if (childElement.getName().equals("img")) {
					elements.add(new Image(childElement, logger));
				}
				else if (childElement.getName().equals("chemistry")) {
					elements.add(new Chemistry(childElement, logger));
				}
				else if (childElement.getName().equals("maths")) {
					elements.add(new Maths(childElement, logger));
				}
				else if (childElement.getName().equals("tables")) {
					elements.add(new Tables(childElement, logger));
				}
				else if (childElement.getName().equals("br")) {
					//ignore
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
