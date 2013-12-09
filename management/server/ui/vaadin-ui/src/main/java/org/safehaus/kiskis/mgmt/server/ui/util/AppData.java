/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.util;

import com.vaadin.Application;
import com.vaadin.service.ApplicationContext.TransactionListener;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

import java.io.Serializable;
import java.util.*;

/**
 * Holds data for one user session.
 */
public class AppData
        implements TransactionListener, Serializable {

    private ResourceBundle bundle;
    private Locale locale;   // Current locale
    private final Application app; // For distinguishing between apps
    private static final ThreadLocal<AppData> instance = new ThreadLocal<AppData>();
    //
    private List<Agent> selectedAgentList;

    public AppData(Application app) {
        this.app = app;
        selectedAgentList = new ArrayList<Agent>();

        // It's usable from now on in the current request
        instance.set(AppData.this);
    }

    @Override
    public void transactionStart(Application application,
            Object transactionData) {
        // Set this data instance of this application
        // as the one active in the current thread. 
        if (this.app == application) {
            instance.set(this);
        }
    }

    @Override
    public void transactionEnd(Application application,
            Object transactionData) {
        // Clear the reference to avoid potential problems
        if (this.app == application) {
            instance.set(null);
        }
    }

    public static void initLocale(Locale locale,
            String bundleName) {
        instance.get().locale = locale;
        instance.get().bundle = ResourceBundle.getBundle(bundleName, locale);
    }

    public static Locale getLocale() {
        return instance.get().locale;
    }

    public static String getMessage(String msgId) {
        return instance.get().bundle.getString(msgId);
    }

    public static Application getApplication() {
        return instance.get().app;
    }

    public static List<Agent> getSelectedAgentList() {
        if (instance.get() != null) {
            return instance.get().selectedAgentList;
        }
        return null;
    }

    public static void setSelectedAgentList(List<Agent> agentList) {
        if (instance.get() != null) {
            instance.get().selectedAgentList = agentList;
        }
    }
}
