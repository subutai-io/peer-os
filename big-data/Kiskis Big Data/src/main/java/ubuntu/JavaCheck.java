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
package ubuntu;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * ...
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$
 */
public class JavaCheck {
    public static void main(String [] args)
    {
        String processName = "NameNode";
        String host = "ubuntu@172.16.33.9";
        boolean exist = checkJavaProcess(processName, host);
        if (exist)
            System.out.println(processName +" is running on " + host);
        else
            System.out.println(processName +" is not running on " + host);
    }
    /**
     * @param processName
     * @param host
     * @return
     * example usage:
     * {
     *  String processName = "NameNode";
     *  String host = "ubuntu@172.16.33.9";
     *  boolean exist = checkJavaProcess(processName, host);
     * }
     */
    public static boolean checkJavaProcess(String processName, String host)
    {
        boolean exists = false;
        try {
            String line;
            Process p = Runtime.getRuntime().exec("ssh " + host +" jps");

            p.waitFor();
            if (p.exitValue()==0)
            {
                BufferedReader input =
                        new BufferedReader(new InputStreamReader(p.getInputStream()));
                while ((line = input.readLine()) != null) {
//                    System.out.println(line);
                    if(line.contains(processName))
                    {
//                        System.out.println(processName +" is running on " + host);
                        exists = true;
                        break;
                    }
                }
                input.close();
            }

        } catch (Exception err) {
            err.printStackTrace();
        }
//        if(exists==false)
//            System.out.println(processName +" is not running on " + host);
        return exists;
    }

}
