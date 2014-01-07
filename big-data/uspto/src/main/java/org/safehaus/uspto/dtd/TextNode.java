package org.safehaus.uspto.dtd;

import org.w3c.dom.Element;
import org.w3c.dom.Text;

public class TextNode implements Converter{

	private String text;
	
	public TextNode() {
		text = "";
	}
	
	public TextNode(Element element)
	{		
		text = element.getTextContent();
	}

	public TextNode(org.jdom2.Text element)
	{		
		text = element.getTextNormalize();
	}
	
	public TextNode(Text element)
	{		
		text = element.getNodeValue();
	}

	public String getText() {
		return text;
	}


	@Override
	public String toString() {
		return text;
	}

	public String toJSon() {
		return text;
	}

	public String toBasicDBObject() {
		return text;
	}

	public String getTitle() {
		return "";
	}
}
