package org.safehaus.subutai.common.protocol;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by daralbaev on 7/20/14.
 */
public class Parameters {
	@SerializedName ("parameters")
	public List<Setting> parameters;
}
