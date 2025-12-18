package mafia.engine.file;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.json.JSONObject;

public class FileReader {

    public static final JSONObject readJSON(String path) {
        var bytes = readFile(path);
        return bytes == null ? null : new JSONObject(new String(bytes));
    }

    public static final byte[] readFile(String path) {
        return readFile(new File(path));
    }

    public static final byte[] readFile(File file) {
		try {
            return file == null ? null : Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            return null;
        }
	}
}
