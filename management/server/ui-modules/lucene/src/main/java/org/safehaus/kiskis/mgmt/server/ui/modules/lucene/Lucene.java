package org.safehaus.kiskis.mgmt.server.ui.modules.lucene;

import com.vaadin.ui.*;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskRunner;

import org.safehaus.kiskis.mgmt.server.ui.modules.lucene.view.ModuleComponent;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;

public class Lucene implements Module {

    private static final String MODULE_NAME = "Lucene";
    private static TaskRunner taskRunner;

    public void setTaskRunner(TaskRunner taskRunner) {
        Lucene.taskRunner = taskRunner;
    }

    public static TaskRunner getTaskRunner() {
        return taskRunner;
    }

    @Override
    public String getName() {
        return MODULE_NAME;
    }

    @Override
    public Component createComponent() {
        return new ModuleComponent(MODULE_NAME);
    }
}
