# Subutai Social repository

This repository contains source code of Subutai Social Console Project.
This is a multi-module Maven Java project.

## Building the project

###Prerequisites

To build the project, you need to have the following tools:

- Oracle JDK 7 or later

  [Download Page (JDK 8)](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
  
  [Installation](http://askubuntu.com/questions/56104/how-can-i-install-sun-oracles-proprietary-java-jdk-6-7-8-or-jre)

  ###### Setting JAVA_HOME
  ```bash
  update-java-alternatives -l
  sudo nano /etc/profile
  Add
  export JAVA_HOME="path that you found"
  export PATH=$JAVA_HOME/bin:$PATH
  ```
- Unlimited strength files (specific for Java version)

  [Download Page (JDK 8)](http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html)
  
  ```bash
  cp local_policy.jar US_export_policy.jar $JAVA_HOME/jre/lib/security
  ```
  
- Maven version 3.2.2 or later

  [Download Page](https://maven.apache.org/download.cgi)
  
  [Installation](http://basicgroundwork.blogspot.com/2015/05/installing-maven-333-on-ubuntu-1504.html)

###Build steps

- Clone the project by using:

    `git clone https://github.com/subutai-io/Subutai.git`

- Start maven build ( cd to management directory and issue ):

    ```bash
    mvn clean install
    ```

After this you will have `management/server/server-karaf/target` directory with **subutai-{version}.tar.gz** archive
which contains custom Karaf distribution of SS Console application.
Untar it to some directory and execute `{distr}/bin/karaf`.

After that go to `https://you_host_ip:8443` in your browser.
