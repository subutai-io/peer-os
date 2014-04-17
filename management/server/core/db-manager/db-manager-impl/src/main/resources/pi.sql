use test;

create table product_info
(
source text,
key text,
info text,
primary key(source,key)
);