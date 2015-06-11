package org.safehaus.subutai.core.identity.ui.tabs;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.core.identity.api.IdentityManager;
import org.safehaus.subutai.core.identity.api.Permission;

import com.vaadin.data.util.BeanItem;


@RunWith( MockitoJUnitRunner.class )
public class PermissionsTabTest
{
    private PermissionsTab permissionsTab;

    @Mock
    IdentityManager identityManager;
    @Mock
    BeanItem<Permission> beanItem;

    @Before
    public void setUp() throws Exception
    {
        permissionsTab = new PermissionsTab( identityManager );
    }


    @Test
    public void testEditorForm() throws Exception
    {
        permissionsTab.editorForm();
    }


    @Test
    public void testSaveOperation() throws Exception
    {
        permissionsTab.saveOperation( beanItem, true );
        permissionsTab.saveOperation( beanItem, false );
    }


    @Test
    public void testRemoveOperation() throws Exception
    {
        permissionsTab.removeOperation( beanItem, true );
        permissionsTab.removeOperation( beanItem, false );
    }


    @Test
    public void testCancelOperation() throws Exception
    {
        permissionsTab.cancelOperation();
    }
}