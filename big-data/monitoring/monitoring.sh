#!/bin/bash
set -e

function usage()
{
	echo "Example usage:"
	echo "./monitoring.sh server {logstash|elasticsearch} {install|remove|purge}	-> Install, Remove, Purge elasticsearch or logstash"
	echo "./monitoring.sh server logstash configure 'elasticsearch_IP'		-> Configure logstash with the IP address of elasticsearch"
	echo "./monitoring.sh client {install|remove|purge}				-> Install, Remove, Purge monitoring daemon"
	echo "./monitoring.sh client configure 'logstash_IP'				-> Configure monitoring daemon with the IP address of logstash"
	
	exit 0
}

if [[ "$1" == "" ]];
then
	usage
fi
if [[ "$1" == "help" ]];
then
	usage	
fi

case "$1" in
server)
	case "$2" in
	logstash)
		case "$3" in
		install)
			# install ksks-logstash 
			dpkg -i ksks-logstash-*.deb
			;;
		remove)
			# remove ksks-logstash 
			dpkg -r ksks-logstash
		 	;;
		purge)
			# purge ksks-logstash 
			dpkg -P ksks-logstash
		 	;;
		configure)
			ip=$4
			file=/etc/logstash/indexer.conf
			sed -i "s/host =.*/host => \"$ip\"/g" $file
			echo "indexer.conf is changed, please restart logstash service (e.g. service logstash restart)" 
			;;
		*)
			usage
		esac
	;;
	elasticsearch)
		case "$3" in
		install)
			# download & install elasticsearch 
			wget https://download.elasticsearch.org/elasticsearch/elasticsearch/elasticsearch-0.90.10.deb
			dpkg -i elasticsearch-*.deb
		 	;;
		remove)
			# remove elasticsearch
			dpkg -r elasticsearch
		 	;;
		purge)
			# purge elasticsearch
			dpkg -P elasticsearch
		 	;;
		*)
			usage
		esac
	;; 
	*)
		usage 
	esac
;;
client)
	case "$2" in
	install)
		# install ganglia monitoring daemon (gmond)
		apt-get --yes --force-yes install ganglia-monitor

		# copy our configuration into /etc/ganglia/gmond.conf
		cp gmond.conf /etc/ganglia/	
	 	;;
	remove)
		# remove ganglia monitoring daemon (gmond)
		apt-get --yes --force-yes remove ganglia-monitor
	 	;;
	purge)
		# purge ganglia monitoring daemon (gmond)
		apt-get --yes --force-yes purge ganglia-monitor
	 	;;
	configure)
		ip=$3
		file=/etc/ganglia/gmond.conf
		sed -i "s/host =.*/host = $ip/g" $file
		echo "gmond.conf is changed, please restart gmond service (e.g. /etc/init.d/ganglia-monitor restart)" 
		;;
	*)
		usage
	esac
;;
*)
	usage
esac



