import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

public class Menu {
    private enum MenuOpcoes {
        ADICIONAR_MSG("1", "Adicionar mensagem"),
        PROBABILIDADE_ERRO("2", "Definir probabilidade de erro"),
        INSERIR_TOKEN("3", "Inserir token na rede"),
        REMOVER_TOKEN("4", "Remover token da rede"),
        SAIR("99", "Remover computador da rede");

        public String codigo;
        public String descricao;

        MenuOpcoes(String codigo, String descricao) {
            this.codigo = codigo;
            this.descricao = descricao;
        }
    }

    private final BufferedReader reader;
    private boolean deveRodarMenu;

    public Menu() {
        this.reader = new BufferedReader(
                new InputStreamReader(System.in));
    }

    public void run(Rede rede) throws IOException {
        this.deveRodarMenu = true;
        while (deveRodarMenu) {
            exibirMenu();
            String input = reader.readLine();
            lidarComInput(input, rede);
        }

    }

    private void lidarComInput(String input, Rede rede) throws IOException {
        if (MenuOpcoes.ADICIONAR_MSG.codigo.equals(input)) {
            System.out.println("Digite a sua mensagem: ");
            String msg = reader.readLine();
            //TODO chamar método de enviar mensagem
            System.out.println("Mensagem enviada");
        } else if (MenuOpcoes.PROBABILIDADE_ERRO.codigo.equals(input)) {
            System.out.println("Digite a probabilidade de erro desejada: ");
            String probabilidadeErro = reader.readLine();
            //TODO chamar método de definir probabilidade de erro
            System.out.println("Probabilidade definida: " + probabilidadeErro);
        } else if (MenuOpcoes.INSERIR_TOKEN.codigo.equals(input)) {
            //TODO Chamar método para inserir token na rede
            System.out.println("Token inserido");
        } else if (MenuOpcoes.REMOVER_TOKEN.codigo.equals(input)) {
            //TODO Chamar método para remover token da rede
            System.out.println("Token removido");
        }  else if (MenuOpcoes.SAIR.codigo.equals(input)) {
            this.deveRodarMenu = false;
            rede.pararDeRodar();
            System.out.println("Removendo computador da rede...");
        } else {
            System.out.println("Operação não identificada !!!");
        }
        System.out.println("\n\n\n");
    }

    private void exibirMenu() {
        System.out.println("## -- MENU -- ###");
        Arrays.stream(MenuOpcoes.values()).forEach(opcao -> System.out.println(opcao.codigo + " - " + opcao.descricao));
    }
}
