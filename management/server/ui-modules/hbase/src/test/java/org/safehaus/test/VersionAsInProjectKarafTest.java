/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.safehaus.test;

import static junit.framework.Assert.assertTrue;
import static org.apache.karaf.tooling.exam.options.KarafDistributionOption.karafDistributionConfiguration;
import static org.ops4j.pax.exam.CoreOptions.maven;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.ExamReactorStrategy;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.exam.spi.reactors.AllConfinedStagedReactorFactory;

@RunWith(JUnit4TestRunner.class)
@ExamReactorStrategy(AllConfinedStagedReactorFactory.class)
public class VersionAsInProjectKarafTest {

    @Configuration
    public Option[] config() {
        return new Option[]{ karafDistributionConfiguration().frameworkUrl(
            maven().groupId("org.apache.karaf")
                    .artifactId("apache-karaf")
                    .type("zip")
                    .versionAsInProject())
            .karafVersion("2.2.3").name("Apache Karaf")};
    }

    @Test
    public void test() throws Exception {
        assertTrue(true);
    }
    
    
    
}
