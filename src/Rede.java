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
    private Boolean retransmitiu;

    public Rede(ConfiguracaoDestino config) {
        this.config = config;
        this.consumirMensagem = new ConsumirMensagem();
        this.listaMensagensEDestinos = new ArrayList<>();
        this.produzirMensagem = new ProduzirMensagem();
        this.controleErro = new ControleErro();
        this.retransmitiu = false;
        this.probabilidadeErro = 0D;
        if(this.config.iniciouToken){
            this.controleToken = new ControleToken(this);
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

                    if(this.config.iniciouToken && this.controleToken.validarTempoMinimoToken()){ // se o tempo do token for menor que o tempo minimo, retira da rede
                        //TODO verificar se tem algo a fazer, creio que nao
                    } else {
                        String proximaMensagem = listaMensagensEDestinos.get(0);
                        Mensagem mensagem = new Mensagem(proximaMensagem, config);

                        String mensagemParaEnviar = remontarPacote(mensagem);
                        produzirMensagem.enviar(config, mensagemParaEnviar);
                    }

                } else if (mensagemRecebida.startsWith("2222")) {

                    Mensagem mensagem = new Mensagem(mensagemRecebida);
                    if (mensagem.getApelidoDestino().equals(config.getApelido())) { //Verifica se a mensagem eh pra maquina atual, se for, calcula crc e faz uma logica
                        Boolean crcCalculado = controleErro.calcular(mensagem.getMensagem().getBytes(StandardCharsets.UTF_8), this.probabilidadeErro).equals(mensagem.getCrc());
                        System.out.printf("apelido origem: %s | mensagem original: %s | mensagemRecebida: %s\n", mensagem.getApelidoOrigem(), mensagem.getMensagem(), mensagemRecebida);

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


    /** FUNCOES PARA O MENU*/
    public void adicionarMensagemNaFila(String mensagem, String destino){
        if(mensagem.contains(";") || destino.contains(SEPARADOR_MENSAGEM)) {
            System.out.printf("Mensagem ou destino invalidos, nao utilizar \"%s\"\n", SEPARADOR_MENSAGEM);
            return;
        }
        String mensagemFinal = mensagem + SEPARADOR_MENSAGEM + destino;
        listaMensagensEDestinos.add(mensagemFinal);
    }

    public void verListaMensagens(){
        System.out.println("_____________________________________");
        System.out.println("LISTA DE MENSAGENS");
        for(String m: listaMensagensEDestinos) {
            System.out.printf("Mensagem: %s | Destino: %s\n", m.split(SEPARADOR_MENSAGEM)[0],m.split(SEPARADOR_MENSAGEM)[1]);
        }
        System.out.println("_____________________________________");
    }

    public void alterarProbabilidadeCrc(String s){
        try {
            Double valor = Double.valueOf(s);
            if(valor < 0 || valor > 100) {
                System.out.println("Somente sera aceito valores entre 0 e 100");
            }
        } catch (Exception e){
            System.out.println("Enviar somente numeros");
        }
    }

    public void verProbabilidadeCrc(){
        System.out.printf("Probabilidade atual: %f\n", this.probabilidadeErro);
    }

    public void inserirTokenNaRede(){
        if(this.config.iniciouToken) {
            produzirMensagem.enviar(config, TOKEN);
        }
    }

    /** TODO
     *
     MAQUINA QUE GEROU TOKEN
     - recebeu mensagem
     - verifica se mensagem eh token
     - se sim, verifica o tempo MINIMO de token (onde definimos esse tempo?)
     - se for menor, retirar o tempo da rede
     - se nao eh token
     - verifica o tempo MAXIMO de token (onde definimos esse tempo?)
     - se for maior, inserir o token na rede

     qualquer maquina insere ou retira token
     */

}
