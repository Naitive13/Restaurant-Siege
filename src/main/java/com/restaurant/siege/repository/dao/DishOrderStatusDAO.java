package com.restaurant.siege.repository.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import com.restaurant.siege.model.entities.DishOrderStatus;
import com.restaurant.siege.repository.db.Datasource;
import com.restaurant.siege.repository.mapper.DishOrderStatusMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class DishOrderStatusDAO {
  private final Datasource datasource;
  private final DishOrderStatusMapper dishOrderStatusMapper;

  public List<DishOrderStatus> getAll(int page, int pageSize) {
    List<DishOrderStatus> dishOrderStatusList = new ArrayList<>();
    String query =
        "SELECT status_id, dish_order_id, dish_order_status, creation_date "
            + "FROM dish_order_status LIMIT ? OFFSET ?";

    try (Connection connection = this.datasource.getConnection();
        PreparedStatement st = connection.prepareStatement(query)) {
      st.setInt(1, pageSize);
      st.setInt(2, (page - 1) * pageSize);

      try (ResultSet rs = st.executeQuery()) {
        while (rs.next()) {
          dishOrderStatusList.add(dishOrderStatusMapper.apply(rs));
        }
      }
      return dishOrderStatusList;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public DishOrderStatus getById(long id) {
    List<DishOrderStatus> dishOrderStatusList = new ArrayList<>();
    String query =
        "SELECT status_id, dish_order_id, dish_order_id, dish_order_status, creation_date "
            + "FROM dish_order_status WHERE status_id = ?";

    try (Connection connection = this.datasource.getConnection();
        PreparedStatement st = connection.prepareStatement(query)) {
      st.setLong(1, id);

      try (ResultSet rs = st.executeQuery()) {
        if (rs.next()) {
          return dishOrderStatusMapper.apply(rs);
        } else {
          throw new RuntimeException("Dish Order Status not found");
        }
      }

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public List<DishOrderStatus> saveAll(List<DishOrderStatus> dishOrderStatusesToAdd) {
    List<DishOrderStatus> dishOrderStatusList = new ArrayList<>();

    if (dishOrderStatusesToAdd != null && !dishOrderStatusesToAdd.isEmpty()) {
      String query =
          "INSERT INTO dish_order_status "
              + "(status_id, dish_order_id, dish_order_status, creation_date) "
              + "VALUES (?,?,?::statusType,?) "
              + "ON CONFLICT (dish_order_id, dish_order_status) DO NOTHING "
              + "RETURNING status_id, dish_order_id, dish_order_status, creation_date";

      try (Connection connection = this.datasource.getConnection();
          PreparedStatement st = connection.prepareStatement(query)) {

        dishOrderStatusesToAdd.forEach(
            dishOrderStatus -> {
              try {
                st.setLong(1, dishOrderStatus.getId());
                st.setLong(2, dishOrderStatus.getDishOrderId());
                st.setString(3, dishOrderStatus.getStatus().toString());
                st.setTimestamp(4, Timestamp.valueOf(dishOrderStatus.getCreationDate()));

                try (ResultSet rs = st.executeQuery()) {
                  if (rs.next()) {
                    dishOrderStatusList.add(dishOrderStatusMapper.apply(rs));
                  }
                }
              } catch (SQLException e) {
                throw new RuntimeException(e);
              }
            });

      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    return dishOrderStatusList;
  }

  public List<DishOrderStatus> getStatusListFor(Long dishOrderId) {
    List<DishOrderStatus> dishOrderStatusList = new ArrayList<>();
    String query =
        "SELECT status_id, dish_order_id, dish_order_status, creation_date "
            + "FROM dish_order_status WHERE dish_order_id = ?";

    try (Connection connection = this.datasource.getConnection();
        PreparedStatement st = connection.prepareStatement(query)) {
      st.setLong(1, dishOrderId);

      try (ResultSet rs = st.executeQuery()) {
        while (rs.next()) {
          dishOrderStatusList.add(dishOrderStatusMapper.apply(rs));
        }
      }
      return dishOrderStatusList;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
