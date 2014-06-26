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


    /**
     * Adds template to template tree
     *
     * @param template - {@code Template}
     */
    public void addTemplate( Template template ) {
        List<Template> children = parentChild.get( template.getParentTemplateName() );
        if ( children == null ) {
            children = new LinkedList<>();
            parentChild.put( template.getParentTemplateName(), children );
        }
        children.add( template );
        childParent.put( template.getTemplateName(), template.getParentTemplateName() );
    }


    /**
     * Returns parent template of the supplied template or null if the supplied template is root template
     *
     * @param childTemplate - template whose parent to return
     *
     * @return - parent template {@code Template}
     */
    public Template getParentTemplate( Template childTemplate ) {
        return getParentTemplate( childTemplate.getTemplateName() );
    }


    /**
     * Returns parent template of the supplied template or null if the supplied template is root template
     *
     * @param childTemplateName - name of template whose parent to return
     *
     * @return - parent template {@code Template}
     */
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


    /**
     * Returns parent template name of the supplied template or null if the template is root template
     *
     * @param childTemplateName - name of template whose parent template name to return
     *
     * @return - name of parent template {@code String}
     */
    public String getParentTemplateName( String childTemplateName ) {
        return childParent.get( childTemplateName );
    }


    /**
     * Returns list of child templates of the supplied template
     *
     * @param parentTemplateName - name of template whose children to return
     *
     * @return - list of {@code Template}
     */
    public List<Template> getChildrenTemplates( String parentTemplateName ) {
        return parentChild.get( parentTemplateName );
    }


    /**
     * Returns list of child templates of the supplied template
     *
     * @param parentTemplate - template whose children to return
     *
     * @return - list of {@code Template}
     */
    public List<Template> getChildrenTemplates( Template parentTemplate ) {
        return getChildrenTemplates( parentTemplate.getTemplateName() );
    }
}
