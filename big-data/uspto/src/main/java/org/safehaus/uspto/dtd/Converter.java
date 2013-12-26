package org.safehaus.uspto.dtd;


public interface Converter {

	public Object toJSon();
	
	public Object toBasicDBObject();
	
	public String getTitle();
	
}