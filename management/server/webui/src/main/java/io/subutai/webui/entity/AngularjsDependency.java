package io.subutai.webui.entity;


import java.util.ArrayList;
import java.util.List;

public class AngularjsDependency {
    private String name;
    private List<String> files;

    public AngularjsDependency( String name )
    {
        this.name = name;
        files = new ArrayList<>();
    }

    public AngularjsDependency( String name, String ... files )
    {
        this(name);
        for( String file : files )
        {
            addFileString(file);
        }
    }

    public void addFileString( String file )
    {
        files.add(file);
    }

    public String getAngularjsList()
    {
        StringBuilder filesCsv = new StringBuilder();
        files.forEach( f -> filesCsv.append( f ).append( "," ) );

        String fileString = filesCsv.toString();

        if( fileString.length() > 0 )
        {
            fileString = fileString.substring(0, fileString.length() - 1);
        }

        return String.format(
                "{name : '%s', files : [%s]}", name, fileString);
    }
}
