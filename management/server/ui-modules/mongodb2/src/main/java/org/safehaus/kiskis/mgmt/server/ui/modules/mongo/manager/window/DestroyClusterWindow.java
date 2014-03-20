/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.mongo.manager.window;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.Window;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.safehaus.kiskis.mgmt.server.ui.MgmtApplication;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.common.Config;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.dao.MongoDAO;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.MongoModule;

/**
 *
 * @author dilshat
 */
public class DestroyClusterWindow extends Window {

    private static final Logger LOG = Logger.getLogger(DestroyClusterWindow.class.getName());

    private final TextArea outputTxtArea;
    private final Button ok;
    private final Label indicator;
    private final Config config;

    public DestroyClusterWindow(Config config) {
        super("Cluster uninstallation");
        setModal(true);
        setClosable(false);

        this.config = config;

        setWidth(650, DestroyClusterWindow.UNITS_PIXELS);

        GridLayout content = new GridLayout(10, 2);
        content.setSizeFull();
        content.setMargin(true);
        content.setSpacing(true);

        outputTxtArea = new TextArea("Operation output");
        outputTxtArea.setRows(13);
        outputTxtArea.setColumns(43);
        outputTxtArea.setImmediate(true);
        outputTxtArea.setWordwrap(true);

        content.addComponent(outputTxtArea, 0, 0, 9, 0);
        ok = new Button("Ok");
        ok.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                //close window   
                MgmtApplication.removeCustomWindow(getWindow());
            }
        });

        indicator = MgmtApplication.createImage("indicator.gif", 50, 11);

        content.addComponent(ok, 9, 1, 9, 1);
        content.addComponent(indicator, 5, 1, 8, 1);
        content.setComponentAlignment(indicator, Alignment.MIDDLE_RIGHT);
        content.setComponentAlignment(ok, Alignment.MIDDLE_LEFT);

        addComponent(content);
    }

    private void start() {
        MongoModule.getExecutor().execute(new Runnable() {

            public void run() {
                if (destroyLxcs()) {
                    addOutput("Lxc containers successfully destroyed");
                } else {
                    addOutput("Not all lxc containers destroyed. Use LXC module to cleanup");
                }
                if (MongoDAO.deleteMongoClusterInfo(config.getClusterName())) {
                    addOutput("Cluster info deleted from DB");
                } else {
                    addOutput("Error while deleting cluster info from DB. Check logs");
                }
                hideProgress();
            }
        });

    }

    private class DestroyInfo {

        private final Agent physicalAgent;
        private final String lxcHostname;
        private boolean result;

        public DestroyInfo(Agent physicalAgent, String lxcHostname) {
            this.physicalAgent = physicalAgent;
            this.lxcHostname = lxcHostname;
        }

        public boolean isResult() {
            return result;
        }

        public void setResult(boolean result) {
            this.result = result;
        }

        public Agent getPhysicalAgent() {
            return physicalAgent;
        }

        public String getLxcHostname() {
            return lxcHostname;
        }

    }

    private class Destroyer implements Callable<DestroyInfo> {

        private final DestroyInfo info;

        public Destroyer(DestroyInfo cloneInfo) {
            this.info = cloneInfo;
        }

        public DestroyInfo call() throws Exception {
            info.setResult(MongoModule.getLxcManager().destroyLxcOnHost(info.physicalAgent, info.getLxcHostname()));
            return info;
        }
    }

    private boolean destroyLxcs() {
        CompletionService<DestroyInfo> completer = new ExecutorCompletionService<DestroyInfo>(MongoModule.getExecutor());
        try {
            Set<Agent> agents = new HashSet<Agent>();
            agents.addAll(config.getConfigServers());
            agents.addAll(config.getRouterServers());
            agents.addAll(config.getDataNodes());
            int tasks = 0;
            for (Agent agent : agents) {
                addOutput(String.format("Destroying lxc %s", agent.getHostname()));
                Agent physicalAgent = MongoModule.getAgentManager().getAgentByHostname(agent.getParentHostName());
                if (physicalAgent == null) {
                    addOutput(String.format("Could not determine physical parent of %s. Use LXC module to cleanup", agent.getHostname()));
                } else {
                    tasks++;
                    completer.submit(new Destroyer(new DestroyInfo(physicalAgent, agent.getHostname())));
                }
            }

            boolean result = true;
            for (int i = 0; i < tasks; i++) {
                Future<DestroyInfo> future = completer.take();
                DestroyInfo info = future.get();
                result &= info.isResult();
            }

            return result;
        } catch (InterruptedException e) {
        } catch (ExecutionException e) {
        }
        return false;
    }

    public void startOperation() {
        try {
            showProgress();
            start();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error in startOperation", e);
        }
    }

    private void showProgress() {
        indicator.setVisible(true);
        ok.setEnabled(false);
    }

    private void hideProgress() {
        indicator.setVisible(false);
        ok.setEnabled(true);
    }

    private void addOutput(String output) {
        if (!Util.isStringEmpty(output)) {
            outputTxtArea.setValue(
                    MessageFormat.format("{0}\n\n{1}",
                            outputTxtArea.getValue(),
                            output));
            outputTxtArea.setCursorPosition(outputTxtArea.getValue().toString().length() - 1);
        }
    }

}
