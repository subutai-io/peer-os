# Subutai Server repository

This repository contains source code of Subutai Server Project.
This is a multi-module Maven Java project.

## Building the project

###Prerequisites

To build the project, you need to have the following tools:

- Oracle JDK 7 or later

  [Download Page (JDK 8)](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
  
  ###### Installation (manual way)
   - Download the 32-bit or 64-bit Linux "compressed binary file" - it has a ".tar.gz" file extension.

   - Uncompress it

    `tar -xvf jdk-8-linux-i586.tar.gz`   (32-bit)

    `tar -xvf jdk-8-linux-x64.tar.gz`   (64-bit)

   The JDK 8 package is extracted into `./jdk1.8.0` directory. N.B.: Check carefully this folder name since Oracle seem to    change this occasionally with each update.

   - Now move the JDK 8 directory to `/usr/lib`

    ```bash
    sudo mkdir -p /usr/lib/jvm
    sudo mv ./jdk1.8.0 /usr/lib/jvm/
    ```

   - Now run

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

   - Run

   ```bash
   sudo update-alternatives --config java
   ```

   You will see output similar to the one below - choose the number of jdk1.8.0 - for example `3` in this list (unless you have have never installed Java installed in your computer in which case a sentence saying "There is nothing to configure" will appear):
   
   ```bash
   $ sudo update-alternatives --config java
   There are 3 choices for the alternative java (providing /usr/bin/java).

      Selection    Path                                            Priority   Status
      ------------------------------------------------------------
      0            /usr/lib/jvm/java-7-openjdk-amd64/jre/bin/java   1071      auto mode
      1            /usr/lib/jvm/java-7-openjdk-amd64/jre/bin/java   1071      manual mode
    * 2            /usr/lib/jvm/jdk1.7.0/bin/java                   1         manual mode
      3            /usr/lib/jvm/jdk1.8.0/bin/java                   1         manual mode

   Press enter to keep the current choice[*], or type selection number: 3
   update-alternatives: using /usr/lib/jvm/jdk1.8.0/bin/java to provide /usr/bin/java (java) in manual mode
   ```
   
   Repeat the above for:
   
   ```bash
   sudo update-alternatives --config javac
   sudo update-alternatives --config javaws
   ```
   __Note for NetBeans users!__

   You need to [set the new JDK as default](http://stackoverflow.com/questions/2809366/changing-java-platform-on-which-netbeans-runs/2809447#2809447) editing the configuration file.



  ###### Ubuntu: Installing Java 8
  ```bash
  sudo add-apt-repository ppa:webupd8team/java
  sudo apt-get update
  sudo apt-get install oracle-java8-installer
  sudo apt-get install oracle-java8-unlimited-jce-policy
  ```

  ###### Setting JAVA_HOME
  
  To check if JAVA_HOME is set or not, execute
  
  ```bash
  echo $JAVA_HOME
  ```
  
  If the result is empty or points to the version of Java that is not suitable, you need to set it. 
  
  ```bash
  update-java-alternatives -l
  sudo nano /etc/profile
  
  Add following lines at the end:
  export JAVA_HOME="path that you found in update-java-alternatives for your JDK without quotes"
  export PATH=$JAVA_HOME/bin:$PATH
  
  Save file
  
  reset
  ```
  
  Check again.
  
- Unlimited strength files (specific for Java version)

  [Download Page (JDK 8)](http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html)
  
  ```bash
  unzip jce_policy-8.zip
  cd UnlimitedJCEPolicyJDK8
  sudo cp local_policy.jar US_export_policy.jar $JAVA_HOME/jre/lib/security
  ```

  OR

  ```bash
  sudo apt-get install oracle-java8-unlimited-jce-policy
  ```
  
- Maven version 3.2.2 or later

  [Download Page](https://maven.apache.org/download.cgi)
  
  Before installing, remove older versions:
  
  ```bash
  sudo apt-get purge maven maven2 maven3
  ```
  
  ###### Installation (manual way - works only on Ubuntu 15.04)
  
  - Unzip the binary with tar
  
  ```bash
  tar -zxf apache-maven-3.3.3-bin.tar.gz
  ```
  
  - Move the application directory to `/usr/local`
  
  ```bash
  sudo cp -R apache-maven-3.3.3 /usr/local
  ```

  - Make a soft link in `/usr/bin` for universal access of `mvn`
  
  ```bash
  sudo ln -s /usr/local/apache-maven-3.3.3/bin/mvn /usr/bin/mvn
  ```
  
  ###### Installation (apt repo - older versions)
  
  ```bash
  sudo apt-add-repository ppa:andrei-pozolotin/maven3
  sudo apt-get update
  sudo apt-get install maven3
  ```
  
  ###### Verify mvn installation
  
```bash
  mvn --version
  
  Apache Maven 3.3.3 (12a6b3acb947671f09b81f49094c53f426d8cea1; 2014-12-14T17:29:23+00:00)
  Maven home: /usr/local/apache-maven-3.2.5
  Java version: 1.7.0_80, vendor: Oracle Corporation
  Java home: /usr/lib/jvm/java-7-oracle/jre
  Default locale: en_US, platform encoding: UTF-8
  OS name: "linux", version: "3.13.0-48-generic", arch: "amd64", family: "unix"
```

###Build steps

- Clone the project by using:

    `git clone https://github.com/subutai-io/base.git`

- Start maven build:

    ```bash
    cd base/management
    mvn clean install
    ```
  If you want to create a Debian package, add additional flag
  
    ```bash
  mvn clean install -P deb
    ```

After this you will have `management/server/server-karaf/target` directory with **subutai-{version}.tar.gz** archive
which contains custom Karaf distribution of SS application.

Untar it to some directory and execute `{distr}/bin/karaf`.

After that go to `https://your_host_ip:8443` in your browser.


###Development

For development purposes, access to management container can be opened
by executing the following commands on RH-with-MH:

```
sudo iptables -t nat -A PREROUTING -i wan -p tcp -m tcp --dport 2222 -j DNAT --to-destination 10.10.10.1:22
sudo iptables -t nat -A PREROUTING -i wan -p tcp -m tcp --dport 5005 -j DNAT --to-destination 10.10.10.1:5005
```

This would open ports **2222** for ssh access and **5005** for debugger.

**CAUTION**: this must be used for development only. Highly dangerous to do this in production!