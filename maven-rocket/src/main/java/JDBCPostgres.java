import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

public class JDBCPostgres {
    public static void main(String[] args) {
        /// https://jdbc.postgresql.org/documentation/use/

        try {
            String url = "jdbc:postgresql://localhost:5432/rocket_db";
            Properties props = new Properties();
            props.setProperty("user", "postgres");
            props.setProperty("password", "postgres123");
            //props.setProperty("ssl", "true");
            Connection conn = DriverManager.getConnection(url, props);
            System.out.println("conexão realizada.");

//            INSERT INTO tab_cadastro(nome, idade) VALUES ('isa', 2);
            String instrucaoSQL =  "INSERT INTO tab_cadastro(nome, idade) VALUES (?, ?)";
            String nome = "isa";
            int idade = 2;

            PreparedStatement pst = conn.prepareStatement(instrucaoSQL);

            pst.setString(1,nome);
            pst.setInt(2,idade);
            System.out.println("insercao no bd feita com sucesso");

            pst.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }

//        String url = "jdbc:postgresql://localhost/test?user=fred&password=secret&ssl=true";
//        Connection conn = DriverManager.getConnection(url);

    }
}
