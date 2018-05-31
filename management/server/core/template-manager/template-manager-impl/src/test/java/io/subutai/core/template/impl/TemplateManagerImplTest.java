package io.subutai.core.template.impl;


import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;

import com.google.common.cache.LoadingCache;

import io.subutai.common.protocol.Templat;
import io.subutai.core.identity.api.IdentityManager;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;


@RunWith( MockitoJUnitRunner.class )
public class TemplateManagerImplTest
{

    private final static String TEMPLATE_ID = "QmTRTLq1L4AnqvQy85zc8LpC3dys9jtWjScgBRafBnf5Hi";
    private final static String TEMPLATE_NAME = "test-template";
    private final static String LIST_OUTPUT = String.format( "[{\"id"
                    + "\":\"QmTRTLq1L4AnqvQy85zc8LpC3dys9jtWjScgBRafBnf5Hi\",\"name\":\"test-template\","
                    + "\"md5\":\"11a7826d014ad8e73554eccfa51bfbc1\",\"owner\":\"dilshat\",\"version\":\"1.0.0\","
                    + "\"size\":674862}]",
            TEMPLATE_ID, TEMPLATE_NAME );
    private TemplateManagerImpl templateManager;

    @Mock
    CloseableHttpClient webClient;
    @Mock
    CloseableHttpResponse response;
    @Mock
    LoadingCache<String, Templat> cache;
    @Mock
    Templat template;
    @Mock
    IdentityManager identityManager;


    @Before
    public void setUp() throws Exception
    {

        templateManager = spy( new TemplateManagerImpl( identityManager ) );

        doReturn( webClient ).when( templateManager ).getHttpsClient();
        doReturn( response ).when( webClient ).execute( any() );
        doReturn( null ).when( cache ).get( TEMPLATE_NAME );
        doReturn( LIST_OUTPUT ).when( templateManager ).readContent( response );
    }


    @Test
    public void testGetTemplates() throws Exception
    {
        Set<Templat> templates = templateManager.getTemplates();

        assertFalse( templates.isEmpty() );
    }


    @Test
    public void testGetTemplate() throws Exception
    {
        Templat template = templateManager.getTemplate( TEMPLATE_ID );

        assertNotNull( template );
    }


    @Test
    public void testGetTemplateByName() throws Exception
    {
        Templat template = templateManager.getTemplateByName( TEMPLATE_NAME );

        assertNotNull( template );
    }
}
