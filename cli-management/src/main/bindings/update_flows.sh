#!/bin/bash
# ==========================================================================
# This script updates vni-vlan mapping flow by using tunnel map lists
# ==========================================================================
for file_name in /var/subutai-network/*_vni_vlan
do
    tunnel_name=`echo $file_name | cut -d"/" -f4 | cut -d"_" -f1`
    echo $tunnel_name
    tunnel_port=`ovs-ofctl show br-tun|grep "$tunnel_name"|cut -d"(" -f1 | cut -d" " -f2`
    echo $tunnel_port
    peer_port=`ovs-ofctl show br-tun|grep tunTOint|cut -d"(" -f1 |cut -d" " -f2`
    echo $peer_port
    while read line
    do
        vni=`echo $line | cut -d ' ' -f1`
        vlan=`echo $line | cut -d ' ' -f2`
	echo $vni $vlan
        ovs-ofctl add-flow br-tun "in_port=$peer_port,dl_vlan=$vlan,actions=set_field:$vni->tun_id,output:$tunnel_port"
        ovs-ofctl add-flow br-tun "in_port=$tunnel_port,tun_id=$vni,actions=set_field:$vlan->vlan_vid,output:$peer_port"
    done < $file_name
done


