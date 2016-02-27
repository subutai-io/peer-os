package io.subutai.common.task;


import java.util.List;

import io.subutai.common.tracker.OperationMessage;


public interface TaskResponse
{
    List<OperationMessage> getOperationMessages();
}
