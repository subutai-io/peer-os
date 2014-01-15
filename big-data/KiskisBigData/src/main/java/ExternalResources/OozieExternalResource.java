/*
*  Licensed to the Apache Software Foundation (ASF) under one
*  or more contributor license agreements.  See the NOTICE file
*  distributed with this work for additional information
*  regarding copyright ownership.  The ASF licenses this file
*  to you under the Apache License, Version 2.0 (the
*  "License"); you may not use this file except in compliance
*  with the License.  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*
*/

package ExternalResources;

import org.apache.oozie.client.OozieClient;
import org.apache.oozie.client.WorkflowAction;
import org.apache.oozie.client.WorkflowJob;
import org.apache.oozie.local.LocalOozie;
import org.junit.rules.ExternalResource;

import java.text.MessageFormat;
import java.util.Properties;

/**
* ...
*
* @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
* @version $Rev$
*/
public class OozieExternalResource extends ExternalResource {
    private OozieClient wc;
    private Properties conf;
    private String oozieHost;
    private String oozieHome;
    private String oozieDataDir;
    private boolean isRemote;
    /**
     *
     * @param oozieHost  set this value to the ip address of the machine that is running Oozie Server
     */
    public OozieExternalResource(String oozieHost)
    {
        isRemote = true;
        this.oozieHost = oozieHost;
    }
    /**
     *  @param oozieHome set this value to the root directory of your oozie configuration folder.
     *                              example: "/home" directory where /home/conf exist.
     *  @param oozieDataDir set this value to a directory where you want to store the oozie data.
     */
    public OozieExternalResource(String oozieHome, String oozieDataDir)
    {
        isRemote = false;
        this.oozieHome = oozieHome;
        this.oozieDataDir = oozieDataDir;

    }
    @Override
    public void before()
    {
        if(isRemote)
            accessRemoteServer(oozieHost);
        else
            startLocalOozie(oozieHome, oozieDataDir);


    }

    private void startLocalOozie(String oozieHome,String oozieDataDir) {
        System.out.println("Starting Local Oozie");
        System.setProperty("oozie.home.dir", oozieHome);
        System.setProperty("oozie.data.dir",oozieDataDir);
        System.setProperty("oozie.db.schema.name", "default-schema");
        try {
            LocalOozie.start();
            wc = LocalOozie.getClient();

            conf = getWc().createConfiguration();

        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    private void accessRemoteServer(String oozieHost)
    {
        wc = new OozieClient("http://"+oozieHost+":11000/oozie");
//        create a workflow job configuration and set the workflow application path
        conf = getWc().createConfiguration();
    }
    @Override
    public void after()
    {
        if(!isRemote)
        {
            System.out.println("Stopping Oozie Server!");
            LocalOozie.stop();
        }
    }

    public OozieClient getWc() {
        return wc;
    }

    public void setWc(OozieClient wc) {
        this.wc = wc;
    }

    public Properties getConf() {
        return conf;
    }

    public void setConf(Properties conf) {
        this.conf = conf;
    }
    public static void printWorkflowInfo(WorkflowJob wf) {
        System.out.println("--Detailed information of Workflow job is as follows:");
        System.out.println("Application Path   : " + wf.getAppPath());
        System.out.println("Application Name   : " + wf.getAppName());
        System.out.println("Application Status : " + wf.getStatus());
        System.out.println("Application Actions:");
        for (WorkflowAction action : wf.getActions()) {
            System.out.println(MessageFormat.format("   Name: {0} Type: {1} Status: {2}", action.getName(),
                    action.getType(), action.getStatus()));
        }
        System.out.println();
    }

}
