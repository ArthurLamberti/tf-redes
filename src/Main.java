import static java.util.Objects.isNull;

public class Main {
    public static void main(String[] args) {
        LerConfiguracaoDestino lerConfiguracao = new LerConfiguracaoDestino();
        Rede rede = new Rede();
        ConfiguracaoDestino configuracao = lerConfiguracao.ler();

        if(isNull(configuracao)) {
            System.out.println("Erro ao ler arquivo de configuracao");
            return;
        }

        rede.entrarNaRede(configuracao);
    }
}
