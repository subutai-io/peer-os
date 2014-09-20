package org.safehaus.subutai.core.git.api;


import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


/**
 * Represents a git branch
 */
public class GitBranch
{

    /**
     * Name of branch
     */
    private String name;
    /**
     * Indicates whether this branch is current branch
     */
    private boolean current;


    public GitBranch( final String name, final boolean current )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( name ), "Branch name is null or empty" );
        this.name = name;
        this.current = current;
    }


    /**
     * Returns name of this branch
     */
    public String getName()
    {
        return name;
    }


    /**
     * Indicates whether this branch is a current branch
     */
    public boolean isCurrent()
    {
        return current;
    }


    /**
     * Indicates whether this branch is a remote branch
     */
    public boolean isRemote()
    {
        return name.contains( "/" );
    }


    @Override
    public String toString()
    {
        return "GitBranch{" +
                "name='" + name + '\'' +
                ", current=" + current +
                '}';
    }
}
