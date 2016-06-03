Acceptance tests for Subutai Social
===================================
Dependencies:

Your system must has: ***Ubuntu 14*** and above, **Maven 3** and above, **Java 8**  
Also need additional packages:

```
sudo add-apt-repository ppa:gijzelaar/opencv2.4
sudo apt-get update
sudo apt-get install maven
sudo apt-get install xvfb
sudo apt-get install openjdk-8-jdk
sudo apt-get install recordmydesktop
sudo apt-get install wmctrl
sudo apt-get install xdotool
sudo apt-get install build-essentials
sudo apt-get build-essentials
sudo apt-get install libopencv2.4-java
sudo apt-get libcv-dev
sudo apt-get install libtesseract3 
```

At first you need go to playbooks directory

``` cd playbooks ```

Then You can run tests usign following script and parameters:

``` ./run_tests_qa.sh [-m] [-M] [-l] [-L] [-s] [-r] [-h]```

You can easily combine all parameters in one comand!

Parameter       | Description 
----------------|----------------------
-m              | Set Management Host First:  IP/domain
-M              | Set Management Host Second: IP/domain
-l              | Observe List of All Playbooks
-L              | Observe List Playbooks for current run
-s              | Choice of Playbooks for run
                | “playbook1.story playbook2.story” ...  Start a few Playbooks
-r              | Start acceptance tests
-h              | Get Help info

Also you can start tests using maven commands:

***For clean test project***
``` 
mvn clean;  
``` 
***For run tests, which is inside directory tests_run***
```
mvn integration-test; 
```

***For create html with reports about running tests***
```
mvn serenity:aggregate;  
```

After running tests you can find report in the ```target/site/serenity/``` directory.
For it, you need open ```index.html``` in your browser.

How it works?

Examples:

***Observe List of All Playbooks***
``` 
./run_tests_qa.sh -l 

AddRole.story
CassandraTemplate.story
ChangePassword.story
ChooseTheSizeOfContainers.story
CrossPeerEnviOnRemotePeer.story
CrossPeerWithTwoHosts.story
GrowContainer.story
KurjunAddTemplate.story
LocalEnvironment.story
PeerRegistration.story
PluginAppScale.story
PluginGeneric.story
SetDomainToContainer.story
ShareEnvironment.story
SmokeTest.story
StopRemoveContainer.story
TokensEnvironmentPgpKey.story
TokensOwnPgpKey.story
UserRegistration.story
```
***Choice of Playbooks for run***
```
./run_tests_qa.sh -s "AddRole.story, CassandraTemplate.story"
```
***Set Management Hosts, First and Second:  IP, domain***
```
./run_tests_qa.sh - m 192.168.0.119 -M domain.ddns.com
```
***Start acceptance tests***
```
./run_tests_qa.sh -r

SERENITY 
TEST STARTED ....
```
For record video you need use ```./run_tests_rec.sh```
Directory for videos: ``` /src/test/resources/video/ ```
For run on the Jenkins you need use ```./run_tests.sh```
