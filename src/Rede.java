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
    private Double probabilidadeErro;
    private ControleToken controleToken;

    private final String TOKEN = "1111";
    private final String TIPO_MENSAGEM = "2222";
    private final String TRANSMISSAO_BROADCAST = "TODOS";
    private Boolean retransmitiu;
    private boolean possuiToken;

    public Rede(ConfiguracaoDestino config) {
        this.possuiToken = false;
        this.config = config;
        this.consumirMensagem = new ConsumirMensagem();
        this.listaMensagensEDestinos = new ArrayList<>();
        this.produzirMensagem = new ProduzirMensagem();
        this.controleErro = new ControleErro();
        this.retransmitiu = false;
        this.probabilidadeErro = 0D;
        if (this.config.iniciouToken) {
            this.controleToken = new ControleToken(this);
            this.possuiToken = true;
        }

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
                produzirMensagem.enviar(config, TOKEN);
                controleToken.resetarTempo();
                possuiToken = false;
            }

            while (deveRodar) {
                mensagemRecebida = this.consumirMensagem.consumir(serverSocket);
                if (Objects.isNull(mensagemRecebida)) {
                    continue;
                }
                System.out.println(mensagemRecebida);
                sleep(config.getTempoToken() * 1000);
                if (mensagemRecebida.startsWith(TOKEN)) { // Verifica se recebeu token
                    possuiToken = true;

                    if (this.config.iniciouToken && this.controleToken.validarTempoMinimoToken()) { // se o tempo do token for menor que o tempo minimo, retira da rede
                        // se entrar aqui eh pq o tempo do token foi menor que o tempo minimo, portanto, saiu da rede
                        possuiToken = false;
                    } else {
                        if (this.listaMensagensEDestinos.isEmpty()) { // se a fila de mensagens estiver vazia, apenas passa o token para a proxima maquina
                            produzirMensagem.enviar(config, TOKEN);
                            possuiToken = false;
                        } else { //se tiver dados, envia a mensagem pra proxima maquina
                            String proximaMensagem = listaMensagensEDestinos.get(0);
                            Mensagem mensagem = new Mensagem(proximaMensagem, config);

                            String mensagemParaEnviar = remontarPacote(mensagem);
                            produzirMensagem.enviar(config, mensagemParaEnviar);
                            possuiToken = true;
                        }
                    }

                } else if (mensagemRecebida.startsWith(TIPO_MENSAGEM)) {

                    Mensagem mensagem = new Mensagem(mensagemRecebida);
                    if (mensagem.getApelidoDestino().equals(config.getApelido())) { //Verifica se a mensagem eh pra maquina atual, se for, calcula crc e faz uma logica
                        //DESTINO
                        boolean crcCalculado = controleErro.calcular(mensagem.getMensagem().getBytes(StandardCharsets.UTF_8), this.probabilidadeErro).equals(mensagem.getCrc());
                        System.out.printf("apelido origem: %s | mensagem original: %s | pacoteRecebido: %s\n", mensagem.getApelidoOrigem(), mensagem.getMensagem(), mensagemRecebida);

                        if (crcCalculado) {
                            mensagem.setControleDeErro(ControleDeErrosEnum.ACK.getCampo());
                        } else {
                            mensagem.setControleDeErro(ControleDeErrosEnum.NAK.getCampo());
                        }

                        String mensagemRemontada = remontarPacote(mensagem);
                        produzirMensagem.enviar(config, mensagemRemontada);
                        System.out.printf("Enviou mensagem \"%s\" para o vizinho\n", mensagemRemontada);
                    } else if (mensagem.getApelidoDestino().equalsIgnoreCase(TRANSMISSAO_BROADCAST) && !mensagem.getApelidoOrigem().equals(config.getApelido())) {
                        //SE FOR MENSAGEM BROADCAST E NAO FOI A MAQUINA ATUAL QUE MANDOU
                        System.out.printf("Mensagem via broadcast: %s", mensagem.getMensagem());

                    } else if (mensagem.getApelidoOrigem().equals(config.getApelido())) { //Verifica se a maquina atual gerou a mensagem
                        //ORIGEM
                        if (mensagem.getControleDeErro().equals(ControleDeErrosEnum.MAQUINA_NAO_EXISTE.getCampo())) { //maquina destino nao se encontra na rede
                            if (mensagem.getApelidoDestino().equalsIgnoreCase(TRANSMISSAO_BROADCAST)) {
                                System.out.println("Mensagem enviada para todas maquinas conectadas");
                            } else {
                                System.out.println("Maquina destino nao se encontra na rede ou esta desligada");
                            }
                            String msgTemp = listaMensagensEDestinos.remove(0);
                            System.out.printf("Removida mensagem \"%s\" e enviando token para proxima maquina\n", msgTemp);
                            retransmitiu = false;
                        } else if (mensagem.getControleDeErro().equals(ControleDeErrosEnum.NAK.getCampo())) { //maquina destino identificou erro
                            if (retransmitiu) {
                                System.out.printf("maquina destino identificou erro novamente no pacote, a mensagem \"%s\" sera removida e nao sera retransmitida na proxima vez\n", mensagem.getMensagem());
                                String msgTemp = listaMensagensEDestinos.remove(0);
                                System.out.printf("Mensagem \"%s\" removida da fila. Token sera enviado para a proxima maquina\n", msgTemp);
                                retransmitiu = false;
                            } else {
                                System.out.printf("maquina destino identificou erro no pacote, a mensagem \"%s\" sera retransmitida na proxima vez. Enviando token para proxima maquina\n", mensagem.getMensagem());
                                retransmitiu = true;
                            }
                        } else if (mensagem.getControleDeErro().equals(ControleDeErrosEnum.ACK.getCampo())) { //maquina destino recebeu com sucesso
                            System.out.printf("Maquina %s recebeu com sucesso a mensagem \"%s\"\n", mensagem.getApelidoDestino(), mensagem.getMensagem());
                            String msgTemp = listaMensagensEDestinos.remove(0);
                            System.out.printf("Mensagem \"%s\" removida da fila. Token sera enviado para a proxima maquina\n", msgTemp);
                            retransmitiu = false;
                        }
                        if (possuiToken) {
                            produzirMensagem.enviar(config, TOKEN);
                        }
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
        stringBuilder.append(TIPO_MENSAGEM).append(SEPARADOR_MENSAGEM);
        stringBuilder.append(mensagem.getControleDeErro()).append(SEPARADOR_MENSAGEM);
        stringBuilder.append(mensagem.getApelidoOrigem()).append(SEPARADOR_MENSAGEM);
        stringBuilder.append(mensagem.getApelidoDestino()).append(SEPARADOR_MENSAGEM);
        stringBuilder.append(mensagem.getCrc()).append(SEPARADOR_MENSAGEM);
        stringBuilder.append(mensagem.getMensagem());

        return stringBuilder.toString();
    }


    /**
     * FUNCOES PARA O MENU
     */
    public void adicionarMensagemNaFila(String mensagem, String destino) {
        if (listaMensagensEDestinos.size() == 10) {
            System.out.println("Numero maximo de mensagens na fila");
            return;
        }
        if (mensagem.contains(";") || destino.contains(SEPARADOR_MENSAGEM)) {
            System.out.printf("Mensagem ou destino invalidos, nao utilizar \"%s\"\n", SEPARADOR_MENSAGEM);
            return;
        }
        String mensagemFinal = mensagem + SEPARADOR_MENSAGEM + destino;
        listaMensagensEDestinos.add(mensagemFinal);
    }

    public void verListaMensagens() {
        System.out.println("_____________________________________");
        System.out.println("LISTA DE MENSAGENS");
        for (String m : listaMensagensEDestinos) {
            System.out.printf("Mensagem: %s | Destino: %s\n", m.split(SEPARADOR_MENSAGEM)[0], m.split(SEPARADOR_MENSAGEM)[1]);
        }
        System.out.println("_____________________________________");
    }

    public void alterarProbabilidadeCrc(String s) {
        try {
            Double valor = Double.valueOf(s);
            if (valor < 0 || valor > 100) {
                System.out.println("Somente sera aceito valores entre 0 e 100");
            }
        } catch (Exception e) {
            System.out.println("Enviar somente numeros");
        }
    }

    public void verProbabilidadeCrc() {
        System.out.printf("Probabilidade atual: %f\n", this.probabilidadeErro);
    }

    public void inserirTokenNaRede() {
        produzirMensagem.enviar(config, TOKEN);
        if (this.config.iniciouToken) { //se for a maquina que iniciou token, reseta o tempo de controle
            controleToken.resetarTempo();
        }
    }

    public void removerToken() {
        if (possuiToken) {
            System.out.println("Removendo token da rede");
            possuiToken = false;
        } else {
            System.out.println("Maquina atual nao possui token, portanto nao sera removido");
        }
    }
}
