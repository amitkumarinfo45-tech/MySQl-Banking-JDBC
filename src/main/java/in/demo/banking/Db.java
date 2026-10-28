package in.demo.banking;
import java.sql.*;
public final class Db {
 private static final String URL=System.getenv().getOrDefault("BANK_DB_URL","jdbc:mysql://localhost:3306/java_banking?useSSL=false&serverTimezone=UTC");
 private static final String USER=System.getenv().getOrDefault("BANK_DB_USER","root");
 private static final String PASSWORD=System.getenv().getOrDefault("BANK_DB_PASSWORD","");
 private Db(){}
 public static Connection getConnection() throws SQLException { return DriverManager.getConnection(URL,USER,PASSWORD); }
}
