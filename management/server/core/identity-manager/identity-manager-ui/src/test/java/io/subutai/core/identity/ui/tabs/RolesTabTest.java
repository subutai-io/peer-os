package io.subutai.core.identity.ui.tabs;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.Role;
import io.subutai.core.identity.ui.tabs.RolesTab;

import com.vaadin.data.util.BeanItem;


@RunWith( MockitoJUnitRunner.class )
public class RolesTabTest
{
    private RolesTab rolesTab;
    
    @Mock
    IdentityManager identityManager;
    @Mock
    BeanItem<Role> beanItem;

    @Before
    public void setUp() throws Exception
    {
        rolesTab = new RolesTab( identityManager );
    }


    @Test
    public void testSaveOperation() throws Exception
    {
        rolesTab.saveOperation( beanItem, true );
        rolesTab.saveOperation( beanItem, false );
    }


    @Test
    public void testRemoveOperation() throws Exception
    {
        rolesTab.removeOperation( beanItem, true );
        rolesTab.removeOperation( beanItem, false );
    }


    @Test
    public void testCancelOperation() throws Exception
    {
        rolesTab.cancelOperation();
    }
}