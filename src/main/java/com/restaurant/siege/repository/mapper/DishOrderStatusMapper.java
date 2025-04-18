package com.restaurant.siege.repository.mapper;

import java.sql.ResultSet;
import java.util.function.Function;

import com.restaurant.siege.model.entities.DishOrderStatus;
import com.restaurant.siege.model.enums.StatusType;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

@Component
public class DishOrderStatusMapper implements Function<ResultSet, DishOrderStatus> {
  @Override
  @SneakyThrows
  public DishOrderStatus apply(ResultSet rs) {
    DishOrderStatus dishOrderStatus = new DishOrderStatus();

    dishOrderStatus.setId(rs.getLong("status_id"));
    dishOrderStatus.setDishOrderId(rs.getLong("dish_order_id"));
    dishOrderStatus.setStatus(StatusType.valueOf(rs.getString("dish_order_status")));
    dishOrderStatus.setCreationDate(rs.getTimestamp("creation_date").toLocalDateTime());

    return dishOrderStatus;
  }
}
