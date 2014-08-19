package org.safehaus.subutai.common;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashMap;


public class JsonUtil {

	public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();


	public static String toJson(Object key, Object value) {

		HashMap<Object, Object> map = new HashMap();
		map.put(key, value);

		return GSON.toJson(map);
	}
}
