package org.safehaus.subutai.api.templateregistry;


import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * TemplateTree is used for storing templates in genealogical tree. This class is used by UI modules to visualize
 * template hierarchy.
 */
public class TemplateTree {

    Map<String, List<Template>> parentChild = new HashMap<>();
    Map<String, String> childParent = new HashMap<>();


    public void addTemplate( Template template ) {
        List<Template> children = parentChild.get( template.getParentTemplateName() );
        if ( children == null ) {
            children = new LinkedList<>();
            parentChild.put( template.getParentTemplateName(), children );
        }
        children.add( template );
        childParent.put( template.getTemplateName(), template.getParentTemplateName() );
    }


    public Template getParentTemplate( Template childTemplate ) {
        return getParentTemplate( childTemplate.getTemplateName() );
    }


    public Template getParentTemplate( String childTemplateName ) {
        String parentTemplateName = getParentTemplateName( childTemplateName );
        if ( parentTemplateName != null ) {
            List<Template> templates = getChildrenTemplates( getParentTemplateName( parentTemplateName ) );
            if ( templates != null ) {
                for ( Template template : templates ) {
                    if ( parentTemplateName.equalsIgnoreCase( template.getTemplateName() ) ) {
                        return template;
                    }
                }
            }
        }
        return null;
    }


    public String getParentTemplateName( String childTemplateName ) {
        return childParent.get( childTemplateName );
    }


    public List<Template> getChildrenTemplates( String parentTemplateName ) {
        return parentChild.get( parentTemplateName );
    }


    public List<Template> getChildrenTemplates( Template parentTemplate ) {
        return getChildrenTemplates( parentTemplate.getTemplateName() );
    }
}
