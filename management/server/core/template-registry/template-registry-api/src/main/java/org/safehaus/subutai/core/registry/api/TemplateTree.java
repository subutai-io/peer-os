package org.safehaus.subutai.core.registry.api;


import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.safehaus.subutai.common.protocol.Template;

import com.google.common.base.Strings;


/**
 * TemplateTree is used for storing templates in genealogical tree. This class is used by UI modules to visualize
 * template hierarchy.
 */
public class TemplateTree
{
    private static final String TEMPLATE_ARCH_FORMAT = "%s-%s";

    Map<String, List<Template>> parentChild = new HashMap<>();
    Map<String, String> childParent = new HashMap<>();


    /**
     * Adds template to template tree
     *
     * @param template - {@code Template}
     */
    public void addTemplate( Template template )
    {
        String parentTemplateName = Strings.isNullOrEmpty( template.getParentTemplateName() ) ? null :
                                    String.format( TEMPLATE_ARCH_FORMAT, template.getParentTemplateName().toLowerCase(),
                                            template.getLxcArch().toLowerCase() );
        List<Template> children = parentChild.get( parentTemplateName );
        if ( children == null )
        {
            children = new LinkedList<>();
            parentChild.put( parentTemplateName, children );
        }
        children.add( template );
        childParent.put( String.format( TEMPLATE_ARCH_FORMAT, template.getTemplateName().toLowerCase(),
                template.getLxcArch().toLowerCase() ), parentTemplateName );
    }


    /**
     * Returns parent template of the supplied template or null if the supplied template is root template
     *
     * @param childTemplate - template whose parent to return
     *
     * @return - parent template {@code Template}
     */
    public Template getParentTemplate( Template childTemplate )
    {
        return getParentTemplate( childTemplate.getTemplateName(), childTemplate.getLxcArch() );
    }


    /**
     * Returns parent template of the supplied template or null if the supplied template is root template
     *
     * @param childTemplateName - name of template whose parent to return
     * @param lxcArch - lxc architecture
     *
     * @return - parent template {@code Template}
     */
    public Template getParentTemplate( String childTemplateName, String lxcArch )
    {
        String parentTemplateName = getParentTemplateName( childTemplateName, lxcArch );
        if ( parentTemplateName != null )
        {
            List<Template> templates =
                    getChildrenTemplates( getParentTemplateName( parentTemplateName, lxcArch ), lxcArch );

            for ( Template template : templates )
            {
                if ( parentTemplateName.equalsIgnoreCase( template.getTemplateName() ) && template.getLxcArch()
                                                                                                  .equalsIgnoreCase(
                                                                                                          lxcArch ) )
                {
                    return template;
                }
            }
        }
        return null;
    }


    /**
     * Returns parent template name of the supplied template or null if the template is root template
     *
     * @param childTemplateName - name of template whose parent template name to return
     * @param lxcArch - lxc architecture
     *
     * @return - name of parent template {@code String}
     */
    public String getParentTemplateName( String childTemplateName, String lxcArch )
    {
        if ( lxcArch != null )
        {
            String childName = childTemplateName != null ?
                               String.format( TEMPLATE_ARCH_FORMAT, childTemplateName.toLowerCase(),
                                       lxcArch.toLowerCase() ) : null;
            String parentName = childParent.get( childName );

            if ( parentName != null )
            {
                return parentName.replace( String.format( "-%s", lxcArch.toLowerCase() ), "" );
            }
        }
        return null;
    }


    /**
     * Returns list of child templates of the supplied template
     *
     * @param parentTemplateName - name of template whose children to return
     * @param lxcArch - lxc architecture
     *
     * @return - list of {@code Template}
     */
    public List<Template> getChildrenTemplates( String parentTemplateName, String lxcArch )
    {
        if ( lxcArch != null )
        {
            String parentName = parentTemplateName != null ?
                                String.format( TEMPLATE_ARCH_FORMAT, parentTemplateName.toLowerCase(),
                                        lxcArch.toLowerCase() ) : null;
            List<Template> templates = parentChild.get( parentName );
            if ( templates != null )
            {
                return templates;
            }
        }
        return Collections.emptyList();
    }


    public List<Template> getRootTemplates()
    {
        return parentChild.get( null );
    }


    /**
     * Returns list of child templates of the supplied template
     *
     * @param parentTemplate - template whose children to return
     *
     * @return - list of {@code Template}
     */
    public List<Template> getChildrenTemplates( Template parentTemplate )
    {
        return getChildrenTemplates( parentTemplate.getTemplateName(), parentTemplate.getLxcArch() );
    }
}
