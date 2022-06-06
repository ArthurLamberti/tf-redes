public enum ControleDeErrosEnum {
    ACK("ACK"),
    NAK("NAK"),
    MAQUINA_NAO_EXISTE("maquinanaoexiste");

    private String campo;

    ControleDeErrosEnum(String campo) {
        this.campo = campo;
    }

    public String getCampo(){
        return this.campo;
    }

}
