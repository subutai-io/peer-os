package org.safehaus.subutai.api.manager.util;


import org.safehaus.subutai.api.manager.helper.Blueprint;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;


/**
 * Created by bahadyr on 6/24/14.
 */
public class BlueprintParser {

    Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();


    public Blueprint parseBlueprint( final String blueprintStr ) {
        try {
            Blueprint blueprint = gson.fromJson( blueprintStr, Blueprint.class );
            return blueprint;
        } catch (JsonSyntaxException e) {
            System.out.println("Error parsing blueprint");
        }
        return null;
    }
}
