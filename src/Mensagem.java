public class Mensagem {
    //<controle de erro>:<apelido de origem>:<apelido do destino>:<CRC>:<mensagem ou dados do arquivo>.
    private String controleDeErro; //campos[0]
    private String apelidoOrigem; //campos[1]
    private String apelidoDestino; //campos[2]
    private Integer crc; //campos[3]
    private String mensagem; //campos[4]

    public Mensagem(String mensagem) {
        String[] campos = mensagem.split(";");

        controleDeErro = campos[1];
        apelidoOrigem = campos[2];
        apelidoDestino = campos[3];
        crc = Integer.valueOf(campos[4]);
        this.mensagem = campos[5];
    }

    public Mensagem(String mensagemAEnviar, ConfiguracaoDestino configuracao) {
        this.controleDeErro = ControleDeErrosEnum.MAQUINA_NAO_EXISTE.getCampo();
        this.apelidoOrigem = configuracao.getApelido();
        this.apelidoDestino = mensagemAEnviar.split(";")[1];
        this.crc = 123123; //TODO calcular
        this.mensagem = mensagemAEnviar.split(";")[0];
    }

    public String getControleDeErro() {
        return controleDeErro;
    }

    public void setControleDeErro(String controleDeErro) {
        this.controleDeErro = controleDeErro;
    }

    public String getApelidoOrigem() {
        return apelidoOrigem;
    }

    public void setApelidoOrigem(String apelidoOrigem) {
        this.apelidoOrigem = apelidoOrigem;
    }

    public String getApelidoDestino() {
        return apelidoDestino;
    }

    public void setApelidoDestino(String apelidoDestino) {
        this.apelidoDestino = apelidoDestino;
    }

    public Integer getCrc() {
        return crc;
    }

    public void setCrc(Integer crc) {
        this.crc = crc;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }
}
