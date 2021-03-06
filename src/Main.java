import java.io.IOException;
import java.util.Collections;

import static java.util.Objects.isNull;

public class Main {
    public static void main(String[] args) throws IOException {
        LerConfiguracaoDestino lerConfiguracao = new LerConfiguracaoDestino();
        ConfiguracaoDestino configuracao = lerConfiguracao.ler();

        if (isNull(configuracao)) {
            System.out.println("Erro ao ler arquivo de configuracao");
            return;
        }
        Rede rede = new Rede(configuracao);
        Menu menu = new Menu(rede);

        Utils.printar(Collections.singletonList("Iniciando maquina na rede"));

        rede.start();
        menu.run(rede);
    }
}
