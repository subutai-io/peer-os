# Subutai Server repository

This repository contains source code of Subutai Server Project.
This is a multi-module Maven Java project.

# Building the project

## Prerequisites

To build the project, you need to have the following tools:

* Oracle JDK 7 or later
* maven >3.3.3
* curl
* git
* software-properties-common
* debconf-utils
* build-essential
* tar

  [Download Page (JDK 8)](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
  
  ### Installation (manual way)
   - Download the 32-bit or 64-bit Linux "compressed binary file" - it has a ".tar.gz" file extension.

   - Uncompress it

    `tar -xvf jdk-8-linux-i586.tar.gz`   (32-bit)

    `tar -xvf jdk-8-linux-x64.tar.gz`   (64-bit)

   The JDK 8 package is extracted into `./jdk1.8.0` directory. N.B.: Check carefully this folder name since Oracle seem to    change this occasionally with each update.

   ###### Now move the JDK 8 directory to `/usr/lib`

    ```bash
    sudo mkdir -p /usr/lib/jvm
    sudo mv ./jdk1.8.0 /usr/lib/jvm/
    ```

   ###### Now run

    ```bash
    sudo update-alternatives --install "/usr/bin/java" "java" "/usr/lib/jvm/jdk1.8.0/bin/java" 1
    sudo update-alternatives --install "/usr/bin/javac" "javac" "/usr/lib/jvm/jdk1.8.0/bin/javac" 1
    sudo update-alternatives --install "/usr/bin/javaws" "javaws" "/usr/lib/jvm/jdk1.8.0/bin/javaws" 1
    ```

   This will assign Oracle JDK a priority of 1, which means that installing other JDKs will [replace it as the default](http://askubuntu.com/q/344059/23678). Be sure to use a higher priority if you want Oracle JDK to remain the default.

   - Correct the file ownership and the permissions of the executables:

   ```bash
   sudo chmod a+x /usr/bin/java
   sudo chmod a+x /usr/bin/javac
   sudo chmod a+x /usr/bin/javaws
   sudo chown -R root:root /usr/lib/jvm/jdk1.8.0
   ```

   N.B.: Remember - Java JDK has many more executables that you can similarly install as above. `java`, `javac`, `javaws` are probably the most frequently required. This [answer lists](http://askubuntu.com/a/68227/14356) the other executables available.

   ###### Run

   ```bash
   sudo update-alternatives --config java
   ```
   
   ###### Then run
   
   ```
   java -version
   ```
   
   If you get any errors, or your java is not the oracle 8 revision, follow the below:
   
   ```bash
   echo 'export JAVA_HOME=/usr/lib/jvm/java-8-oracle/jre' | sudo tee --append /etc/profile
   echo 'export PATH=$JAVA_HOME/bin:$PATH' | sudo tee --append /etc/profile
   reset
   ```
   
   __Note for NetBeans users!__

   You need to [set the new JDK as default](http://stackoverflow.com/questions/2809366/changing-java-platform-on-which-netbeans-runs/2809447#2809447) editing the configuration file.



  ### Ubuntu: Installing Java 8
  
  ```bash
  sudo add-apt-repository ppa:webupd8team/java
  sudo apt-get update
  sudo echo "oracle-java8-installer shared/accepted-oracle-license-v1-1 select true" | sudo debconf-set-selections
  sudo apt-get install -y software-properties-common debconf-utils git build-essential tar curl
  sudo apt-get install oracle-java8-installer oracle-java8-unlimited-jce-policy oracle-java8-set-default
  ```
  
  ### Debian: Installing Java 8 - requires manually adding of ubuntu repository to PPA/Debian local repository list.
  
  ```bash
  sudo echo "deb http://ppa.launchpad.net/webupd8team/java/ubuntu xenial main" | tee /etc/apt/sources.list.d/webupd8team-java.list
  sudo echo "deb-src http://ppa.launchpad.net/webupd8team/java/ubuntu xenial main" | tee -a /etc/apt/sources.list.d/webupd8team-java.list
  sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys EEA14886
  sudo apt-get update
  sudo echo "oracle-java8-installer shared/accepted-oracle-license-v1-1 select true" | sudo debconf-set-selections
  sudo apt-get install -y software-properties-common debconf-utils git build-essential tar curl
  sudo apt-get install oracle-java8-installer oracle-java8-unlimited-jce-policy oracle-java8-set-default
  ```
  
  ### Maven3 is now default on Debian and Ubuntu, install it!
  
  ```bash
  sudo apt-get update
  sudo apt-get install maven
  ```
  
  ### Verify maven installation, check java (Must be >= 3.3.3)
  
  ```bash
  mvn --version
  Apache Maven 3.3.3 (12a6b3acb947671f09b81f49094c53f426d8cea1; 2014-12-14T17:29:23+00:00)
  Maven home: /usr/local/apache-maven-3.2.5
  Java version: 1.7.0_80, vendor: Oracle Corporation
  Java home: /usr/lib/jvm/java-7-oracle/jre
  Default locale: en_US, platform encoding: UTF-8
  OS name: "linux", version: "3.13.0-48-generic", arch: "amd64", family: "unix"
  ```

  ### Build steps (Debian)
  
    ```bash
    curl -L https://api.github.com/repos/subutai-io/base/tarball > base.tar.gz;mkdir -p base; tar xvf base.tar.gz -C base/ 
    cd base*/management
    mvn clean install
    ```
    
  ### Build steps (Ubuntu)
  
    ```bash
    curl -L https://api.github.com/repos/subutai-io/base/tarball > base.tar.gz;mkdir -p base; tar xvf base.tar.gz -C base/
    cd base*/management
    mvn clean install
    ```
    
    If you would like to build a .deb:
    
    ```bash
    mvn clean install -P deb
    ```

  ##### After that go to `https://your_host_ip:8443` in your browser.
  
  Login with:
  
  ```
  username: admin
  password: secret
  ```
