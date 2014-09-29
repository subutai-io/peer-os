package org.safehaus.subutai.core.git.impl;


import org.junit.Test;
import org.safehaus.subutai.core.git.api.GitBranch;


/**
 * Test for GitBranch class
 */
public class GitBranchTest
{

    @Test( expected = IllegalArgumentException.class )
    public void constructorShouldFailOnNullName()
    {
        new GitBranch( null, false );
    }


}
