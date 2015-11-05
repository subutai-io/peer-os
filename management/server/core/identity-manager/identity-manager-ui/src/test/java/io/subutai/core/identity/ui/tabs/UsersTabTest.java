package io.subutai.core.identity.ui.tabs;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.model.User;

import com.vaadin.data.util.BeanItem;


@RunWith( MockitoJUnitRunner.class )
public class UsersTabTest
{
    private UsersTab usersTab;

    @Mock
    IdentityManager identityManager;
    @Mock
    BeanItem<User> beanItem;

    @Before
    public void setUp() throws Exception
    {
        usersTab = new UsersTab( identityManager );
    }


    @Test
    public void testSaveOperation() throws Exception
    {
        usersTab.saveOperation( beanItem, true );
        usersTab.saveOperation( beanItem, false );
    }


    @Test
    public void testRemoveOperation() throws Exception
    {
        usersTab.removeOperation( beanItem, true );
        usersTab.removeOperation( beanItem, false );
    }


    @Test
    public void testCancelOperation() throws Exception
    {
        usersTab.cancelOperation();
    }
}