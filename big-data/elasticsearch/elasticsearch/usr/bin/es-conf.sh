#!/bin/bash
 
usage()
{
    echo "Example usage:"
    echo "es-conf.sh cluster.name my_cluster    -> Changes cluster name"
    echo "es-conf.sh node.name my_node1         -> Changes nodes name"
    echo "es-conf.sh node.data {true|false} -> Sets this node as data node"
    echo "es-conf.sh node.master {true|false}   -> Sets this node as master node"
    echo "es-conf.sh index.number_of_shards 5   -> Sets index.number_of_shards property. Default is 5"
    echo "es-conf.sh index.number_of_replicas 1 -> Sets index.number_of_shards property. Default is 1"
    exit 0
}
if [[ "$1" == "" || "$2" == ""  ]]; then
    usage
fi
 
if [ "$1" == "help" ]; then
    usage
fi
 
case "$1" in
    cluster.name)
        sed -i "s/.*cluster.name: .*/cluster.name: $2/g" /etc/elasticsearch/elasticsearch.yml
    ;;
    node.name)
        sed -i "s/.*node.name: .*/node.name: $2/g" /etc/elasticsearch/elasticsearch.yml
    ;;
    node.data)
        if [[ "$2" == "true" || "$2" == "false" ]]; then
            sed -i "s/.*node.data: .*/node.data: $2/g" /etc/elasticsearch/elasticsearch.yml
        else
            echo "You should enter true or false"
            usage
        fi 
    ;;
    node.master)
        if [[ "$2" == "true" || "$2" == "false" ]]; then
            sed -i "s/.*node.master: .*/node.master: $2/g" /etc/elasticsearch/elasticsearch.yml
        else
            echo "You should enter true or false."
            usage
        fi     
    ;;
    index.number_of_shards)
        sed -i "s/.*index.number_of_shards: .*/index.number_of_shards: $2/g" /etc/elasticsearch/elasticsearch.yml
    ;;
    index.number_of_replicas)
        sed -i "s/.*index.number_of_replicas: .*/index.number_of_replicas: $2/g" /etc/elasticsearch/elasticsearch.yml
    ;;
    *)
        usage
        exit 1
    ;;
esac
