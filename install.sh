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
echo """If the result is empty, do the below:
oldIFS="$IFS"
IFS=$'\n'
choices=( $(find /usr/java -type d -maxdepth 1 -print) )
select choice in "${choices[@]}"; do
  [[ "$choice" ]] && break
done
IFS="$oldIFS"
this = "export JAVA_HOME=$choice"
that = "export PATH=\$JAVA_HOME/bin:\$PATH"
export JAVA_HOME=$choice
export PATH=$JAVA_HOME/bin:$PATH
sudo echo $this > /etc/profile
sudo echo $that > /etc/profile
reset
echo $this
echo $that
echo "Purging old mavens:"
sudo apt-get purge maven maven2 maven3
echo "Installing latest maven:"
sudo apt-add-repository -y ppa:andrei-pozolotin/maven3
sudo apt-get update
sudo apt-get install -y maven3
echo "Below is your maven version, please verify it is ABOVE version 3.2.2:"
mvn --version
echo "If the version IS NOT ABOVE 3.2.2 something went wrong...."
echo "Git cloning base p2p cluster software..."
git clone https://github.com/Gr1dd/base.git
cd base/management
echo "Attempting build of base package"
mvn clean install
echo "Hopefully everything was successful!"
cd server/server-karaf/target
echo "Untarring package"
tar xvzf *.tar.gz
echo """Now run {distr}/bin/karaf..."""



