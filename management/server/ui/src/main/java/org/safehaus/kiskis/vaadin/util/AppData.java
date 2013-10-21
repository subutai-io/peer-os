/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.vaadin.util;

import com.vaadin.Application;
import com.vaadin.service.ApplicationContext.TransactionListener;
import org.safehaus.kiskis.mgmt.server.broker.impl.ResponseStorage;

import java.io.Serializable;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Holds data for one user session.
 */
public class AppData
        implements TransactionListener, Serializable {

    private ResourceBundle bundle;
    private Locale locale;   // Current locale
    private Application app; // For distinguishing between apps
    private static ThreadLocal<AppData> instance =
            new ThreadLocal<AppData>();

    //
//    private ResponseStorage broker;

    public AppData(Application app) {
        this.app = app;

        // It's usable from now on in the current request
        instance.set(this);
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
        instance.get().bundle =
                ResourceBundle.getBundle(bundleName, locale);
    }

    public static Locale getLocale() {
        return instance.get().locale;
    }

    public static String getMessage(String msgId) {
        return instance.get().bundle.getString(msgId);
    }

//    public static ResponseStorage getBroker() {
//        return instance.get().broker;
//    }
//
//    public static void setRemoteExecService(ResponseStorage broker) {
//        instance.get().broker = broker;
//    }
}