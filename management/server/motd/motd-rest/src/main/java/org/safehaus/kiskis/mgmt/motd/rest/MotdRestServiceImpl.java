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
package org.safehaus.kiskis.mgmt.motd.rest;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.safehaus.kiskis.mgmt.motd.api.service.MotdService;

public class MotdRestServiceImpl implements MotdRestService {

    private MotdService motdService;

    public MotdRestServiceImpl() {
        BundleContext context = FrameworkUtil.getBundle(MotdRestServiceImpl.class).getBundleContext();
        ServiceReference ref = context.getServiceReference(MotdService.class.getName());
        if (ref != null) {
            try {
                motdService = (MotdService) context.getService(ref);
                System.out.println("Message of the day: " + motdService.getMessageOfTheDay());
            } catch (Exception e) {
                System.out.println("Error trying to connect to motd service: " + e.getMessage());
            }
        } else {
            System.out.println("Cannot find any registered services");
        }

        System.out.println("Motd Rest Service Implementation is created.");
    }

    public String getMessage() {
        String message = "<html><title>Message of the Day REST service</title><head>Message of the Day REST service</head><body><p>";
        try {
            message += "<a href=\"http://www.safehaus.org\">" + motdService.getMessageOfTheDay() + "</a>";
        } catch (Exception e) {
            message += "Cannot connect to the motd service : " + e.getMessage();
        }
        return message + "</p></body></html>";
    }

}