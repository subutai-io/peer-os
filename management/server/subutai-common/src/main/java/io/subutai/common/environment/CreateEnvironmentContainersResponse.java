package io.subutai.common.environment;


import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import io.subutai.common.task.CloneRequest;
import io.subutai.common.task.CloneResponse;
import io.subutai.common.util.HostUtil;


public class CreateEnvironmentContainersResponse
{
    private Set<String> messages = Sets.newHashSet();
    private Set<CloneResponse> responses = Sets.newHashSet();
    private boolean hasSucceeded;


    public CreateEnvironmentContainersResponse( HostUtil.Results results )
    {
        Preconditions.checkNotNull( results );


        for ( HostUtil.Task task : results.getTasks().getTasks() )
        {
            CloneContainerTask cloneContainerTask = ( CloneContainerTask ) task;
            CloneRequest request = cloneContainerTask.getRequest();

            if ( task.getTaskState() == HostUtil.Task.TaskState.SUCCEEDED )
            {
                responses.add( new CloneResponse( task.getHost().getId(), request.getHostname(),
                        request.getContainerName(), request.getTemplateName(), request.getTemplateArch(),
                        request.getIp(), cloneContainerTask.getResult(), task.getDuration() ) );

                this.messages.add( String
                        .format( "Task (%s) succeeded on host %s [%s]", task.name(), task.getHost().getId(),
                                task.getDurationFormatted() ) );
            }
            else if ( task.getTaskState() == HostUtil.Task.TaskState.FAILED )
            {
                this.messages.add( String
                        .format( "Task (%s) failed on host %s [%s]", task.name(), task.getHost().getId(),
                                task.getFailureReason() ) );
            }
            else
            {
                this.messages.add( String.format( "Task (%s) is %s on host %s [%s]", task.name(), task.getTaskState(),
                        task.getHost().getId(), task.getDurationFormatted() ) );
            }
        }

        this.hasSucceeded = !results.hasFailures();
    }


    public Set<CloneResponse> getResponses()
    {
        return responses;
    }


    public Set<String> getMessages()
    {
        return messages;
    }


    public boolean hasSucceeded()
    {
        return hasSucceeded;
    }


    public CloneResponse findByHostname( final String hostname )
    {
        for ( CloneResponse response : responses )
        {
            if ( response.getHostname().equalsIgnoreCase( hostname ) )
            {
                return response;
            }
        }

        return null;
    }
}
