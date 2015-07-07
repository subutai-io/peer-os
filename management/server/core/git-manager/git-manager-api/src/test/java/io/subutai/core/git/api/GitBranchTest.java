package io.subutai.core.git.api;


import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import io.subutai.core.git.api.GitBranch;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;


/**
 * Test for GitBranch class
 */
public class GitBranchTest
{
    private static final String LOCAL_BRANCH_NAME = "branch";
    private static final String REMOTE_BRANCH_NAME = "origin/branch";


    @Test( expected = IllegalArgumentException.class )
    public void constructorShouldFailOnNullName()
    {
        new GitBranch( null, false );
    }


    @Test
    public void shouldReturnLocalBranch()
    {
        GitBranch gitBranch = new GitBranch( LOCAL_BRANCH_NAME, false );

        assertEquals( LOCAL_BRANCH_NAME, gitBranch.getName() );

        assertFalse( gitBranch.isRemote() );
    }


    @Test
    public void shouldReturnRemoteBranch()
    {
        GitBranch gitBranch = new GitBranch( REMOTE_BRANCH_NAME, false );

        assertEquals( REMOTE_BRANCH_NAME, gitBranch.getName() );

        assertTrue( gitBranch.isRemote() );
    }


    @Test
    public void shouldReturnCurrentBranch()
    {
        GitBranch gitBranch = new GitBranch( LOCAL_BRANCH_NAME, true );


        assertTrue( gitBranch.isCurrent() );
    }


    @Test
    public void shouldBeEqual()
    {
        GitBranch gitBranch1 = new GitBranch( LOCAL_BRANCH_NAME, true );
        GitBranch gitBranch2 = new GitBranch( LOCAL_BRANCH_NAME, true );
        GitBranch gitBranch3 = new GitBranch( LOCAL_BRANCH_NAME, false );


        assertEquals( gitBranch1, gitBranch2 );
        assertFalse( gitBranch1.equals( gitBranch3 ) );
        Assert.assertFalse( gitBranch1.equals( new Object() ) );
    }


    @Test
    public void shouldReturnNameInToString()
    {
        GitBranch gitBranch1 = new GitBranch( LOCAL_BRANCH_NAME, true );


        assertThat( gitBranch1.toString(), containsString( LOCAL_BRANCH_NAME ) );
    }


    @Test
    public void checkHashCode()
    {
        GitBranch gitBranch1 = new GitBranch( LOCAL_BRANCH_NAME, true );


        Map<GitBranch, GitBranch> map = new HashMap<>();
        map.put( gitBranch1, gitBranch1 );

        assertEquals( map.get( gitBranch1 ), gitBranch1 );
    }
}
