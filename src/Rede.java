import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.List;

public class Rede {

    private ConsumirMensagem consumirMensagem;
    private List<String> listaMensagens;
    private ProduzirMensagem produzirMensagem;

    public Rede(){
        this.consumirMensagem = new ConsumirMensagem();
        this.listaMensagens = new ArrayList<>();
        this.produzirMensagem = new ProduzirMensagem();
    }

    int count = 0;
    public void entrarNaRede(ConfiguracaoDestino configuracao) {
        String mensagem;
        try {
            DatagramSocket serverSocket = new DatagramSocket(ConfiguracaoLocal.PORTA_LOCAL);
            if(configuracao.getIniciouToken()) {
                produzirMensagem.enviar(configuracao, configuracao.getApelido() + " - " + count++);
            }
            while (true) {
                mensagem = this.consumirMensagem.consumir(serverSocket);
                System.out.println(mensagem);
                Thread.sleep(configuracao.getTempoToken());

                produzirMensagem.enviar(configuracao, configuracao.getApelido() + " - " + count++);

                try {
                    Thread.sleep(configuracao.getTempoToken());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
