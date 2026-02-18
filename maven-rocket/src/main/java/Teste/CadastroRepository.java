package Teste;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CadastroRepository {
    private Connection connection;

    public CadastroRepository(){
        connection = FabricaConexao.getConexao();

    }

    void salvar(Cadastro cadastro){
        try {
            String sql = "INSERT INTO tab_cadastro (nome, idade) VALUES (?,?)";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1,cadastro.getNome());
            ps.setInt(2,cadastro.getIdade());
            ps.execute();
            System.out.println("Inserção concluida com sucesso");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    };

    void alterar(Cadastro cadastro){
        try {
            String sql = "UPDATE  tab_cadastro SET nome=?, idade=? WHERE id=? ";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1,cadastro.getNome());
            ps.setInt(2,cadastro.getIdade());
            ps.setInt(3,cadastro.getId());
            ps.execute();
            System.out.println("Update concluido com sucesso");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    };

    void delete(Integer id){

    }

    public List<Cadastro> listar(){
        List<Cadastro> list = new ArrayList<>();
        try {
            String sql = "SELECT id, nome, idade FROM tab_cadastro";
            PreparedStatement ps = connection.prepareStatement(sql);

            ResultSet resultSet = ps.executeQuery();
            while (resultSet.next()){
                Integer id = resultSet.getInt("id");
                String nome = resultSet.getString("nome");
                Integer idade = resultSet.getInt("idade");

                Cadastro cadastro = new Cadastro();

                cadastro.setId(id);
                cadastro.setNome(nome);
                cadastro.setIdade(idade);

                list.add(cadastro);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public Cadastro buscar(Integer id){
        Cadastro cadastro = null;
        try {
            String sql = "SELECT id, nome, idade FROM tab_cadastro WHERE id= ?";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1,id);

            ResultSet resultSet = ps.executeQuery();
            if (resultSet.next()){

                Integer i = resultSet.getInt("id");
                String nome = resultSet.getString("nome");
                Integer idade = resultSet.getInt("idade");

                cadastro = new Cadastro();

                cadastro.setId(i);
                cadastro.setNome(nome);
                cadastro.setIdade(idade);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return cadastro;
    }
}
