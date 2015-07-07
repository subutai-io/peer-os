package io.subutai.core.env.ui.forms;


import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;


/**
 * GSOn serialization exclusion strategy by field name
 */
public class FieldExclusionStrategy implements ExclusionStrategy
{
    private final String excludedFieldName;


    public FieldExclusionStrategy( final String excludedFieldName )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( excludedFieldName ) );

        this.excludedFieldName = excludedFieldName;
    }


    @Override
    public boolean shouldSkipField( final FieldAttributes f )
    {
        return f.getName().equalsIgnoreCase( excludedFieldName );
    }


    @Override
    public boolean shouldSkipClass( final Class<?> clazz )
    {
        return false;
    }
}
