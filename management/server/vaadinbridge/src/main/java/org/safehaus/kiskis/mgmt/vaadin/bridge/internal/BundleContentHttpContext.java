package org.safehaus.kiskis.mgmt.vaadin.bridge.internal;

import org.osgi.framework.Bundle;
import org.osgi.service.http.HttpContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;

class BundleContentHttpContext implements HttpContext {

    private Bundle bundle;

    BundleContentHttpContext(Bundle bundle) {
        this.bundle = bundle;
    }

    public boolean handleSecurity(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // default behaviour assumes the container has already performed authentication
        return true;
    }

    public URL getResource(String name) {
        return bundle.getResource(name);
    }

    public String getMimeType(String name) {
        return null;
    }
}
