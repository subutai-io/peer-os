#!/bin/bash
sudo apt-get update
echo "PLEASE MAKE SURE IF YOU GIT CLONED ALREADY TO PLACE THIS SCRIPT ABOVE THE BASE DIRECTORY THAT WAS CLONED!"
echo "THIS SCRIPT WILL GIT CLONE FOR YOU!!!! Waiting 15s....."
sleep 15s
echo "Installing... Please wait! =^_^= This may take some time... Get some coffee."
sudo apt-get install -y dirmngr --fix-missing
sudo apt-get install -y software-properties-common debconf-utils git build-essential tar --fix-missing
sudo add-apt-repository -y ppa:webupd8team/java
sudo apt-get update
echo "oracle-java8-installer shared/accepted-oracle-license-v1-1 select true" | sudo debconf-set-selections
sudo apt-get install -y oracle-java8-installer --fix-missing
sudo apt-get install -y oracle-java8-unlimited-jce-policy --fix-missing
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
sudo apt-add-repository -y universe
sudo apt-get update
sudo apt install -y maven --fix-missing
echo "Below is your maven version, please verify it is ABOVE version 3.2.2:"
mvn -version
echo "If the version IS NOT ABOVE 3.2.2 something went wrong...."
echo "Git cloning base p2p cluster software..."
git clone https://github.com/Gr1dd/base.git
cd base/
echo "Installing immortal! Cool software!"
sudo apt install ./immortal_0.17.0_amd64.deb --fix-missing
cd management/
echo "ATTEMPTING BUILD OF BASE PACKAGE - THIS TAKES VERY VERY LONG"
mvn clean install -P deb
echo "HOPEFULLY EVERYTHING WAS SUCCESSFUL"
cd server/server-karaf/target/
echo "Untarring package"
tar xvzf *.tar.gz
echo """Now run /base/management/server/server-karaf/target/subutai-6.2.1-SNAPSHOT/bin/karaf"""
echo """Then go here: https://your_host_ip:8443"""
echo """attempting to execute..."""
echo $CWD
cd subutai*
cd bin/
sudo mkdir /data
echo "Launching in screen, you won't see anything here... It is also IMMORTAL!"
sudo immortal screen -d -m bash -c "sudo ./karaf"
echo """screen has been launched, go here: https://your_host_ip:8443"""
echo """the executable is here if you need it /base/management/server/server-karaf/target/subutai-*.*.*/bin/karaf"""
