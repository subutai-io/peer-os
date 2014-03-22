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
);

CREATE INDEX idx_agents_hostname ON agents (hostname);
CREATE INDEX idx_agents_lxc ON agents (islxc);
CREATE INDEX idx_agents_transport ON agents (transportid);

create table product_info
(
source text,
key text,
info text,
primary key(source,key)
);

CREATE TABLE logs (
id text,
log text,
PRIMARY KEY (id)
);

CREATE TABLE product_operation (
  id uuid,
  in_date timestamp,
  info text,
  PRIMARY KEY (id, in_date)
) WITH CLUSTERING ORDER BY (in_date DESC)