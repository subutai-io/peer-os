package org.safehaus.uspto;

import org.safehaus.uspto.dtd.UsPatentGrant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

public class USPTOV44Parser {

	Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public USPTOV44Parser() {
		// TODO Auto-generated constructor stub
	}
	
	public UsPatentGrant parseDocument(Document doc)
	{
		doc.getDocumentElement().normalize();
		String rootElement = doc.getDocumentElement().getNodeName();
		if (rootElement.equals("us-patent-grant"))
		{
			UsPatentGrant usPatentGrant = new UsPatentGrant(doc.getDocumentElement());
			//logger.info("Document: {}", usPatentGrant);
			return usPatentGrant;
		}
		else if (rootElement.equals("sequence-cwu"))
		{
			return null;
		}
		else
		{
			logger.warn("Unknown root element {}", rootElement);
			logger.warn("==========================");
	
			return null;
//			NodeList nodes = doc.getDocumentElement().getChildNodes();
//			for (int i = 0; i < nodes.getLength(); i++) {
//				Node node = nodes.item(i);
//				processNode(node);
//			}
		}
	}
	
	public void processNode(Node node)
	{
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element element = (Element) node;
				processElement(element);
			}
			else if (node.getNodeType() == Node.ATTRIBUTE_NODE)
			{
				Attr attribute = (Attr) node;
				processAttribute(attribute);
			}
			else if (node.getNodeType() == Node.DOCUMENT_NODE)
			{
				Document document = (Document) node;
				parseDocument(document);
			}
			else if (node.getNodeType() == Node.DOCUMENT_TYPE_NODE)
			{
				DocumentType type = (DocumentType) node;
				processDocumentType(type);
			}
			else if (node.getNodeType() == Node.TEXT_NODE)
			{
				Text text = (Text) node;
				processText(text);
			}
	}

	public void processElement(Element element)
	{
		System.out.println("Element Node Name: "+element.getNodeName()+" value: "+element.getNodeValue());
		NamedNodeMap nodemap = element.getAttributes();
		for (int i=0; i < nodemap.getLength(); i++)
		{
			Node node = nodemap.item(i);
			processNode(node);
		}
		
		NodeList nodeList = element.getChildNodes();
		for (int i=0; i < nodeList.getLength(); i++)
		{
			Node node = nodeList.item(i);
			processNode(node);
		}
	}
	
	public void processAttribute(Attr attribute)
	{
		System.out.println("Attribute Name: "+attribute.getNodeName()+" value: "+attribute.getNodeValue());
		NamedNodeMap nodemap = attribute.getAttributes();
		if (nodemap == null)
		{
			return;
		}
		
		for (int i=0; i < nodemap.getLength(); i++)
		{
			Node node = nodemap.item(i);
			processNode(node);
		}		
	}
	
	public void processDocumentType(DocumentType type)
	{
		System.out.println("Document Type Name: "+type.getNodeName()+" value: "+type.getNodeValue());
		NamedNodeMap nodemap = type.getAttributes();
		if (nodemap == null)
		{
			return;
		}
		
		for (int i=0; i < nodemap.getLength(); i++)
		{			
			Node node = nodemap.item(i);
			processNode(node);
		}
	}

	public void processText(Text text)
	{
		System.out.println("Text value: "+text.getNodeValue());
		NamedNodeMap nodemap = text.getAttributes();
		if (nodemap == null)
		{
			return;
		}
		
		for (int i=0; i < nodemap.getLength(); i++)
		{			
			Node node = nodemap.item(i);
			processNode(node);
		}
	}

}
