package com.restaurant.siege.repository.dao;

import com.restaurant.siege.model.entities.DishOrder;
import com.restaurant.siege.repository.db.Datasource;
import com.restaurant.siege.repository.mapper.DishOrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class DishOrderDAO {
  private final Datasource datasource;
  private final DishOrderMapper dishOrderMapper;

  public List<DishOrder> getAll(int page, int pageSize) {
    List<DishOrder> dishes = new ArrayList<>();
    String query =
        "SELECT dish_order_id, sales_point, dish_name,"
            + "quantity_sold, total_amount, actual_status "
            + "FROM dish_order LIMIT ? OFFSET ?";

    try (Connection connection = this.datasource.getConnection();
        PreparedStatement st = connection.prepareStatement(query)) {
      st.setInt(1, pageSize);
      st.setInt(2, (page - 1) * pageSize);

      try (ResultSet rs = st.executeQuery()) {
        while (rs.next()) {
          DishOrder dishOrder = dishOrderMapper.apply(rs);
          dishes.add(dishOrder);
        }
      }
      return dishes;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public List<DishOrder> saveAll(List<DishOrder> dishOrderToSave) {
    List<DishOrder> dishes = new ArrayList<>();
    String query =
        "INSERT INTO dish_order "
            + "(dish_order_id, sales_point, dish_name, quantity_sold,total_amount,actual_status) "
            + "VALUES (?,?,?,?,?,?::statusType) "
            + "ON CONFLICT (dish_order_id) DO NOTHING "
            + "RETURNING dish_order_id, sales_point, dish_name, quantity_sold,total_amount,actual_status";

    try (Connection connection = this.datasource.getConnection();
        PreparedStatement st = connection.prepareStatement(query)) {

      dishOrderToSave.forEach(
          dish -> {
            try {
              st.setLong(1, dish.getDishOrderId());
              st.setString(2, dish.getSalesPoint());
              st.setString(3,dish.getDishName());
              st.setLong(4,dish.getQuantity());
              st.setLong(5, dish.getTotalAmount());
              st.setString(6,dish.getActualStatus().toString());

              try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                  dishes.add(dishOrderMapper.apply(rs));
                }
              }
            } catch (SQLException e) {
              throw new RuntimeException(e);
            }
          });

      return dishes;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
