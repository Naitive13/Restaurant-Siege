package com.restaurant.siege.repository.db;

import java.sql.Connection;
import java.sql.DriverManager;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Datasource {
  private final String url = System.getenv("DB_URL");
  private final String port = System.getenv("DB_PORT");
  private final String database = System.getenv("DB_DATABASE");
  private final String user = System.getenv("DB_USER");
  private final String password = System.getenv("DB_PASSWORD");
  private final String jdbcURL;

  public Datasource() {
    jdbcURL = "jdbc:postgresql://" + url + ":" + port + "/" + database;
  }

  public Connection getConnection() {
    try {
      return DriverManager.getConnection(jdbcURL, user, password);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
