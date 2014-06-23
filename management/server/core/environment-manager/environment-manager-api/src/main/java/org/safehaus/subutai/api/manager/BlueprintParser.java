package org.safehaus.subutai.api.manager;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


/**
 * Created by bahadyr on 6/24/14.
 */
public class BlueprintParser {

    Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();


    public Blueprint parseBlueprint( final String blueprintStr ) {

        Blueprint blueprint = gson.fromJson( blueprintStr, Blueprint.class );
        return blueprint;
    }
}
