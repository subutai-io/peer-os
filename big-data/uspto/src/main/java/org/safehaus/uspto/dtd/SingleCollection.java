package org.safehaus.uspto.dtd;

import java.util.ArrayList;
import java.util.Collection;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Element;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

public abstract class SingleCollection<Type extends Converter> implements Converter{

	protected Collection<Type> elements;
	
	public SingleCollection() {
		elements = new ArrayList<Type>();
	}
	
	public SingleCollection(Element element)
	{
		elements = new ArrayList<Type>();
	}

	public SingleCollection(org.jdom2.Element element)
	{
		elements = new ArrayList<Type>();
	}

	public Collection<Type> getElements() {
		return elements;
	}

	@Override
	public String toString() {
		StringBuffer toStringBuffer = new StringBuffer();
		for (Type element : elements)
		{
			toStringBuffer.append("\n");
			toStringBuffer.append(element);				
		}
		return toStringBuffer.toString();
	}

	public JSONObject toJSon() {
		if (elements.size() > 0)
		{
			JSONObject jsonObject = new JSONObject();
			JSONArray jsonArray = new JSONArray();
			jsonObject.put("elements", jsonArray);
			for (Type element : elements)
			{
				JSONObject elementJSon = new JSONObject();
				elementJSon.put(element.getTitle(), element.toJSon());
				jsonArray.put(elementJSon);
			}
			return jsonObject;
		}
		else
		{
			return null;
		}
	}

	public BasicDBObject toBasicDBObject() {
		if (elements.size() > 0)
		{
			BasicDBObject basicDBObject = new BasicDBObject();
			BasicDBList basicDBList = new BasicDBList();
			basicDBObject.put("elements", basicDBList);
			for (Type element : elements)
			{
				BasicDBObject elementDBObject = new BasicDBObject();
				elementDBObject.put(element.getTitle(), element.toBasicDBObject());
				basicDBList.add(elementDBObject);
			}
			return basicDBObject;
		}
		else {
			return null;
		}
	}

	abstract public String getTitle();
	
}
