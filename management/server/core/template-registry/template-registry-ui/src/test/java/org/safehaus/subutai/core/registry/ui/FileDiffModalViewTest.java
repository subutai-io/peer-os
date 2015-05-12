package org.safehaus.subutai.core.registry.ui;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.vaadin.ui.HorizontalLayout;


@RunWith( MockitoJUnitRunner.class )
public class FileDiffModalViewTest
{
    private FileDiffModalView fileDiffModalView;

    @Mock
    HorizontalLayout horizontalLayout;

    @Before
    public void setUp() throws Exception
    {
        fileDiffModalView = new FileDiffModalView( "test", horizontalLayout, "@@-555" );
        fileDiffModalView = new FileDiffModalView( "test", horizontalLayout, "diff --git-555" );
    }

    @Test
    public void test()
    {

    }
}