package org.safehaus.kiskis.mgmt.ui.sqoop.manager;

import com.vaadin.ui.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.safehaus.kiskis.mgmt.api.sqoop.setting.ExportSetting;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.ui.sqoop.SqoopUI;

public class ExportPanel extends ImportExportBase {

    AbstractTextField hdfsPathField = UIUtil.getTextField("HDFS file path:", 300);

    public ExportPanel() {
        init();
    }

    @Override
    public void setAgent(Agent agent) {
        super.setAgent(agent);
        init();
    }

    @Override
    final void init() {

        removeAllActionHandlers();
        removeAllComponents();

        if(agent == null) {
            addComponent(UIUtil.getLabel("<h1>No node selected</h1>", 200));
            return;
        }

        super.init();
        fields.add(hdfsPathField);

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.addComponent(UIUtil.getButton("Export", 120,
                new Button.ClickListener() {

                    public void buttonClick(Button.ClickEvent event) {
                        clearLogMessages();
                        if(!checkFields()) return;
                        setFieldsEnabled(false);
                        ExportSetting es = makeSettings();
                        final UUID trackId = SqoopUI.getManager().exportData(es);

                        OperationWatcher watcher = new OperationWatcher(trackId);
                        watcher.setCallback(new OperationCallback() {

                            public void onComplete() {
                                setFieldsEnabled(true);
                            }
                        });
                        SqoopUI.getExecutor().execute(watcher);
                    }

                }));
        buttons.addComponent(UIUtil.getButton("Cancel", 120, new Button.ClickListener() {

            public void buttonClick(Button.ClickEvent event) {
                detachFromParent();
            }
        }));

        List<Component> ls = new ArrayList<Component>();
        String title = "Sqoop Export / Hostname: " + agent.getHostname();
        ls.add(UIUtil.getLabel("<h1>" + title + "</h1>", 100, UNITS_PERCENTAGE));
        ls.add(connStringField);
        ls.add(tableField);
        ls.add(usernameField);
        ls.add(passwordField);
        ls.add(hdfsPathField);
        ls.add(buttons);

        addComponents(ls);
    }

    ExportSetting makeSettings() {
        ExportSetting s = new ExportSetting();
        s.setClusterName(clusterName);
        s.setHostname(agent.getHostname());
        s.setConnectionString(connStringField.getValue().toString());
        s.setTableName(tableField.getValue().toString());
        s.setUsername(usernameField.getValue().toString());
        s.setPassword(passwordField.getValue().toString());
        s.setHdfsPath(hdfsPathField.getValue().toString());
        return s;
    }

    @Override
    boolean checkFields() {
        if(super.checkFields()) {
            if(!hasValue(tableField, "Table name not specified"))
                return false;
            if(!hasValue(hdfsPathField, "HDFS file path not specified"))
                return false;
            // every field has value
            return true;
        }
        return false;
    }

}
