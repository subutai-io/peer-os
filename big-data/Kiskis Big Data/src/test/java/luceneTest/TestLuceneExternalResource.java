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
package luceneTest;

import org.apache.lucene.demo.IndexFiles;
import org.apache.lucene.demo.SearchFiles;
import org.junit.ClassRule;
import org.junit.Test;

/**
 * ...
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$
 */
public class TestLuceneExternalResource {

    @Test
    public void searchQuery() throws Exception {
        String [] args = new String [2];
        args[0] = "-docs";
        args[1] = "/home/emin/test_documents/docs";

        IndexFiles.main(args);
        System.out.println("Searching query in Lucene Test");
        String input="furkan";

        String [] argv = new String [2];
        argv [0] = "-query";
        argv [1] = input;
        try {
            SearchFiles.main(argv);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
