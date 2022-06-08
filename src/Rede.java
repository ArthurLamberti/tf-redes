import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Rede extends Thread {

    private ConfiguracaoDestino config;
    private ConsumirMensagem consumirMensagem;
    private List<String> listaMensagensEDestinos;
    private ProduzirMensagem produzirMensagem;
    private final String SEPARADOR_MENSAGEM = ";";
    private ControleErro controleErro;
    private DatagramSocket serverSocket;
    private boolean deveRodar;

    private final String TOKEN = "1111";
    private Boolean retransmitiu;

    public Rede(ConfiguracaoDestino config) {
        this.config = config;
        this.consumirMensagem = new ConsumirMensagem();
        this.listaMensagensEDestinos = new ArrayList<>();
        this.produzirMensagem = new ProduzirMensagem();
        this.controleErro = new ControleErro();
        this.retransmitiu = false;

        listaMensagensEDestinos.add("Mensagem 1;Bob");
        listaMensagensEDestinos.add("Mensagem 2;Bob");
        listaMensagensEDestinos.add("Mensagem 3;Bob");
        listaMensagensEDestinos.add("Mensagem 4;Bob");
        listaMensagensEDestinos.add("Mensagem 5;Bob");
        listaMensagensEDestinos.add("Mensagem 6;Bob");
    }

    int count = 0;

    @Override
    public void run() {
        this.deveRodar = true;
        entrarNaRede();
    }

    public void pararDeRodar() {
        this.deveRodar = false;

        if (Objects.nonNull(this.serverSocket)) {
            this.serverSocket.close();
        }
    }

    private void entrarNaRede() {
        String mensagemRecebida;
        try {
            this.serverSocket = new DatagramSocket(ConfiguracaoLocal.PORTA_LOCAL);
            if (config.getIniciouToken()) {
//                produzirMensagem.enviar(configuracao, configuracao.getApelido() + " - " + count++);

                String proximaMensagem = listaMensagensEDestinos.get(0);
                Mensagem mensagem = new Mensagem(proximaMensagem, config);

                String mensagemParaEnviar = remontarPacote(mensagem);
                produzirMensagem.enviar(config, mensagemParaEnviar);
            }

            while (deveRodar) {
                mensagemRecebida = this.consumirMensagem.consumir(serverSocket);
                if(Objects.isNull(mensagemRecebida)) {
                    continue;
                }
                System.out.println(mensagemRecebida);

                Thread.sleep(config.getTempoToken());
                if (mensagemRecebida.startsWith("1111")) { // Verifica se recebeu token
                    //TODO tira a mensagem da fila e envia
                    String proximaMensagem = listaMensagensEDestinos.get(0);
                    Mensagem mensagem = new Mensagem(proximaMensagem, config);

                    String mensagemParaEnviar = remontarPacote(mensagem);
                    produzirMensagem.enviar(config, mensagemParaEnviar);
                } else if (mensagemRecebida.startsWith("2222")) {

                    Mensagem mensagem = new Mensagem(mensagemRecebida); //TODO FALTA VERIFICAR SE A MENSAGEM EH MINHA
                    if (mensagem.getApelidoDestino().equals(config.getApelido())) { //Verifica se a mensagem eh pra maquina atual, se for, calcula crc e faz uma logica
                        //TODO calcular o CRC
                        Boolean crcCalculado = controleErro.calcular(mensagem.getMensagem().getBytes(StandardCharsets.UTF_8), 100.0).equals(mensagem.getCrc());
                        //TODO IMPRIMIR CAMPOS
                        System.out.printf("apelido origem: %s | mensagem original: %s | mensagemRecebida: %s\n", mensagem.getApelidoOrigem(), mensagem.getMensagem(), mensagemRecebida);

                        //TODO MUDAR CAMPO PARA ACK OU NAK
                        if (crcCalculado) {
                            mensagem.setControleDeErro(ControleDeErrosEnum.ACK.getCampo());
                        } else {
                            mensagem.setControleDeErro(ControleDeErrosEnum.NAK.getCampo());
                        }

                        String mensagemRemontada = remontarPacote(mensagem);
                        produzirMensagem.enviar(config, mensagemRemontada);
                        System.out.printf("Enviou mensagem %s para o vizinho\n", mensagemRemontada);
                    } else if (mensagem.getApelidoOrigem().equals(config.getApelido())) { //Verifica se a maquina atual gerou a mensagem

                        if (mensagem.getControleDeErro().equals(ControleDeErrosEnum.MAQUINA_NAO_EXISTE.getCampo())) { //maquina destino nao se encontra na rede
                            System.out.println("Maquina destino nao se encontra na rede");
                            listaMensagensEDestinos.remove(0);
                            retransmitiu = false;
                        } else if (mensagem.getControleDeErro().equals(ControleDeErrosEnum.NAK.getCampo())) { //maquina destino identificou erro
                            if (retransmitiu) {
                                listaMensagensEDestinos.remove(0);
                                System.out.println("maquina destino identificou erro novamente no pacote, nao sera retransmitido na proxima vez");
                                retransmitiu = false;
                            } else {
                                System.out.println("maquina destino identificou erro no pacote, sera retransmitido na proxima vez");
                                retransmitiu = true;
                            }
                        } else if (mensagem.getControleDeErro().equals(ControleDeErrosEnum.ACK.getCampo())) { //maquina destino recebeu com sucesso
                            System.out.println("Maquina destino recebeu com sucesso");
                            listaMensagensEDestinos.remove(0);
                            retransmitiu = false;
                        }
                        produzirMensagem.enviar(config, TOKEN);
                    } else { // se nao for, manda a mensagem pro vizinho
                        produzirMensagem.enviar(config, mensagemRecebida);
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
