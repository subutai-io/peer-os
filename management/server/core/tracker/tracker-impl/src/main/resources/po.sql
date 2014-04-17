use test;

CREATE TABLE product_operation (
  source text,
  id timeuuid,
  info text,
  PRIMARY KEY (source, id)
);