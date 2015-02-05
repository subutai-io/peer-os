package org.safehaus.subutai.core.identity.ui.tabs.subviews;


import java.util.EnumSet;
import java.util.Locale;

import org.safehaus.subutai.core.identity.api.PermissionGroup;

import com.vaadin.data.util.converter.Converter;


/**
 * Created by talas on 2/5/15.
 */
public class PermissionGroupConverter implements Converter<String, PermissionGroup>
{
    /**
     * Converts the given value from target type to source type. <p> A converter can optionally use locale to do the
     * conversion. </p> A converter should in most cases be symmetric so chaining {@link #convertToPresentation(Object,
     * Class, java.util.Locale)} and {@link #convertToModel(Object, Class, java.util.Locale)} should return the original
     * value.
     *
     * @param value The value to convert, compatible with the target type. Can be null
     * @param targetType The requested type of the return value
     * @param locale The locale to use for conversion. Can be null.
     *
     * @return The converted value compatible with the source type
     *
     * @throws com.vaadin.data.util.converter.Converter.ConversionException If the value could not be converted
     */
    @Override
    public PermissionGroup convertToModel( final String value, final Class<? extends PermissionGroup> targetType,
                                           final Locale locale ) throws ConversionException
    {
        for ( final PermissionGroup group : EnumSet.allOf( PermissionGroup.class ) )
        {
            if ( group.getName().equals( value ) )
            {
                return group;
            }
        }
        return null;
    }


    /**
     * Converts the given value from source type to target type. <p> A converter can optionally use locale to do the
     * conversion. </p> A converter should in most cases be symmetric so chaining {@link #convertToPresentation(Object,
     * Class, java.util.Locale)} and {@link #convertToModel(Object, Class, java.util.Locale)} should return the original
     * value.
     *
     * @param value The value to convert, compatible with the target type. Can be null
     * @param targetType The requested type of the return value
     * @param locale The locale to use for conversion. Can be null.
     *
     * @return The converted value compatible with the source type
     *
     * @throws com.vaadin.data.util.converter.Converter.ConversionException If the value could not be converted
     */
    @Override
    public String convertToPresentation( final PermissionGroup value, final Class<? extends String> targetType,
                                         final Locale locale ) throws ConversionException
    {
        return value.getName();
    }


    /**
     * The source type of the converter.
     *
     * Values of this type can be passed to {@link #convertToPresentation(Object, Class, java.util.Locale)}.
     *
     * @return The source type
     */
    @Override
    public Class<PermissionGroup> getModelType()
    {
        return PermissionGroup.class;
    }


    /**
     * The target type of the converter.
     *
     * Values of this type can be passed to {@link #convertToModel(Object, Class, java.util.Locale)}.
     *
     * @return The target type
     */
    @Override
    public Class<String> getPresentationType()
    {
        return String.class;
    }
}
