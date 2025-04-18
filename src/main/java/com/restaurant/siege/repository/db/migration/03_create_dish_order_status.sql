create table if not exists dish_order_status(
    status_id bigint PRIMARY KEY,
    dish_order_id bigint references dish_order(dish_order_id),
    dish_order_status statusType not null,
    creation_date timestamp not null,
    unique (dish_order_id, dish_order_status)
);