package Teste;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class FabricaConexao {
    private static Connection conexao;

    public static void conectar(){
//            String url = "jdbc:postgresql://localhost:5432/rocket_db";
//            Properties props = new Properties();
//            props.setProperty("user", "postgres");
//            props.setProperty("password", "postgres123");
//            props.setProperty("ssl", "true");
        try {
            if (conexao == null) {
                String url = "jdbc:postgresql://localhost:5432/rocket_db?user=postgres&password=postgres123";
                conexao = DriverManager.getConnection(url);
                System.out.println("conexão realizada.");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static Connection getConexao() {
        return conexao;
    }
}
