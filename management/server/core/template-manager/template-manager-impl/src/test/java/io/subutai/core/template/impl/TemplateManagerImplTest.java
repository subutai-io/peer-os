package io.subutai.core.template.impl;


import java.util.Set;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import org.apache.cxf.jaxrs.client.WebClient;

import io.subutai.common.protocol.Template;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;


@RunWith( MockitoJUnitRunner.class )
public class TemplateManagerImplTest
{

    private final static String TEMPLATE_ID = "public.c2deca182fe8cb8e747065b6eda5920b";
    private final static String TEMPLATE_NAME = "httpd";
    private final static String GORJUN_OUTPUT = String.format( "[{\"id\":\"%s\","
                    + "\"name\":\"%s\"},{\"id\":\"public.6cc434a73bf7df6e9d2b8f0cf3feacec\",\"name\":\"rabbitmq\"}]",
            TEMPLATE_ID, TEMPLATE_NAME );
    private TemplateManagerImpl templateManager;

    @Mock
    WebClient webClient;
    @Mock
    Response response;


    @Before
    public void setUp() throws Exception
    {

        templateManager = spy( new TemplateManagerImpl() );

        doReturn( webClient ).when( templateManager ).getWebClient( anyString() );
        doReturn( response ).when( webClient ).get();
        doReturn( GORJUN_OUTPUT ).when( response ).readEntity( String.class );
    }


    @Test
    public void testGetTemplates() throws Exception
    {
        Set<Template> templates = templateManager.getTemplates();

        assertFalse( templates.isEmpty() );
    }


    @Test
    public void testGetTemplate() throws Exception
    {
        Template template = templateManager.getTemplate( TEMPLATE_ID );

        assertNotNull( template );
    }


    @Test
    public void testGetTemplateByName() throws Exception
    {

        Template template = templateManager.getTemplateByName( TEMPLATE_NAME );

        assertNotNull( template );
    }
}
