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