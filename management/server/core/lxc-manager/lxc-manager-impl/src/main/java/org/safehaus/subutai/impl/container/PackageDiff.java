package org.safehaus.subutai.impl.container;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.safehaus.subutai.shared.protocol.settings.Common;


/**
 * This class is used to retrieve packages difference between a template and its parent. Packages info is supposed to be
 * provided as a <tt>String</tt> instance and is the output of <tt>dpkg -l</tt> command. Packages are filtered first to
 * contain only Subutai related packages.
 */
class PackageDiff {

    List<String> getDiff( String parent, String child ) {
        Set<String> p = extractPackageNames( read( parent ) );
        Set<String> c = extractPackageNames( read( child ) );
        c.removeAll( p );

        List<String> ls = new ArrayList<>( c );
        Collections.sort( ls );
        return ls;
    }


    private List<String> read( String s ) {
        List<String> ls = new ArrayList<>();
        try ( BufferedReader br = new BufferedReader( new StringReader( s ) ) ) {
            String line;
            while ( ( line = br.readLine() ) != null ) {
                ls.add( line );
            }
        }
        catch ( IOException ex ) {
        }
        return ls;
    }


    private Set<String> extractPackageNames( List<String> ls ) {
        Pattern p = Pattern.compile( "\\s(" + Common.PACKAGE_PREFIX + "[\\w:.+-]*)\\s" );
        Matcher m = p.matcher( "" );

        Set<String> res = new HashSet<>();
        for ( String s : ls ) {
            if ( m.reset( s ).find() ) {
                res.add( m.group( 1 ) );
            }
        }

        return res;
    }
}
