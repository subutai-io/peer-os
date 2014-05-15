package org.safehaus.kiskis.mgmt.ui.flume.manager;

import com.vaadin.terminal.Sizeable;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.*;

import java.util.Set;
import java.util.UUID;

import org.safehaus.kiskis.mgmt.api.flume.Config;
import org.safehaus.kiskis.mgmt.shared.operation.ProductOperationState;
import org.safehaus.kiskis.mgmt.shared.operation.ProductOperationView;
import org.safehaus.kiskis.mgmt.server.ui.MgmtApplication;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;
import org.safehaus.kiskis.mgmt.ui.flume.FlumeUI;

class AddNodeWindow extends Window {

    private final TextArea outputTxtArea;
    private final Button ok;
    private final Label indicator;
    private volatile boolean track = true;

    public AddNodeWindow(final Config config, Set<Agent> nodes) {
        super("Add New Node");
        setModal(true);
        setClosable(false);

        setWidth(600, AddNodeWindow.UNITS_PIXELS);

        GridLayout content = new GridLayout(1, 3);
        content.setSizeFull();
        content.setMargin(true);
        content.setSpacing(true);

        HorizontalLayout topContent = new HorizontalLayout();
        topContent.setSpacing(true);

        content.addComponent(topContent);
        topContent.addComponent(new Label("Nodes:"));

        final ComboBox hadoopNodes = new ComboBox();
        hadoopNodes.setMultiSelect(false);
        hadoopNodes.setImmediate(true);
        hadoopNodes.setTextInputAllowed(false);
        hadoopNodes.setNullSelectionAllowed(false);
        hadoopNodes.setRequired(true);
        hadoopNodes.setWidth(200, Sizeable.UNITS_PIXELS);
        for (Agent node : nodes) {
            hadoopNodes.addItem(node);
            hadoopNodes.setItemCaption(node, node.getHostname());
        }
        hadoopNodes.setValue(nodes.iterator().next());

        topContent.addComponent(hadoopNodes);

        final Button addNodeBtn = new Button("Add");
        topContent.addComponent(addNodeBtn);

        addNodeBtn.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                addNodeBtn.setEnabled(false);
                showProgress();
                Agent agent = (Agent) hadoopNodes.getValue();
                final UUID trackID = FlumeUI.getManager().addNode(config.getClusterName(), agent.getHostname());
                FlumeUI.getExecutor().execute(new Runnable() {

                    public void run() {
                        while (track) {
                            ProductOperationView po = FlumeUI.getTracker().getProductOperation(Config.PRODUCT_KEY, trackID);
                            if (po != null) {
                                setOutput(po.getDescription() + "\nState: " + po.getState() + "\nLogs:\n" + po.getLog());
                                if (po.getState() != ProductOperationState.RUNNING) {
                                    hideProgress();
                                    break;
                                }
                            } else {
                                setOutput("Product operation not found. Check logs");
                                break;
                            }
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException ex) {
                                break;
                            }
                        }
                    }
                });
            }
        });

        outputTxtArea = new TextArea("Operation output");
        outputTxtArea.setRows(13);
        outputTxtArea.setColumns(43);
        outputTxtArea.setImmediate(true);
        outputTxtArea.setWordwrap(true);

        content.addComponent(outputTxtArea);

        indicator = new Label();
        indicator.setIcon(new ThemeResource("icons/indicator.gif"));
        indicator.setContentMode(Label.CONTENT_XHTML);
        indicator.setHeight(11, Sizeable.UNITS_PIXELS);
        indicator.setWidth(50, Sizeable.UNITS_PIXELS);
        indicator.setVisible(false);

        ok = new Button("Ok");
        ok.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                //close window   
                track = false;
                MgmtApplication.removeCustomWindow(getWindow());
            }
        });

        HorizontalLayout bottomContent = new HorizontalLayout();
        bottomContent.addComponent(indicator);
        bottomContent.setComponentAlignment(indicator, Alignment.MIDDLE_RIGHT);
        bottomContent.addComponent(ok);

        content.addComponent(bottomContent);
        content.setComponentAlignment(bottomContent, Alignment.MIDDLE_RIGHT);

        addComponent(content);
    }

    @Override
    protected void close() {
        super.close();
        track = false;
    }

    private void showProgress() {
        indicator.setVisible(true);
        ok.setEnabled(false);
    }

    private void hideProgress() {
        indicator.setVisible(false);
        ok.setEnabled(true);
    }

    private void setOutput(String output) {
        if (!Util.isStringEmpty(output)) {
            outputTxtArea.setValue(output);
            outputTxtArea.setCursorPosition(outputTxtArea.getValue().toString().length() - 1);
        }
    }

}
