package io.subutai.common.environment;


import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import io.subutai.common.network.NetworkResource;
import io.subutai.common.task.CloneRequest;
import io.subutai.common.task.CloneResponse;
import io.subutai.common.util.HostUtil;


public class CreateEnvironmentContainersResponse
{
    @JsonProperty( value = "messages" )
    private Set<String> messages = Sets.newHashSet();

    @JsonProperty( value = "responses" )
    private Set<CloneResponse> responses = Sets.newHashSet();

    @JsonProperty( value = "hasSucceeded" )
    private boolean hasSucceeded;


    public CreateEnvironmentContainersResponse( @JsonProperty( value = "messages" ) final Set<String> messages,
                                                @JsonProperty( value = "responses" ) final Set<CloneResponse> responses,
                                                @JsonProperty( value = "hasSucceeded" ) final boolean hasSucceeded )
    {
        this.messages = messages;
        this.responses = responses;
        this.hasSucceeded = hasSucceeded;
    }


    public CreateEnvironmentContainersResponse( HostUtil.Results results, NetworkResource networkResource )
    {
        Preconditions.checkNotNull( results );


        for ( HostUtil.Task task : results.getTasks().getTasks() )
        {
            CloneContainerTask cloneContainerTask = ( CloneContainerTask ) task;
            CloneRequest request = cloneContainerTask.getRequest();

            if ( task.getTaskState() == HostUtil.Task.TaskState.SUCCEEDED )
            {
                responses.add( new CloneResponse( task.getHost().getId(), request.getHostname(),
                        request.getContainerName(), request.getTemplateId(), request.getTemplateArch(), request.getIp(),
                        cloneContainerTask.getResult(), task.getDuration(), request.getContainerQuota(),
                        networkResource.getVlan() ) );

                this.messages.add( String
                        .format( "Task (%s) succeeded on host %s [%s]", task.name(), task.getHost().getHostname(),
                                task.getDurationFormatted() ) );
            }
            else if ( task.getTaskState() == HostUtil.Task.TaskState.FAILED )
            {
                this.messages.add( String
                        .format( "Task (%s) failed on host %s [%s]", task.name(), task.getHost().getHostname(),
                                task.getFailureReason() ) );
            }
            else
            {
                this.messages.add( String.format( "Task (%s) is %s on host %s [%s]", task.name(), task.getTaskState(),
                        task.getHost().getHostname(), task.getDurationFormatted() ) );
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
}
