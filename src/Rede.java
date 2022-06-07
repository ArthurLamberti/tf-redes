import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.List;

public class Rede {

    private ConsumirMensagem consumirMensagem;
    private List<String> listaMensagensEDestinos;
    private ProduzirMensagem produzirMensagem;
    private final String SEPARADOR_MENSAGEM = ";";

    public Rede(){
        this.consumirMensagem = new ConsumirMensagem();
        this.listaMensagensEDestinos = new ArrayList<>();
        this.produzirMensagem = new ProduzirMensagem();

        listaMensagensEDestinos.add("Mensagem 1;Bob");
        listaMensagensEDestinos.add("Mensagem 2;Bob");
        listaMensagensEDestinos.add("Mensagem 3;Bob");
        listaMensagensEDestinos.add("Mensagem 4;Bob");
        listaMensagensEDestinos.add("Mensagem 5;Bob");
        listaMensagensEDestinos.add("Mensagem 6;Bob");
    }

    int count = 0;
    public void entrarNaRede(ConfiguracaoDestino configuracao) {
        String mensagemRecebida;
        try {
            DatagramSocket serverSocket = new DatagramSocket(ConfiguracaoLocal.PORTA_LOCAL);
            if (configuracao.getIniciouToken()) {
//                produzirMensagem.enviar(configuracao, configuracao.getApelido() + " - " + count++);

                String proximaMensagem = listaMensagensEDestinos.get(0);
                Mensagem mensagem = new Mensagem(proximaMensagem, configuracao);

                String mensagemParaEnviar = remontarPacote(mensagem);
                produzirMensagem.enviar(configuracao,mensagemParaEnviar);
            }
            while (true) {
                mensagemRecebida = this.consumirMensagem.consumir(serverSocket);
                System.out.println(mensagemRecebida);

                Thread.sleep(configuracao.getTempoToken());
                if (mensagemRecebida.startsWith("1111")) { // Verifica se recebeu token
                    //TODO tira a mensagem da fila e envia
                    String proximaMensagem = listaMensagensEDestinos.get(0);
                    Mensagem mensagem = new Mensagem(proximaMensagem, configuracao);

                    String mensagemParaEnviar = remontarPacote(mensagem);
                    produzirMensagem.enviar(configuracao,mensagemParaEnviar);
                } else if (mensagemRecebida.startsWith("2222")) {

                    Mensagem mensagem = new Mensagem(mensagemRecebida); //TODO FALTA VERIFICAR SE A MENSAGEM EH MINHA
                    if (mensagem.getApelidoDestino().equals(configuracao.getApelido())) { //Verifica se a mensagem eh pra maquina atual, se for, calcula crc e faz uma logica
                        //TODO calcular o CRC
                        Boolean crcCalculado = true;
                        //TODO IMPRIMIR CAMPOS
                        System.out.printf("apelido origem: %s | mensagem original: %s | mensagemRecebida: %s\n", mensagem.getApelidoOrigem(), mensagem.getMensagem(), mensagemRecebida);

                        //TODO MUDAR CAMPO PARA ACK OU NAK
                        if (crcCalculado) {
                            mensagem.setControleDeErro(ControleDeErrosEnum.ACK.getCampo());
                        } else {
                            mensagem.setControleDeErro(ControleDeErrosEnum.NAK.getCampo());
                        }

                        String mensagemRemontada = remontarPacote(mensagem);
                        produzirMensagem.enviar(configuracao, mensagemRemontada);
                        System.out.printf("Enviou mensagem %s para o vizinho\n", mensagemRemontada);
                    } else if (mensagem.getApelidoOrigem().equals(configuracao.getApelido())) { //Verifica se a maquina atual gerou a mensagem

                    } else { // se nao for, manda a mensagem pro vizinho
                        produzirMensagem.enviar(configuracao, mensagemRecebida);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private String remontarPacote(Mensagem mensagem) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("2222").append(SEPARADOR_MENSAGEM);
        stringBuilder.append(mensagem.getControleDeErro()).append(SEPARADOR_MENSAGEM);
        stringBuilder.append(mensagem.getApelidoOrigem()).append(SEPARADOR_MENSAGEM);
        stringBuilder.append(mensagem.getApelidoDestino()).append(SEPARADOR_MENSAGEM);
        stringBuilder.append(mensagem.getCrc()).append(SEPARADOR_MENSAGEM);
        stringBuilder.append(mensagem.getMensagem());

        return stringBuilder.toString();
    }
}
