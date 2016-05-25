Acceptance tests for Subutai Social
===================================
Dependencies:

Your system must has: ***Ubuntu 14*** and above, **Maven 3** and above, **Java 7/8**  
Also need additional packages **OpenCV**
Install next packages:
```
sudo add-apt-repository ppa:gijzelaar/opencv2.4
sudo apt-get update
sudo apt-get libcv-dev
sudo apt-get install libtesseract3 
```

You are can run tests usign following script and parameters:

``` run_tests_qa.sh [-m] [-M] [-l] [-L] [-s] [-r] [-h]```

Parameter       | Description 
----------------|----------------------
-m              | Set Management Host First:  IP
-M              | Set Management Host Second: IP
-l              | Observe List of All Playbooks
-s              | Choice of Playbooks for run
                | “playbook1.story, playbook2.story” ...  Start a few Playbooks
-r              | Start acceptance tests
-h              | Get Help info

Also you can start tests using maven commands:

``` 
mvn clean;  ***for clean test project***
``` 
```
mvn integration-tests; ***for run tests, which is inside directory tests_run***
```
```
mvn serenity:aggregate;  ***for create html with reports about running tests***
```


.... to be continue 
