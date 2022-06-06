import java.nio.ByteBuffer;
import java.util.zip.CRC32;

public class ControleErro {

    private final CRC32 CHECKSUM = new CRC32();

    public Long calcular(byte[] bytes, double percentagemErro) {
        CHECKSUM.reset();

        if (deveInserirErro(percentagemErro)) {
            double aleatorioParaErro = Math.random();
            byte[] bytesParaErro = converterDoubleParaBytes(aleatorioParaErro);
            CHECKSUM.update(bytesParaErro);
        } else {
            CHECKSUM.update(bytes);
        }

        return CHECKSUM.getValue();
    }

    public Long calcular(byte[] bytes) {
        return calcular(bytes, 0.0);
    }

    private boolean deveInserirErro(double percentagemErro) {
        double aleatorio = Math.random() * 100;
        return aleatorio < percentagemErro;
    }

    private byte[] converterDoubleParaBytes(double number) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(Double.BYTES);
        byteBuffer.putDouble(number);
        return byteBuffer.array();
    }
}
