/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.ui.mongodb;

import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Runo;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.safehaus.kiskis.mgmt.api.dbmanager.DbManager;
import org.safehaus.kiskis.mgmt.api.mongodb.Mongo;
import org.safehaus.kiskis.mgmt.server.ui.services.Module;
import org.safehaus.kiskis.mgmt.shared.protocol.Disposable;
import org.safehaus.kiskis.mgmt.ui.mongodb.manager.Manager;
import org.safehaus.kiskis.mgmt.ui.mongodb.tracker.Tracker;
import org.safehaus.kiskis.mgmt.ui.mongodb.wizard.Wizard;

/**
 *
 * @author dilshat
 * @todo make sure that operation log tracking thread is killed when UI is
 * closed (introduce thread timeout)
 */
public class MongoUI implements Module {

    public static final String MODULE_NAME = "Mongo";
    private static Mongo mongoManager;
    private static DbManager dbManager;
    private static ExecutorService executor;

    public static Mongo getMongoManager() {
        return mongoManager;
    }

    public static DbManager getDbManager() {
        return dbManager;
    }

    public static ExecutorService getExecutor() {
        return executor;
    }

    public void setMongoManager(Mongo mongoManager) {
        MongoUI.mongoManager = mongoManager;
    }

    public void setDbManager(DbManager dbManager) {
        MongoUI.dbManager = dbManager;
    }

    public void init() {
        executor = Executors.newCachedThreadPool();
    }

    public void destroy() {
        dbManager = null;
        mongoManager = null;
        executor.shutdown();
    }

    public static class ModuleComponent extends CustomComponent implements Disposable {

        private final Wizard wizard;
        private final Tracker tracker;
        private final Manager manager;
        private final String trackerTabName = "Tracker";

        public ModuleComponent() {
            setSizeFull();
            VerticalLayout verticalLayout = new VerticalLayout();
            verticalLayout.setSpacing(true);
            verticalLayout.setSizeFull();

            TabSheet mongoSheet = new TabSheet();
            mongoSheet.setStyleName(Runo.TABSHEET_SMALL);
            mongoSheet.setSizeFull();
            tracker = new Tracker();
            wizard = new Wizard(tracker, mongoSheet);
            manager = new Manager(tracker, mongoSheet);
            mongoSheet.addTab(wizard.getContent(), "Install");
            mongoSheet.addTab(manager.getContent(), "Manage");
            mongoSheet.addTab(tracker.getContent(), trackerTabName);

            mongoSheet.addListener(new TabSheet.SelectedTabChangeListener() {

                public void selectedTabChange(TabSheet.SelectedTabChangeEvent event) {
                    TabSheet tabsheet = event.getTabSheet();
                    String caption = tabsheet.getTab(event.getTabSheet().getSelectedTab()).getCaption();
                    if (caption.equals(trackerTabName)) {
                        tracker.startTracking();
                    } else {
                        tracker.stopTracking();
                    }
                }
            });

            verticalLayout.addComponent(mongoSheet);

            setCompositionRoot(verticalLayout);

        }

        public void dispose() {
            tracker.stopTracking();
        }

    }

    public String getName() {
        return MODULE_NAME;
    }

    public Component createComponent() {
        return new ModuleComponent();
    }

}
