package org.safehaus.subutai.core.environment.ui.window;


import org.safehaus.subutai.core.environment.api.helper.BuildBlock;
import org.safehaus.subutai.core.environment.api.helper.BuildProcess;

import com.vaadin.ui.TextArea;


/**
 * Created by bahadyr on 9/14/14.
 */
public class EnvironmentBuildProcessDetails extends DetailsWindow {

    public EnvironmentBuildProcessDetails(final String caption) {
            super(caption);
        }


        public void setContent(final BuildProcess buildProcess) {
            StringBuilder sb = new StringBuilder();
            sb.append(buildProcess.getUuid()).append( "\n" );
            if (buildProcess.getBuildBlocks() != null) {
                for (BuildBlock block : buildProcess.getBuildBlocks()) {
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
