package org.safehaus.subutai.server.ui.views;

import com.vaadin.event.LayoutEvents;
import com.vaadin.server.FileResource;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Image;
import org.safehaus.subutai.server.ui.api.PortalModule;

/**
 * Created by talas on 9/16/14.
 */
public class ModuleView extends CssLayout {

    private PortalModule mModule;
    private ModuleViewListener mClickListener;

    public interface ModuleViewListener {
        public void OnModuleClick(PortalModule module);
    }

    public ModuleView (PortalModule module, ModuleViewListener clickListener) {
        this.mModule = module;
        this.mClickListener = clickListener;

        setId(module.getId());
        setWidth(150, Unit.PIXELS);
        setHeight(200, Unit.PIXELS);
        addStyleName("create");

        addLayoutClickListener(new LayoutEvents.LayoutClickListener() {
            @Override
            public void layoutClick(LayoutEvents.LayoutClickEvent layoutClickEvent) {
                if (mClickListener != null) {
                    mClickListener.OnModuleClick(mModule);
                }
            }
        });

        Image image = new Image("", new FileResource(module.getImage()));
        image.setWidth(90, Unit.PERCENTAGE);
        image.setDescription(module.getName());
        addComponent(image);
    }
}
