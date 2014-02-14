CREATE KEYSPACE kiskis WITH replication = {'class': 'SimpleStrategy','replication_factor': '1'};
USE kiskis;
CREATE TABLE agents (
uuid uuid,
hostname text,
islxc boolean,
lastheartbeat timestamp,
listip list<text>,
macaddress text,
parenthostname text,
transportid text,
PRIMARY KEY (uuid)
) ;
CREATE INDEX idx_agents_hostname ON agents (hostname);
CREATE INDEX idx_agents_lxc ON agents (islxc);
CREATE INDEX idx_agents_transport ON agents (transportid);
CREATE TABLE cassandra_cluster_info (
uid uuid,
name text,
commitlogdir text,
datadir text,
nodes list<uuid>,
savedcachedir text,
seeds list<uuid>,
PRIMARY KEY (uid, name)
) ;
CREATE TABLE hadoop_cluster_info (
uid uuid,
cluster_name text,
data_nodes list<uuid>,
ip_mask text,
job_tracker uuid,
name_node uuid,
replication_factor int,
secondary_name_node uuid,
task_trackers list<uuid>,
PRIMARY KEY (uid, cluster_name)
) ;
CREATE TABLE hbase_info (
uid uuid,
info blob,
PRIMARY KEY (uid)
);
CREATE TABLE mongo_cluster_info (
cluster_name text,
config_servers list<uuid>,
data_nodes list<uuid>,
replica_set_name text,
routers list<uuid>,
PRIMARY KEY (cluster_name)
) ;
CREATE TABLE oozie_info (
uid uuid,
info blob,
PRIMARY KEY (uid)
) ;
CREATE TABLE requests (
taskuuid uuid,
agentuuid uuid,
reqseqnum int,
args list<text>,
environment map<text, text>,
erroutpath text,
outputredirectionstderr text,
outputredirectionstdout text,
pid int,
program text,
runsas text,
source text,
stdoutpath text,
timeout int,
type text,
workingdirectory text,
PRIMARY KEY (taskuuid, agentuuid, reqseqnum)
) ;
CREATE TABLE responses (
taskuuid uuid,
reqseqnum int,
resseqnum int,
agentuuid uuid,
errout text,
exitcode int,
hostname text,
ips list<text>,
islxc boolean,
macaddress text,
pid int,
responsetype text,
source text,
stdout text,
PRIMARY KEY (taskuuid, reqseqnum, resseqnum)
) ;
CREATE TABLE tasks (
uuid timeuuid,
description text,
status text,
PRIMARY KEY (uuid)
) ;

create table product_info
(
source text,
key text,
info text,
primary key(source,key)
);