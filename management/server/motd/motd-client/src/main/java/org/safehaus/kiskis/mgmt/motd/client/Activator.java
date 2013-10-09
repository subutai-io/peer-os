/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.safehaus.kiskis.mgmt.motd.client;

import org.safehaus.kiskis.mgmt.motd.api.service.MotdService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class Activator implements BundleActivator {

    public void start(BundleContext context) {
        ServiceReference ref = context.getServiceReference(MotdService.class.getName());
        if(ref != null) {
            try {
                MotdService motd =(MotdService) context.getService(ref);
                System.out.println("Message of the day : " + motd.getMessageOfTheDay());
            }
            catch (Exception e) {
                System.out.println("Error trying to connect to motd service : " + e.getMessage());
            }
        }
        else {
            System.out.println("Cannot find any registered services");
        }
    }

    public void stop(BundleContext context) {
        System.out.println("Stopping : " + context.getBundle().getSymbolicName());
    }

}