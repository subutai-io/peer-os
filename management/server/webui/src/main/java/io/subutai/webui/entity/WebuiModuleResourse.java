package io.subutai.webui.entity;


import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class WebuiModuleResourse
{
    private String img;
    private String name;
    private List<AngularjsDependency> dependencies;

    private String bodyClass;
    private String layout;

    public WebuiModuleResourse( String name, String img )
    {
        this.bodyClass = "";
        this.layout = "default";
        this.name = name;
        this.img = img;
        this.dependencies = new ArrayList<>();
    }

    public String getImg()
    {
        return img;
    }


    public void setImg( final String img )
    {
        this.img = img;
    }


    public String getName()
    {
        return name;
    }


    public void setName( final String name )
    {
        this.name = name;
    }

    public void addDependency( AngularjsDependency dependency )
    {
        dependencies.add(dependency);
    }

    public void setBodyClass(String bodyClass) {
        this.bodyClass = bodyClass;
    }

    public void setLayout(String layout) {
        this.layout = layout;
    }

    public String getAngularjsList()
    {
        StringBuilder strBuilder = new StringBuilder();
        dependencies.forEach( d -> strBuilder.append( d.getAngularjsList() ).append( "," ) );

        String depsArg = strBuilder.toString();
        if( depsArg.length() > 0 )
        {
            depsArg = depsArg.substring( 0, depsArg.length() - 1 );
        }

        Object[] args = { name, bodyClass, layout, depsArg };
        MessageFormat mf = new MessageFormat(
                "{url:'/plugins/{0}',templateUrl:'plugins/{0}/partials/view.html',data:{bodyClass:'{1}',layout:'{2}'}," +
                        "resolve: {loadPlugin: ['$ocLazyLoad', function ($ocLazyLoad) {return $ocLazyLoad.load([{3}]);}]}};" );
        return mf.format( args );
    }
}
