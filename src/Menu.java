import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

public class Menu {
    private enum MenuOpcoes {
        LISTAR_MENU("0", "Listar menu"),
        ADICIONAR_MSG("1", "Adicionar mensagem"),
        LISTAR_MENSAGENS("2", "Listar mensagens da maquina atual"),
        PROBABILIDADE_ERRO("3", "Definir probabilidade de erro"),
        VER_PROBABILIDADE_ERRO("4", "Ver probabilidade de erro atual"),
        INSERIR_TOKEN("5", "Inserir token na rede"),
        REMOVER_TOKEN("6", "Remover token da rede"),
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
    private Rede rede;

    public Menu(Rede rede) {
        this.reader = new BufferedReader(
                new InputStreamReader(System.in));
        this.rede = rede;
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
        if (MenuOpcoes.LISTAR_MENU.codigo.equals(input)) { // 0
            exibirMenu();
        } else if (MenuOpcoes.ADICIONAR_MSG.codigo.equals(input)) { // 1
            Utils.printarLinha("Digite a sua mensagem: ");
            String msg = reader.readLine();
            Utils.printarLinha("Digite o destino: ");
            String dest = reader.readLine();

            this.rede.adicionarMensagemNaFila(msg, dest);
            Utils.printarLinha("Mensagem adicionada");
            Utils.printarLinhaBaixo();
        } else if (MenuOpcoes.LISTAR_MENSAGENS.codigo.equals(input)) { // 2
            this.rede.verListaMensagens();
        } else if (MenuOpcoes.PROBABILIDADE_ERRO.codigo.equals(input)) { // 3
            Utils.printarLinha("Digite a probabilidade de erro desejada: ");
            String probabilidadeErro = reader.readLine();
            this.rede.alterarProbabilidadeCrc(probabilidadeErro);
            Utils.printarLinha("Probabilidade definida: " + probabilidadeErro);
            Utils.printarLinhaBaixo();
        } else if (MenuOpcoes.VER_PROBABILIDADE_ERRO.codigo.equals(input)) { // 4
            this.rede.verProbabilidadeCrc();
        } else if (MenuOpcoes.INSERIR_TOKEN.codigo.equals(input)) { // 5
            this.rede.inserirTokenNaRede();
            Utils.printarLinha("Token inserido");
            Utils.printarLinhaBaixo();
        } else if (MenuOpcoes.REMOVER_TOKEN.codigo.equals(input)) { // 6
            this.rede.removerToken();
            Utils.printarLinha("Token removido");
            Utils.printarLinhaBaixo();
        } else if (MenuOpcoes.SAIR.codigo.equals(input)) { // 99
            this.deveRodarMenu = false;
            rede.pararDeRodar();
            Utils.printarLinha("Removendo computador da rede...");
            Utils.printarLinhaBaixo();
        } else {
            Utils.printarLinha("Operação não identificada, selecionar uma opcao validao");
            Utils.printarLinhaBaixo();
            exibirMenu();
        }
        System.out.println("\n\n");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void exibirMenu() {
        ArrayList<String> menu = new ArrayList<>();
        menu.add("## -- MENU -- ##");

        Arrays.stream(MenuOpcoes.values()).forEach(opcao -> menu.add(opcao.codigo + " - " + opcao.descricao));
        Utils.printar(menu);
    }
}
