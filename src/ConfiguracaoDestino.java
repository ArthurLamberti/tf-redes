public class ConfiguracaoDestino {

    String ipDestino;
    Integer portaDestino;
    String apelido;
    Integer tempoToken;
    Boolean iniciouToken;

    public ConfiguracaoDestino(String ipDestino, Integer portaDestino, String apelido, Integer tempoToken, Boolean iniciouToken) {
        this.ipDestino = ipDestino;
        this.portaDestino = portaDestino;
        this.apelido = apelido;
        this.tempoToken = tempoToken;
        this.iniciouToken = iniciouToken;
    }

    public String getIpDestino() {
        return ipDestino;
    }

    public Integer getPortaDestino() {
        return portaDestino;
    }

    public String getApelido() {
        return apelido;
    }

    public Integer getTempoToken() {
        return tempoToken;
    }

    public Boolean getIniciouToken() {
        return iniciouToken;
    }
}
