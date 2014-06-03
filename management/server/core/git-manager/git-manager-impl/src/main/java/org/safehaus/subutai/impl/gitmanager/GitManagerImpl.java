package org.safehaus.subutai.impl.gitmanager;


import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.safehaus.subutai.api.commandrunner.AgentResult;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.commandrunner.CommandRunner;
import org.safehaus.subutai.api.commandrunner.RequestBuilder;
import org.safehaus.subutai.api.gitmanager.GitBranch;
import org.safehaus.subutai.api.gitmanager.GitCommand;
import org.safehaus.subutai.api.gitmanager.GitException;
import org.safehaus.subutai.api.gitmanager.GitManager;
import org.safehaus.subutai.shared.protocol.Agent;

import com.google.common.collect.Sets;


/**
 * This is an implementation of GitManager interface
 */
public class GitManagerImpl implements GitManager {

    private final String GIT_REPO_URL = "git@10.10.10.1:/opt/git/project.git";
    private final String MASTER_BRANCH = "master";
    private final String LINE_SEPARATOR = "\n";
    private CommandRunner commandRunner;


    public GitManagerImpl( final CommandRunner commandRunner ) {
        this.commandRunner = commandRunner;
    }


    public void init() {}


    public void destroy() {}


    @Override
    public void init( final Agent host, final String repositoryRoot ) throws GitException {
        Command initCommand = commandRunner
                .createCommand( new RequestBuilder( "git init" ).withCwd( repositoryRoot ), Sets.newHashSet( host ) );

        runCommand( initCommand, host, GitCommand.INIT );
    }


    private void runCommand( Command command, Agent host, GitCommand gitCommand ) throws GitException {
        commandRunner.runCommand( command );

        if ( !command.hasSucceeded() ) {
            if ( command.hasCompleted() ) {
                AgentResult agentResult = command.getResults().get( host.getUuid() );
                throw new GitException(
                        String.format( "Error while performing [git %s]: %s\n%s, exit code %s", gitCommand.getCommand(),
                                agentResult.getStdOut(), agentResult.getStdErr(), agentResult.getExitCode() ) );
            }
            else {
                throw new GitException( String.format( "Error while performing [git %s]: Command timed out",
                        gitCommand.getCommand() ) );
            }
        }
    }


    @Override
    public void add( final Agent host, final String repositoryRoot, final List<String> filePaths ) throws GitException {
        Command addCommand = commandRunner
                .createCommand( new RequestBuilder( "git add" ).withCwd( repositoryRoot ).withCmdArgs( filePaths ),
                        Sets.newHashSet( host ) );

        runCommand( addCommand, host, GitCommand.ADD );
    }


    @Override
    public void delete( final Agent host, final String repositoryRoot, final List<String> filePaths )
            throws GitException {
        Command addCommand = commandRunner
                .createCommand( new RequestBuilder( "git rm" ).withCwd( repositoryRoot ).withCmdArgs( filePaths ),
                        Sets.newHashSet( host ) );

        runCommand( addCommand, host, GitCommand.DELETE );
    }


    @Override
    public String commit( final Agent host, final String repositoryRoot, final List<String> filePaths,
                          final String message ) throws GitException {
        Command addCommand = commandRunner.createCommand(
                new RequestBuilder( String.format( "git commit -m \"%s\"", message ) ).withCwd( repositoryRoot )
                                                                                  .withCmdArgs( filePaths ),
                Sets.newHashSet( host ) );

        runCommand( addCommand, host, GitCommand.COMMIT );
        //parse output to get commit id here
        Pattern p = Pattern.compile( "(\\w+)]" );
        Matcher m = p.matcher( addCommand.getResults().get( host.getUuid() ).getStdOut() );

        if ( m.find() ) {
            return m.group( 1 );
        }

        return null;
    }


    @Override
    public String commit( final Agent host, final String repositoryRoot, final String message ) throws GitException {
        Command addCommand = commandRunner.createCommand(
                new RequestBuilder( String.format( "git commit -a -m \"%s\"", message ) ).withCwd( repositoryRoot ),
                Sets.newHashSet( host ) );

        runCommand( addCommand, host, GitCommand.COMMIT );

        //parse output to get commit id here
        Pattern p = Pattern.compile( "(\\w+)]" );
        Matcher m = p.matcher( addCommand.getResults().get( host.getUuid() ).getStdOut() );

        if ( m.find() ) {
            return m.group( 1 );
        }

        return null;
    }


    @Override
    public void clone( final Agent host, final String newBranchName, final String targetDir ) throws GitException {
        Command cloneCommand = commandRunner.createCommand(
                new RequestBuilder( String.format( "git clone -b %s %s %s", newBranchName, GIT_REPO_URL, targetDir ) )
                        .withTimeout( 180 ), Sets.newHashSet( host ) );

        runCommand( cloneCommand, host, GitCommand.CLONE );
    }


    @Override
    public void checkout( final Agent host, final String repositoryRoot, final String branchName, final boolean create )
            throws GitException {

        String command = create ? String.format( "git checkout --track -b %s", branchName ) :
                         String.format( "git checkout %s", branchName );
        Command checkoutCommand = commandRunner
                .createCommand( new RequestBuilder( command ).withCwd( repositoryRoot ), Sets.newHashSet( host ) );

        runCommand( checkoutCommand, host, GitCommand.CHECKOUT );
    }


    @Override
    public void deleteBranch( final Agent host, final String repositoryRoot, final String branchName )
            throws GitException {
        Command checkoutCommand = commandRunner.createCommand(
                new RequestBuilder( String.format( "git branch -d %s", branchName ) ).withCwd( repositoryRoot ),
                Sets.newHashSet( host ) );

        runCommand( checkoutCommand, host, GitCommand.BRANCH );
    }


    @Override
    public void merge( final Agent host, final String repositoryRoot ) throws GitException {
        merge( host, repositoryRoot, MASTER_BRANCH );
    }


    @Override
    public void merge( final Agent host, final String repositoryRoot, final String branchName ) throws GitException {
        Command mergeCommand = commandRunner.createCommand(
                new RequestBuilder( String.format( "git merge %s", branchName ) ).withCwd( repositoryRoot ),
                Sets.newHashSet( host ) );

        runCommand( mergeCommand, host, GitCommand.MERGE );
    }


    @Override
    public void pull( final Agent host, final String repositoryRoot, final String branchName ) throws GitException {
        Command pullCommand = commandRunner.createCommand(
                new RequestBuilder( String.format( "git pull origin %s", branchName ) ).withCwd( repositoryRoot ),
                Sets.newHashSet( host ) );

        runCommand( pullCommand, host, GitCommand.PULL );
    }


    @Override
    public void pull( final Agent host, final String repositoryRoot ) throws GitException {
        pull( host, repositoryRoot, MASTER_BRANCH );
    }


    @Override
    public GitBranch currentBranch( final Agent host, final String repositoryRoot ) throws GitException {
        Command branchCommand = commandRunner
                .createCommand( new RequestBuilder( "git branch" ).withCwd( repositoryRoot ), Sets.newHashSet( host ) );

        runCommand( branchCommand, host, GitCommand.BRANCH );

        branchCommand.getResults().get( host.getUuid() ).getStdOut();

        StringTokenizer lines =
                new StringTokenizer( branchCommand.getResults().get( host.getUuid() ).getStdOut(), LINE_SEPARATOR );

        while ( lines.hasMoreTokens() ) {
            String line = lines.nextToken();

            if ( line != null && line.startsWith( "*" ) ) {
                return new GitBranch( line.substring( 2 ), true );
            }
        }

        return new GitBranch( MASTER_BRANCH, true );
    }


    @Override
    public List<GitBranch> listBranches( final Agent host, final String repositoryRoot, boolean remote )
            throws GitException {
        List<GitBranch> branches = new LinkedList<>();

        Command branchCommand = commandRunner
                .createCommand( new RequestBuilder( remote ? "git branch -r" : "git branch" ).withCwd( repositoryRoot ),
                        Sets.newHashSet( host ) );

        runCommand( branchCommand, host, GitCommand.BRANCH );

        StringTokenizer lines =
                new StringTokenizer( branchCommand.getResults().get( host.getUuid() ).getStdOut(), LINE_SEPARATOR );

        while ( lines.hasMoreTokens() ) {
            String line = lines.nextToken();

            if ( line != null ) {
                branches.add( new GitBranch( line.substring( 2 ), line.startsWith( "*" ) ) );
            }
        }

        return branches;
    }


    @Override
    public void push( final Agent host, final String repositoryRoot ) throws GitException {
        push( host, repositoryRoot, MASTER_BRANCH );
    }


    @Override
    public void push( final Agent host, final String repositoryRoot, final String branchName ) throws GitException {
        Command pushCommand = commandRunner.createCommand(
                new RequestBuilder( String.format( "git push origin %s", branchName ) ).withCwd( repositoryRoot ),
                Sets.newHashSet( host ) );

        runCommand( pushCommand, host, GitCommand.PUSH );
    }


    @Override
    public void undoSoft( final Agent host, final String repositoryRoot, final List<String> filePaths )
            throws GitException {
        Command undoCommand = commandRunner.createCommand(
                new RequestBuilder( "git checkout --" ).withCwd( repositoryRoot ).withCmdArgs( filePaths ),
                Sets.newHashSet( host ) );

        runCommand( undoCommand, host, GitCommand.CHECKOUT );
    }


    @Override
    public void undoHard( final Agent host, final String repositoryRoot, final String branchName ) throws GitException {
        Command undoCommand = commandRunner.createCommand(
                new RequestBuilder( String.format( "git fetch origin && git reset --hard origin/%s", branchName ) )
                        .withCwd( repositoryRoot ), Sets.newHashSet( host ) );

        runCommand( undoCommand, host, GitCommand.FETCH );
    }


    @Override
    public void revertCommit( final Agent host, final String repositoryRoot, String commitId ) throws GitException {
        Command revertCommand = commandRunner.createCommand(
                new RequestBuilder( String.format( "git revert %s", commitId ) ).withCwd( repositoryRoot ),
                Sets.newHashSet( host ) );

        runCommand( revertCommand, host, GitCommand.REVERT );
    }


    @Override
    public void stash( final Agent host, final String repositoryRoot, final String stashName ) throws GitException {
        Command stashCommand = commandRunner.createCommand(
                new RequestBuilder( String.format( "git stash %s", stashName ) ).withCwd( repositoryRoot ),
                Sets.newHashSet( host ) );

        runCommand( stashCommand, host, GitCommand.STASH );
    }


    @Override
    public void unstash( final Agent host, final String repositoryRoot, final String stashName ) throws GitException {
        Command stashCommand = commandRunner.createCommand(
                new RequestBuilder( String.format( "git stash apply %s", stashName ) ).withCwd( repositoryRoot ),
                Sets.newHashSet( host ) );

        runCommand( stashCommand, host, GitCommand.STASH );
    }


    @Override
    public List<String> listStashes( final Agent host, final String repositoryRoot ) throws GitException {

        List<String> stashes = new LinkedList<>();

        Command stashCommand = commandRunner
                .createCommand( new RequestBuilder( "git stash list" ).withCwd( repositoryRoot ),
                        Sets.newHashSet( host ) );

        runCommand( stashCommand, host, GitCommand.STASH );

        StringTokenizer tok =
                new StringTokenizer( stashCommand.getResults().get( host.getUuid() ).getStdOut(), LINE_SEPARATOR );

        while ( tok.hasMoreTokens() ) {
            stashes.add( tok.nextToken() );
        }

        return stashes;
    }
}
