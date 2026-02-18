package Teste;

import java.sql.SQLException;


public class SistemaCadastro {
    public static void main(String[] args) throws SQLException {
        FabricaConexao.conectar();

        System.out.println("Client Info: "+ FabricaConexao.getConexao().getClientInfo());
        System.out.println("Database: "+ FabricaConexao.getConexao().getCatalog());
        System.out.println("Conection is closed? "+ FabricaConexao.getConexao().isClosed());

        System.out.println();
        CadastroRepository cadastroRepository = new CadastroRepository();
//        Cadastro cadastro1 = new Cadastro();
//        cadastro1.setNome("cadastroAlteradoTest");
//        cadastro1.setIdade(999);
//        cadastro1.setId(2);
//
//        cadastroRepository.alterar(cadastro1);

        cadastroRepository.listar().forEach(obj-> System.out.println("id: "+ obj.getId() +
                " nome: "+ obj.getNome()+" idade: "+ obj.getIdade()));

        Cadastro x = cadastroRepository.buscar(7);
        if(x != null) {
            System.out.println("x id: " + x.getId() + " x nome: " + x.getNome() + " x idade: " + x.getIdade());
        }else {
            System.out.println("nao foi possivel localizar");
        }
    }
}
