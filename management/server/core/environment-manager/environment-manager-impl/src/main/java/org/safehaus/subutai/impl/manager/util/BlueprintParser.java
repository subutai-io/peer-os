package org.safehaus.subutai.impl.manager.util;


import org.safehaus.subutai.api.manager.helper.Environment;
import org.safehaus.subutai.api.manager.helper.EnvironmentBlueprint;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;


/**
 * Created by bahadyr on 6/24/14.
 */
public class BlueprintParser {

    Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();


    public EnvironmentBlueprint parseEnvironmentBlueprintText( final String blueprintStr ) {
        try {
            EnvironmentBlueprint blueprint = gson.fromJson( blueprintStr, EnvironmentBlueprint.class );
            return blueprint;
        }
        catch ( JsonSyntaxException e ) {
            System.out.println( "Error parsing blueprint" );
        }
        return null;
    }


    public String parseEnvironmentBlueprint( EnvironmentBlueprint blueprint ) {
        //TODO catch parse exception
        String blueprintStr = gson.toJson( blueprint, EnvironmentBlueprint.class );
        return blueprintStr;
    }


    public Environment parseEnvironment( final String blueprintStr ) {
        try {
            Environment blueprint = gson.fromJson( blueprintStr, Environment.class );
            return blueprint;
        }
        catch ( JsonSyntaxException e ) {
            System.out.println( "Error parsing blueprint" );
        }
        return null;
    }


    public String parseEnvironment( Environment blueprint ) {
        //TODO catch parse exception
        String blueprintStr = gson.toJson( blueprint, Environment.class );
        return blueprintStr;
    }
}
