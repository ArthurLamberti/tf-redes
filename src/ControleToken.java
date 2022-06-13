public class ControleToken extends Thread {

    private Double tempoMaximoTimeout;
    private Double tempoMinimo;
    private Rede rede;
    private Long tempoDecorrido;
    private Boolean contando;
    private Long tempoInicial;

    public ControleToken(Rede rede) {
        this.tempoMaximoTimeout = ConfiguracaoLocal.TEMPO_TIMEOUT_TOKEN;
        this.tempoMinimo = ConfiguracaoLocal.TEMPO_MINIMO_TOKEN;
        this.rede = rede;
        this.tempoDecorrido = 0L;
        this.contando = false;
    }

    @Override
    public void run() {
        while (true) {
            this.tempoDecorrido = System.currentTimeMillis() - this.tempoInicial;

//            System.out.println(this.tempoDecorrido);

            if (this.tempoDecorrido / 1000.0 > tempoMaximoTimeout) {
                System.out.println("Timeout token. Inserindo mais um token na rede");
                this.contando = false;
                this.rede.inserirTokenNaRede();
            }
        }
    }

    public void resetarTempo() {
        this.tempoDecorrido = 0L;
        this.contando = true;
        this.tempoInicial = System.currentTimeMillis();
    }

    public boolean validarTempoMinimoToken() {
        if (this.tempoDecorrido / 1000.0 < this.tempoMinimo) {
            return true;
        }
        return false;
    }

}
