create type statusType as ENUM ('CREATED','CONFIRMED','IN_PROGRESS','DONE','DELIVERED');
CREATE TABLE IF NOT EXISTS dish_order (
  dish_order_id bigint PRIMARY KEY,
  sales_point varchar(100) not null,
  dish_id bigint not null,
  dish_name varchar(100) not null,
  quantity_sold bigint not null,
  total_amount bigint not null,
  actual_status statusType not null
);