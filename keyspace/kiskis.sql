CREATE KEYSPACE kiskis WITH replication = {'class': 'SimpleStrategy','replication_factor': '1'};

USE kiskis;

create table product_info
(
source text,
key text,
info text,
primary key(source,key)
);

CREATE TABLE product_operation (
  source text,
  id timeuuid,
  info text,
  PRIMARY KEY (source, id)
);

create table environment_info
(
source text,
key text,
info text,
primary key(source,key)
);

create table template_registry_info(
  template text,
  parent text,
  info text,
  primary key (template)
);
create INDEX on template_registry_info (parent);

create table nodes (
  uuid      text,
  env_id    text,
  info      text,
  primary key (uuid)
);
create index on nodes(env_id);

create table peer_info
(
source text,
key text,
info text,
primary key(source,key)
);

CREATE TABLE remote_requests (
  commandid text,
  attempts int,
  info text,
  PRIMARY KEY (commandid, attempts)
)

CREATE TABLE remote_responses (
  commandid text,
  responsenumber text,
  info text,
  PRIMARY KEY (commandid, responsenumber)
)
