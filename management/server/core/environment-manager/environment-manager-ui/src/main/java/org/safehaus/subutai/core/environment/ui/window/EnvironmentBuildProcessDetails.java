package org.safehaus.subutai.core.environment.ui.window;


import org.safehaus.subutai.core.environment.api.helper.ContainerBuildMessage;
import org.safehaus.subutai.core.environment.api.helper.EnvironmentBuildProcess;

import com.vaadin.ui.TextArea;


/**
 * Created by bahadyr on 9/14/14.
 */
public class EnvironmentBuildProcessDetails extends DetailsWindow {

    public EnvironmentBuildProcessDetails(final String caption) {
            super(caption);
        }


        public void setContent(final EnvironmentBuildProcess environmentBuildProcess ) {
            StringBuilder sb = new StringBuilder();
            sb.append( environmentBuildProcess.getUuid()).append( "\n" );
            if ( environmentBuildProcess.getContainerBuildMessages() != null) {
                for (ContainerBuildMessage block : environmentBuildProcess.getContainerBuildMessages()) {
                    sb.append(block.isCompleteState()).append( "\n" );
                }
            }

            TextArea area = getTextArea();
            area.setValue(sb.toString());
            verticalLayout.addComponent(area);
        }


        private TextArea getTextArea() {
            TextArea textArea = new TextArea("Blueprint");
            textArea.setSizeFull();
            textArea.setRows(20);
            textArea.setImmediate(true);
            textArea.setWordwrap(false);
            return textArea;
        }
}
