import java.util.Scanner;

import static java.util.Objects.isNull;

public class Main {
    public static void main(String[] args) {
        LerConfiguracaoDestino lerConfiguracao = new LerConfiguracaoDestino();
        ConfiguracaoDestino configuracao = lerConfiguracao.ler();

        if(isNull(configuracao)) {
            System.out.println("Erro ao ler arquivo de configuracao");
            return;
        }
        Rede rede = new Rede(configuracao);

        rede.start();

    }
}
