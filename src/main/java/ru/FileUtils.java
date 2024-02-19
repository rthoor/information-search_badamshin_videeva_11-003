package ru;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class FileUtils {
    private static final String INDEX_FILE_NAME = "index.txt";
    private static final String DATA_DIRECTORY = "data/";

    /* Очищение файлов со старой итерации и создание новых файлов */
    public static void init() {
        try {
            Path dir = Path.of(DATA_DIRECTORY);
            Path file = Path.of(INDEX_FILE_NAME);
            if (Files.exists(dir)) {
                Files.walk(dir)
                        .map(Path::toFile)
                        .forEach(File::deleteOnExit);
                Files.delete(dir);
            }
            Files.deleteIfExists(file);
            Files.createDirectory(dir);
            Files.createFile(file);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    /* Запись в файлы */
    public static void write(List<CustomURL> customURLS) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(INDEX_FILE_NAME));
            for (int i = 0; i < customURLS.size(); i++) {
                CustomURL element = customURLS.get(i);
                String fileName = i + ".txt";
                Files.write(Paths.get(DATA_DIRECTORY + fileName), element.getBody().getBytes(), StandardOpenOption.CREATE);

                String indexData = i + " - " + element.getUrl() + "\n";
                bw.write(indexData);
                bw.newLine();
            }
            bw.flush();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
