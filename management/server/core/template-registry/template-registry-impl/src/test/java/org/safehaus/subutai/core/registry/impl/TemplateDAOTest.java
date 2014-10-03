package org.safehaus.subutai.core.registry.impl;


import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.core.db.api.DBException;
import org.safehaus.subutai.core.db.api.DbManager;
import org.safehaus.subutai.core.registry.api.Template;

import com.datastax.driver.core.ResultSet;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;


/**
 * Created by talas on 9/30/14.
 */
public class TemplateDAOTest
{
    private DbManager dbManager;
    private TemplateDAO templateDAO;


    @Before
    public void setupClasses() throws DBException
    {
        dbManager = mock( DbManager.class );
        ResultSet rs = mock( ResultSet.class );
        Iterator it = mock( Iterator.class );
        when( it.hasNext() ).thenReturn( false );
        when( rs.iterator() ).thenReturn( it );
        when( dbManager.executeQuery2( any( String.class ), anyVararg() ) ).thenReturn( rs );
        templateDAO = new TemplateDAO( dbManager );
    }


    @Test
    public void shouldCallDbManagerExecuteQuery2OnGetAllTemplates() throws DBException
    {
        templateDAO.getAllTemplates();
        verify( dbManager ).executeQuery2( any( String.class ), anyVararg() );
    }


    @Test
    public void shouldCallDbManagerExecuteQuery2OnGetChildTemplates() throws DBException
    {
        templateDAO.getChildTemplates( "parentTemplateName", "lxcArch" );
        verify( dbManager ).executeQuery2( any( String.class ), anyVararg() );
    }


    @Test
    public void shouldCallDbManagerExecuteQuery2OnGetTemplateByName() throws DBException
    {
        templateDAO.getTemplateByName( "templateName", "lxcArch" );
        verify( dbManager ).executeQuery2( any( String.class ), anyVararg() );
    }


    @Test
    public void shouldCallDbManagerExecuteUpdate2OnSaveTemplate() throws DBException
    {
        Template template = mock( Template.class, withSettings().serializable() );
        when( template.getLxcArch() ).thenReturn( "lxcArch" );
        when( template.getParentTemplateName() ).thenReturn( "ParentTempalteName" );
        when( template.getTemplateName() ).thenReturn( "TempalteName" );

        templateDAO.saveTemplate( template );
        verify( dbManager ).executeUpdate2( any( String.class ), anyVararg() );
    }


    @Test
    public void shouldCallDbManagerExecuteUpdate2OnRemoveTemplate() throws DBException
    {
        Template template = mock( Template.class, withSettings().serializable() );
        when( template.getLxcArch() ).thenReturn( "lxcArch" );
        when( template.getParentTemplateName() ).thenReturn( "ParentTempalteName" );
        when( template.getTemplateName() ).thenReturn( "TempalteName" );

        templateDAO.removeTemplate( template );
        verify( dbManager ).executeUpdate2( any( String.class ), anyVararg() );
    }
}
