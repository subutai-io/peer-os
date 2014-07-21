package org.safehaus.subutai.impl.container;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is used to retrieve packages difference between a template and its
 * parent. Packages info is supposed to be provided as a <tt>String</tt>
 * instance and is the output of <tt>dpkg -l</tt> command. Packages are filtered
 * first to contain only Subutai related packages.
 */
class PackageDiff {

    static final String SUBUTAI_PREFIX = "ksks";

    List<String> getDiff(String parent, String child) {
        Set<String> p = extractPackageNames(read(parent));
        Set<String> c = extractPackageNames(read(child));
        c.removeAll(p);

        List<String> ls = new ArrayList<>(c);
        Collections.sort(ls);
        return ls;
    }

    private List<String> read(String s) {
        List<String> ls = new ArrayList<>();
        try(BufferedReader br = new BufferedReader(new StringReader(s))) {
            String line;
            while((line = br.readLine()) != null)
                ls.add(line);
        } catch(IOException ex) {
        }
        return ls;
    }

    private Set<String> extractPackageNames(List<String> ls) {
        Pattern p = Pattern.compile("\\s(" + SUBUTAI_PREFIX + "[\\w:.+-]*)\\s");
        Matcher m = p.matcher("");

        Set<String> res = new HashSet<>();
        for(String s : ls) if(m.reset(s).find()) res.add(m.group(1));

        return res;
    }

}
