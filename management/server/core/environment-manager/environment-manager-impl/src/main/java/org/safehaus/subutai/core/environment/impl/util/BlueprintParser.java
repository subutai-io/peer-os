package org.safehaus.subutai.core.environment.impl.util;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import org.safehaus.subutai.common.protocol.EnvironmentBlueprint;
import org.safehaus.subutai.core.environment.api.helper.Environment;


/**
 * Blueprint Parser Utility class
 */
public class BlueprintParser {

    Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();


    public EnvironmentBlueprint parseEnvironmentBlueprintText(final String blueprintStr) {
        try {
            return gson.fromJson(blueprintStr, EnvironmentBlueprint.class);
        } catch (JsonSyntaxException e) {
            System.out.println("Error parsing blueprint");
        }
        return null;
    }


    public String parseEnvironmentBlueprint(EnvironmentBlueprint blueprint) {
        //TODO catch parse exception
        return gson.toJson(blueprint, EnvironmentBlueprint.class);
    }


    public Environment parseEnvironment(final String blueprintStr) {
        try {
            return gson.fromJson(blueprintStr, Environment.class);
        } catch (JsonSyntaxException e) {
            System.out.println("Error parsing blueprint");
        }
        return null;
    }
}
