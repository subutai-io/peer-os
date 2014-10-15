package org.safehaus.subutai.core.registry.impl;


import java.lang.reflect.Type;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.sql.DataSource;

import org.safehaus.subutai.common.util.DbUtil;
import org.safehaus.subutai.common.protocol.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;


/**
 * Provides Data Access API for templates
 */
public class TemplateDAO
{
    private static final Logger LOG = LoggerFactory.getLogger( TemplateDAO.class.getName() );
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    protected DbUtil dbUtil;


    public TemplateDAO( final DataSource dataSource ) throws DaoException
    {
        Preconditions.checkNotNull( dataSource, "Data source is null" );

        this.dbUtil = new DbUtil( dataSource );
        setupDb();
    }


    protected void setupDb() throws DaoException
    {
        String sql = "create table if not exists template_registry_info ( template varchar(100), arch varchar(10), "
                + "parent varchar(100), info clob, PRIMARY KEY (template, arch) );";
        try
        {
            dbUtil.update( sql );
        }
        catch ( SQLException e )
        {
            throw new DaoException( e );
        }
    }


    /**
     * Returns all registered templates from database
     *
     * @return {@code List<Template>}
     */
    public List<Template> getAllTemplates() throws DaoException
    {
        try
        {
            ResultSet rs = dbUtil.select( "select info from template_registry_info" );

            return getTemplatesFromResultSet( rs );
        }
        catch ( SQLException | JsonSyntaxException ex )
        {
            LOG.error( "Error in getAllTemplates", ex );
            throw new DaoException( ex );
        }
    }


    private List<Template> getTemplatesFromResultSet( ResultSet rs ) throws SQLException
    {
        List<Template> list = new ArrayList<>();

        while ( rs != null && rs.next() )
        {
            Clob infoClob = rs.getClob( "info" );
            if ( infoClob != null && infoClob.length() > 0 )
            {
                String info = infoClob.getSubString( 1, ( int ) infoClob.length() );
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
     * Returns child templates of supplied parent
     *
     * @param parentTemplateName - name of parent template
     * @param lxcArch - lxc arch of template
     *
     * @return {@code List<Template>}
     */
    public List<Template> getChildTemplates( String parentTemplateName, String lxcArch ) throws DaoException
    {
        if ( parentTemplateName != null && lxcArch != null )
        {
            try
            {
                ResultSet rs = dbUtil.select( "select info from template_registry_info where parent = ? and arch = ?",
                        parentTemplateName.toLowerCase(), lxcArch.toLowerCase() );

                return getTemplatesFromResultSet( rs );
            }
            catch ( SQLException | JsonSyntaxException ex )
            {
                LOG.error( "Error in getChildTemplates", ex );
                throw new DaoException( ex );
            }
        }
        return Collections.emptyList();
    }


    /**
     * Returns template by name
     *
     * @param templateName - template name
     * @param lxcArch -- lxc arch of template
     *
     * @return {@code Template}
     */
    public Template getTemplateByName( String templateName, String lxcArch ) throws DaoException
    {
        if ( templateName != null && lxcArch != null )
        {
            try
            {
                ResultSet rs = dbUtil.select( "select info from template_registry_info where template = ? and arch = ?",
                        templateName.toLowerCase(), lxcArch.toLowerCase() );
                List<Template> list = getTemplatesFromResultSet( rs );
                if ( !list.isEmpty() )
                {
                    return list.iterator().next();
                }
            }
            catch ( SQLException | JsonSyntaxException ex )
            {
                LOG.error( "Error in getTemplateByName", ex );
                throw new DaoException( ex );
            }
        }
        return null;
    }


    /**
     * Saves template to database
     *
     * @param template - template to save
     */
    public void saveTemplate( Template template ) throws DaoException
    {
        Type templateType = new TypeToken<Template>()
        {}.getType();
        try
        {
            dbUtil.update( "merge into template_registry_info(template, arch, parent, info) values(?,?,?,?)",
                    template.getTemplateName().toLowerCase(), template.getLxcArch().toLowerCase(),
                    Strings.isNullOrEmpty( template.getParentTemplateName() ) ? null :
                    template.getParentTemplateName().toLowerCase(), GSON.toJson( template, templateType ) );
        }
        catch ( SQLException e )
        {
            LOG.error( "Error in saveTemplate", e );
            throw new DaoException( e );
        }
    }


    /**
     * Deletes template from database
     *
     * @param template - template to delete
     */
    public void removeTemplate( Template template ) throws DaoException
    {
        try
        {
            dbUtil.update( "delete from template_registry_info where template = ? and arch = ?",
                    template.getTemplateName().toLowerCase(), template.getLxcArch().toLowerCase() );
        }
        catch ( SQLException e )
        {
            LOG.error( "Error in removeTemplate", e );
            throw new DaoException( e );
        }
    }
}
