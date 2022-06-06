import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static java.util.Objects.isNull;

public class LerConfiguracaoDestino {

    public ConfiguracaoDestino ler(){
        try {
            Path path = Path.of("config.txt");
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            String ipDestino = lines.get(0).split(":")[0];
            Integer portaDestino  = Integer.valueOf(lines.get(0).split(":")[1]);
            String apelido = lines.get(1);
            Integer tempoToken = Integer.valueOf(lines.get(2));
            Boolean iniciouToken = Boolean.valueOf(lines.get(3));

            if(isNull(ipDestino)  || isNull(apelido)){
                return null;
            }

            return new ConfiguracaoDestino(ipDestino, portaDestino, apelido, tempoToken, iniciouToken);
        } catch (IOException e) {
            return null;
        }
    }

}
