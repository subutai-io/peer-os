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

import java.util.ArrayList;

/**
 * ...
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$
 */
public class OrderExternalResources extends MyExternalResource
{
    ArrayList<MyExternalResource> resources = new ArrayList<MyExternalResource>();

    public OrderExternalResources(ArrayList<MyExternalResource> externalResources)
    {
        resources = externalResources;
    }

    public OrderExternalResources(MyExternalResource externalResource1, MyExternalResource externalResource2)
    {
        resources.add(0, externalResource1);
        resources.add(1, externalResource2);
    }
    public OrderExternalResources(MyExternalResource externalResource1, MyExternalResource externalResource2, MyExternalResource externalResource3)
    {
        resources.add(0, externalResource1);
        resources.add(1, externalResource2);
        resources.add(2, externalResource3);
    }
    public OrderExternalResources(MyExternalResource externalResource1, MyExternalResource externalResource2, MyExternalResource externalResource3, MyExternalResource externalResource4)
    {
        resources.add(0, externalResource1);
        resources.add(1, externalResource2);
        resources.add(2, externalResource3);
        resources.add(3, externalResource4);
    }

    @Override
    public void before(){
        for(MyExternalResource er : resources)
            er.before();
    }
    @Override
    public void after(){
        for(MyExternalResource er : resources)
            er.after();
    }
}
