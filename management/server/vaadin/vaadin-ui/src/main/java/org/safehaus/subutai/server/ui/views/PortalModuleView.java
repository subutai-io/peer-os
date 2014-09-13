package org.safehaus.subutai.server.ui.views;

import com.vaadin.event.LayoutEvents;
import com.vaadin.server.FileResource;
import com.vaadin.ui.*;
import org.safehaus.subutai.server.ui.api.PortalModule;

/**
 * Created by talas on 9/13/14.
 */
public class PortalModuleView extends CssLayout {
    public PortalModule getPortalModule() {
        return mPortalModule;
    }

    private PortalModule mPortalModule;

    public PortalModuleView(PortalModule portalModule, final ModulesView.PortalModelViewListener callback) {
        mPortalModule = portalModule;

        setId(mPortalModule.getId());
        setWidth(150, Unit.PIXELS);
        setHeight(200, Unit.PIXELS);
        addStyleName("create");

        addLayoutClickListener(new LayoutEvents.LayoutClickListener() {
            @Override
            public void layoutClick(LayoutEvents.LayoutClickEvent layoutClickEvent) {
                callback.onItemClick(PortalModuleView.this);
            }
        });

        Image image = new Image("", new FileResource(mPortalModule.getImage()));
        image.setWidth(90, Unit.PERCENTAGE);
        image.setDescription(mPortalModule.getName());
        addComponent(image);
    }
}
