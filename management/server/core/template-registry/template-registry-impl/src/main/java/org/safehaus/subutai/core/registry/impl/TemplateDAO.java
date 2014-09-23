package org.safehaus.subutai.core.registry.impl;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.safehaus.subutai.core.db.api.DBException;
import org.safehaus.subutai.core.db.api.DbManager;
import org.safehaus.subutai.core.registry.api.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;


/**
 * Provides Data Access API for templates
 */
public class TemplateDAO
{
    private static final Logger LOG = LoggerFactory.getLogger( TemplateDAO.class.getName() );
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final String KEY_FORMAT = "%s-%s";
    private DbManager dbManager;


    public TemplateDAO( final DbManager dbManager )
    {
        Preconditions.checkNotNull( dbManager, "DB Manager is null" );
        this.dbManager = dbManager;
    }


    /**
     * Returns all registered templates from database
     *
     * @return {@code List<Template>}
     */
    public List<Template> getAllTemplates() throws DBException
    {

        try
        {
            ResultSet rs = dbManager.executeQuery2( "select info from template_registry_info" );

            return getTemplatesFromResultSet( rs );
        }
        catch ( JsonSyntaxException ex )
        {
            LOG.error( "Error in getAllTemplates", ex );
            throw new DBException( String.format( "Error in getAllTemplates %s", ex ) );
        }
    }


    /**
     * Returns child templates of supplied parent
     *
     * @param parentTemplateName - name of parent template
     * @param lxcArch - lxc arch of template
     *
     * @return {@code List<Template>}
     */
    public List<Template> geChildTemplates( String parentTemplateName, String lxcArch ) throws DBException
    {
        if ( parentTemplateName != null && lxcArch != null )
        {
            try
            {
                ResultSet rs = dbManager.executeQuery2( "select info from template_registry_info where parent = ?",
                        String.format( KEY_FORMAT, parentTemplateName.toLowerCase(), lxcArch.toLowerCase() ) );

                return getTemplatesFromResultSet( rs );
            }
            catch ( JsonSyntaxException ex )
            {
                LOG.error( "Error in geChildTemplates", ex );
                throw new DBException( String.format( "Error in geChildTemplates %s", ex ) );
            }
        }
        return Collections.emptyList();
    }


    private List<Template> getTemplatesFromResultSet( ResultSet rs )
    {
        List<Template> list = new ArrayList<>();
        if ( rs != null )
        {
            for ( Row row : rs )
            {
                String info = row.getString( "info" );
                Template template = GSON.fromJson( info, Template.class );
                if ( template != null )
                {

                    list.add( template );
                }
            }
        }
        return list;
    }


    /**
     * Returns template by name
     *
     * @param templateName - template name
     * @param lxcArch -- lxc arch of template
     *
     * @return {@code Template}
     */
    public Template getTemplateByName( String templateName, String lxcArch ) throws DBException
    {
        if ( templateName != null && lxcArch != null )
        {
            try
            {
                ResultSet rs = dbManager.executeQuery2( "select info from template_registry_info where template = ?",
                        String.format( KEY_FORMAT, templateName.toLowerCase(), lxcArch.toLowerCase() ) );

                List<Template> list = getTemplatesFromResultSet( rs );
                if ( !list.isEmpty() )
                {
                    return list.iterator().next();
                }
            }
            catch ( JsonSyntaxException ex )
            {
                LOG.error( "Error in getTemplateByName", ex );
                throw new DBException( String.format( "Error in getTemplateByName %s", ex ) );
            }
        }
        return null;
    }


    /**
     * Saves template to database
     *
     * @param template - template to save
     */
    public void saveTemplate( Template template ) throws DBException
    {

        dbManager.executeUpdate2( "insert into template_registry_info(template, parent, info) values(?,?,?)",
                String.format( KEY_FORMAT, template.getTemplateName().toLowerCase(),
                        template.getLxcArch().toLowerCase() ),
                Strings.isNullOrEmpty( template.getParentTemplateName() ) ? null :
                String.format( KEY_FORMAT, template.getParentTemplateName().toLowerCase(),
                        template.getLxcArch().toLowerCase() ), GSON.toJson( template ) );
    }


    /**
     * Deletes template from database
     *
     * @param template - template to delete
     */
    public void removeTemplate( Template template ) throws DBException
    {

        dbManager.executeUpdate2( "delete from template_registry_info where template = ?",
                String.format( KEY_FORMAT, template.getTemplateName().toLowerCase(),
                        template.getLxcArch().toLowerCase() ) );
    }
}
