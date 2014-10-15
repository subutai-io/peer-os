package org.safehaus.subutai.core.registry.impl;


import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.safehaus.subutai.common.util.DbUtil;
import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.core.registry.api.Template;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Test for TemplateDao
 */
public class TemplateDaoTest
{

    private TemplateDAOExt templateDAO;
    private DbUtil dbUtil;
    private static final String TEMPLATE_NAME = "template";
    private static final String ARCH = "arch";


    static class TemplateDAOExt extends TemplateDAO
    {


        TemplateDAOExt( final DataSource dataSource ) throws DaoException
        {
            super( dataSource );
        }


        @Override
        protected void setupDb() throws DaoException
        {
            //disable db setup
        }


        public void testSetupDB() throws DaoException
        {
            super.setupDb();
        }


        public void setDbUtil( DbUtil dbUtil ) {this.dbUtil = dbUtil;}
    }


    @Before
    public void setUp() throws Exception
    {
        templateDAO = new TemplateDAOExt( mock( DataSource.class ) );
        dbUtil = mock( DbUtil.class );
        templateDAO.setDbUtil( dbUtil );
        ResultSet resultSet = mock( ResultSet.class );
        when( dbUtil.select( anyString(), anyVararg() ) ).thenReturn( resultSet );
        when( resultSet.next() ).thenReturn( true ).thenReturn( false );
        Clob clob = mock( Clob.class );
        when( resultSet.getClob( "info" ) ).thenReturn( clob );
        when( clob.length() ).thenReturn( 1L );
        String templateJson = JsonUtil.toJson( TestUtils.getDefaultTemplate() );
        when( clob.getSubString( 1, 1 ) ).thenReturn( templateJson );
    }


    private void throwDbException() throws SQLException
    {
        when( dbUtil.select( anyString(), anyVararg() ) ).thenThrow( new SQLException() );
        when( dbUtil.update( anyString(), anyVararg() ) ).thenThrow( new SQLException() );
    }


    private void returnEmpty() throws SQLException
    {
        when( dbUtil.select( anyString(), anyVararg() ) ).thenReturn( mock( ResultSet.class ) );
    }


    @Test( expected = NullPointerException.class )
    public void constructorShouldFailOnNullDataSource() throws Exception
    {
        new TemplateDAO( null );
    }


    @Test
    public void testGetAllTemplates() throws Exception
    {
        List<Template> allTemplates = templateDAO.getAllTemplates();

        assertTrue( allTemplates.contains( TestUtils.getDefaultTemplate() ) );
    }


    @Test( expected = DaoException.class )
    public void testGetAllTemplatesException() throws Exception
    {
        throwDbException();

        templateDAO.getAllTemplates();
    }


    @Test
    public void testGetChildTemplates() throws Exception
    {
        List<Template> allTemplates = templateDAO.getChildTemplates( TEMPLATE_NAME, ARCH );

        assertTrue( allTemplates.contains( TestUtils.getDefaultTemplate() ) );
    }


    @Test
    public void testGetChildTemplatesEmpty() throws Exception
    {
        returnEmpty();

        List<Template> allTemplates = templateDAO.getChildTemplates( TEMPLATE_NAME, ARCH );

        assertTrue( allTemplates.isEmpty() );
    }


    @Test
    public void testGetChildTemplatesEmpty2() throws Exception
    {

        List<Template> allTemplates = templateDAO.getChildTemplates( TEMPLATE_NAME, null );

        assertTrue( allTemplates.isEmpty() );
    }


    @Test( expected = DaoException.class )
    public void testGetChildTemplatesException() throws Exception
    {
        throwDbException();

        templateDAO.getChildTemplates( TEMPLATE_NAME, ARCH );
    }


    @Test
    public void testGetTemplateByName() throws Exception
    {
        Template template = templateDAO.getTemplateByName( TEMPLATE_NAME, ARCH );

        assertEquals( TestUtils.getDefaultTemplate(), template );
    }


    @Test
    public void testGetTemplateByNameEmpty() throws Exception
    {
        returnEmpty();

        Template template = templateDAO.getTemplateByName( TEMPLATE_NAME, ARCH );

        assertNull( template );
    }


    @Test
    public void testGetTemplateByNameEmpty2() throws Exception
    {

        Template template = templateDAO.getTemplateByName( null, ARCH );

        assertNull( template );
    }


    @Test( expected = DaoException.class )
    public void testGetTemplateByNameException() throws Exception
    {
        throwDbException();

        templateDAO.getTemplateByName( TEMPLATE_NAME, ARCH );
    }


    @Test
    public void testSaveTemplate() throws Exception
    {
        templateDAO.saveTemplate( TestUtils.getDefaultTemplate() );

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass( String.class );
        ArgumentCaptor<String> sqlCaptor2 = ArgumentCaptor.forClass( String.class );
        ArgumentCaptor<String> sqlCaptor3 = ArgumentCaptor.forClass( String.class );
        ArgumentCaptor<String> sqlCaptor4 = ArgumentCaptor.forClass( String.class );
        ArgumentCaptor<String> sqlCaptor5 = ArgumentCaptor.forClass( String.class );
        verify( dbUtil ).update( sqlCaptor.capture(), sqlCaptor2.capture(), sqlCaptor3.capture(), sqlCaptor4.capture(),
                sqlCaptor5.capture() );

        assertEquals( "insert into template_registry_info(template, arch, parent, info) values(?,?,?,?)",
                sqlCaptor.getValue() );
        assertEquals( TestUtils.TEMPLATE_NAME.toLowerCase(), sqlCaptor2.getValue() );
        assertEquals( TestUtils.LXC_ARCH.toLowerCase(), sqlCaptor3.getValue() );
        assertEquals( null, sqlCaptor4.getValue() );
        assertEquals( JsonUtil.toJson( TestUtils.getDefaultTemplate() ), sqlCaptor5.getValue() );
    }


    @Test( expected = DaoException.class )
    public void testSaveTemplateException() throws Exception
    {

        throwDbException();

        templateDAO.saveTemplate( TestUtils.getDefaultTemplate() );
    }


    @Test
    public void testRemoteTemplate() throws Exception
    {
        templateDAO.removeTemplate( TestUtils.getDefaultTemplate() );

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass( String.class );
        ArgumentCaptor<String> sqlCaptor2 = ArgumentCaptor.forClass( String.class );
        ArgumentCaptor<String> sqlCaptor3 = ArgumentCaptor.forClass( String.class );

        verify( dbUtil ).update( sqlCaptor.capture(), sqlCaptor2.capture(), sqlCaptor3.capture() );

        assertEquals( "delete from template_registry_info where template = ? and arch = ?", sqlCaptor.getValue() );
        assertEquals( TestUtils.TEMPLATE_NAME.toLowerCase(), sqlCaptor2.getValue() );
        assertEquals( TestUtils.LXC_ARCH.toLowerCase(), sqlCaptor3.getValue() );
    }


    @Test( expected = DaoException.class )
    public void testRemoteTemplateException() throws Exception
    {

        throwDbException();

        templateDAO.removeTemplate( TestUtils.getDefaultTemplate() );
    }


    @Test
    public void testSetupDb() throws Exception
    {
        templateDAO.testSetupDB();

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass( String.class );


        verify( dbUtil ).update( sqlCaptor.capture() );

        assertEquals( "create table if not exists template_registry_info ( template varchar(100), arch varchar(10), "
                + "parent varchar(100), info clob, PRIMARY KEY (template, arch) );", sqlCaptor.getValue() );
    }


    @Test( expected = DaoException.class )
    public void testSetupDbException() throws Exception
    {

        throwDbException();

        templateDAO.testSetupDB();
    }
}
