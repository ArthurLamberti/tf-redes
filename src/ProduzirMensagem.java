import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ProduzirMensagem {
    public void enviar(ConfiguracaoDestino configuracao, String message) {
        // cria o stream do teclado
        // declara socket cliente
        try {
            DatagramSocket clientSocket = new DatagramSocket();

            // obtem endere�o IP do servidor com o DNS
            InetAddress IPAddress = InetAddress.getByName(configuracao.getIpDestino());

            byte[] sendData = new byte[1024];

            // l� uma linha do teclado
            sendData = message.getBytes();

            // cria pacote com o dado, o endere�o do server e porta do servidor
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, configuracao.getPortaDestino());

            //envia o pacote
            clientSocket.send(sendPacket);

            // fecha o cliente
            clientSocket.close();
        } catch (Exception ignored){}
    }
}
