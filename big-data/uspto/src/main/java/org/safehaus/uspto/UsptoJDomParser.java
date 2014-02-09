package org.safehaus.uspto;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.jdom2.Attribute;
import org.jdom2.Comment;
import org.jdom2.Content;
import org.jdom2.DocType;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.EntityRef;
import org.jdom2.JDOMException;
import org.jdom2.ProcessingInstruction;
import org.jdom2.Text;
import org.jdom2.input.SAXBuilder;
import org.safehaus.uspto.dtd.UsPatentGrant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

public class UsptoJDomParser {

	Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public UsptoJDomParser() {
		// TODO Auto-generated constructor stub
	}
	
	public UsPatentGrant parseXMLString(String xmlString, DocumentBuilderFactory dbFactory, UsptoEntityResolver entityResolver)
	{
		SAXBuilder builder = new SAXBuilder();
		builder.setEntityResolver(entityResolver);
		Document document = null;;
		try {
			document = (Document) builder.build(new InputSource(new StringReader(xmlString)));
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return parseDocument(document);
	}
	
	public UsPatentGrant parseDocument(Document doc)
	{
		Element rootElement = doc.getRootElement();
		if (rootElement.getName().equals("us-patent-grant"))
		{
			UsPatentGrant usPatentGrant = new UsPatentGrant(rootElement);
			//logger.info("Document: {}", usPatentGrant);
			return usPatentGrant;
		}
		else if (rootElement.getName().equals("PATDOC"))
		{
			UsPatentGrant usPatentGrant = new UsPatentGrant(rootElement);
			//logger.info("Document: {}", usPatentGrant);
			return usPatentGrant;
		}
		else if (rootElement.getName().equals("sequence-cwu"))
		{
			return null;
		}
		else
		{
			logger.warn("Unknown root element {}", rootElement);
			logger.warn("==========================");
	
			List<Content> nodes = rootElement.getContent();
			for (int i = 0; i < nodes.size(); i++) {
				Content node = nodes.get(i);
				processNode(node);
			}
			return null;
		}
	}
	
	public void processNode(Content node)
	{
			if (node.getCType() == Content.CType.Element) {
				Element element = (Element) node;
				processElement(element);
			}
			else if (node.getCType() == Content.CType.Comment)
			{
				Comment comment = (Comment) node;
				processComment(comment);
			}
			else if (node.getCType() == Content.CType.EntityRef)
			{
				EntityRef entityRef = (EntityRef) node;
				parseEntityRef(entityRef);
			}
			else if (node.getCType() == Content.CType.DocType)
			{
				DocType type = (DocType) node;
				processDocType(type);
			}
			else if (node.getCType() == Content.CType.Text)
			{
				Text text = (Text) node;
				processText(text);
			}
			else if (node.getCType() == Content.CType.ProcessingInstruction)
			{
				ProcessingInstruction instr = (ProcessingInstruction) node;
				processInstr(instr);
			}
	}

	public void processElement(Element element)
	{
		System.out.println("Element Node Name: "+element.getName()+" value: "+element.getValue());
		List<Attribute> attributes = element.getAttributes();
		for (int i=0; i < attributes.size(); i++)
		{
			Attribute attribute = attributes.get(i);
			processAttribute(attribute);
		}
		
		List<Content> nodes = element.getContent();
		for (int i = 0; i < nodes.size(); i++) {
			Content node = nodes.get(i);
			processNode(node);
		}
		
	}
	
	public void processAttribute(Attribute attribute)
	{
		System.out.println("Attribute Name: "+attribute.getName()+" value: "+attribute.getValue());	
	}
	
	public void parseEntityRef(EntityRef entityRef)
	{
		System.out.println("EntitRef Name: "+entityRef.getName()+" value: "+entityRef.getValue());	
	}
	
	public void processDocType(DocType type)
	{
		System.out.println("DocType Name: "+type.getElementName()+" value: "+type.getValue());
	}

	public void processText(Text text)
	{
		System.out.println("Text: "+text.getText() + " value: "+text.getValue());
	}
	
	public void processComment(Comment comment)
	{
		System.out.println("Comment text: "+comment.getText()+ " value: "+comment.getValue());
	}
	
	public void processInstr(ProcessingInstruction instr)
	{
		System.out.println("Comment data: "+instr.getData()+ " value: "+instr.getValue());
	}

}
