package com.restaurant.siege.repository.mapper;

import com.restaurant.siege.model.entities.DishOrder;
import com.restaurant.siege.model.entities.DishOrderStatus;
import com.restaurant.siege.model.enums.StatusType;
import com.restaurant.siege.repository.dao.DishOrderStatusDAO;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.util.List;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class DishOrderMapper implements Function<ResultSet, DishOrder> {
    private final DishOrderStatusDAO dishOrderStatusDao;

    @Override
    @SneakyThrows
    public DishOrder apply(ResultSet rs) {
        DishOrder dishOrder = new DishOrder();
        Long dishOrderId = rs.getLong("dish_order_id");
        List<DishOrderStatus> dishOrderStatuses = dishOrderStatusDao.getStatusListFor(dishOrderId);

        dishOrder.setDishOrderId(dishOrderId);
        dishOrder.setDishId(rs.getLong("dish_id"));
        dishOrder.setDishName(rs.getString("dish_name"));
        dishOrder.setQuantity((int) rs.getLong("quantity_sold"));
        dishOrder.setTotalAmount(rs.getLong("total_amount"));
        dishOrder.setActualStatus(StatusType.valueOf(rs.getString("actual_status")));
        dishOrder.setSalesPoint(rs.getString("sales_point"));
        dishOrder.setStatusList(dishOrderStatuses);

        return dishOrder;
    }
}
