/etc/init.d/collectd stop
cp /etc/collectd/collectd.conf /etc/collectd/collectd.conf.template
> /etc/collectd/collectd.conf

echo "Hostname `hostname | head -1`" 	    >> /etc/collectd/collectd.conf
echo "LoadPlugin disk"						>> /etc/collectd/collectd.conf
echo "LoadPlugin network" 					>> /etc/collectd/collectd.conf
echo "<Plugin disk>" 						>> /etc/collectd/collectd.conf
echo "    Disk "sda"" 						>> /etc/collectd/collectd.conf
echo "    Disk \"/^hd/\"" 				>> /etc/collectd/collectd.conf
echo "    IgnoreSelected false" 			>> /etc/collectd/collectd.conf
echo "</Plugin>" 							>> /etc/collectd/collectd.conf
echo "<Plugin network>" 					>> /etc/collectd/collectd.conf
echo "    Server \"localhost\" \"25826\"" 	>> /etc/collectd/collectd.conf
echo "</Plugin>"							>> /etc/collectd/collectd.conf

service logstash stop
sed -i '0,/}/s//}\n   collectd{}/' /etc/logstash/indexer.conf
