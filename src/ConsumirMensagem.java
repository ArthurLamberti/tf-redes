import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class ConsumirMensagem {
    public String consumir(DatagramSocket serverSocket) {

        // recebe o pacote do cliente
        try {

            byte[] receiveData = new byte[ConfiguracaoLocal.TAMANHO_PACOTE];

            // declara o pacote a ser recebido
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket);
            // pega os dados, o endere�o IP e a porta do cliente
            // para poder mandar a msg de volta

            return new String(receivePacket.getData());

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
