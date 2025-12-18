package cli;

import java.nio.file.Path;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class Loader {
    
    public static <T> T load(String path, Class<T> clazz) throws Exception {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        return mapper.readValue(Path.of(path).toFile(), clazz);
    }
}