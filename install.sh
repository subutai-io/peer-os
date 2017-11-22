#!/bin/bash
clear
echo "Installing... Please wait! =^_^= This may take some time..."
sudo apt-get install -y python-software-properties debconf-utils git build-essential tar
sudo add-apt-repository -y ppa:webupd8team/java
sudo apt-get update
echo "oracle-java8-installer shared/accepted-oracle-license-v1-1 select true" | sudo debconf-set-selections
sudo apt-get install -y oracle-java8-installer
sudo apt-get install -y oracle-java8-unlimited-jce-policy
echo "The below is the JAVA_HOME:"
echo $JAVA_HOME
echo "If its empty we need to configure it..."
this = "export JAVA_HOME=/usr/lib/jvm/java-8-oracle"
that = "export PATH=\$JAVA_HOME/bin:\$PATH"
export JAVA_HOME=/usr/lib/jvm/java-8-oracle
export PATH=$JAVA_HOME/bin:$PATH
sudo echo $this > /etc/environment
sudo echo $that > /etc/environment
reset
echo $this
echo $that
echo $JAVA_HOME
echo $PATH
java -version
echo "Purging old mavens:"
sudo apt-get purge maven maven2 maven3
echo "Installing latest maven:"
sudo apt-get install -y software-properties-common
sudo apt-add-repository -y universe
sudo apt-get update
sudo apt install -y maven
echo "Below is your maven version, please verify it is ABOVE version 3.2.2:"
mvn -version
echo "If the version IS NOT ABOVE 3.2.2 something went wrong...."
echo "Git cloning base p2p cluster software..."
git clone https://github.com/Gr1dd/base.git
cd base/management/
echo "Attempting build of base package"
mvn clean install
echo "Hopefully everything was successful!"
cd server/server-karaf/target/
echo "Untarring package"
tar xvzf *.tar.gz
echo """Now run {distr}/bin/karaf..."""
echo """Then go here: https://your_host_ip:8443"""



