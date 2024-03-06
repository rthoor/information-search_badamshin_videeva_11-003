package task3;

import com.github.demidko.aot.PartOfSpeech;
import com.github.demidko.aot.WordformMeaning;
import com.google.gson.Gson;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class ThirdTask {
    // Путь к папке с файлами
    public static final String DATA_PATH = "C:\\data";
    public static Map<String, Set<Integer>> map = new HashMap<>();

    /* Основной метод */
    public static void main(String[] args) {
        ThirdTask thirdTask = new ThirdTask();
        Gson gson = new Gson();
        List<File> files = thirdTask.getFiles();
        files.forEach(file -> {
            String text = thirdTask.readFile(file);
            if (text != null) {
                thirdTask.process(file.getName(), text);
            }
        });
        List<String> jsonList = map.entrySet().stream()
                .map(entry -> new Json(entry.getValue().size(), new ArrayList<>(entry.getValue()), entry.getKey()))
                .map(gson::toJson)
                .toList();
        thirdTask.writeFile("inverted_index.txt", jsonList);
    }

    /* Поиск */
    private void search() {
        String searchInput = "";
    }

    /* Разбиваем на слова, собираем все в формате ключ - значение (ключ - лемма, значение - массив индексов файлов) */
    public void process(String fileName, String text) {
        int index = Integer.parseInt(fileName.replace(".txt", ""));
        String[] words = text.toLowerCase().split(" ");
        for (String word : words) {
            List<WordformMeaning> list = WordformMeaning.lookupForMeanings(word);
            if (list != null && !list.isEmpty()) {
                WordformMeaning lemma = list.get(0).getLemma();
                if (!(PartOfSpeech.Pretext).equals(lemma.getPartOfSpeech()) && !(PartOfSpeech.Union).equals(lemma.getPartOfSpeech())) {
                    String keyWord = lemma.toString().toLowerCase();
                    if (map.containsKey(keyWord)) {
                        map.get(keyWord).add(index);
                    } else {
                        Set<Integer> set = new HashSet<>();
                        set.add(index);
                        map.put(keyWord, set);
                    }
                }
            }
        }
    }

    /* Запись в файл */
    public void writeFile(String fileName, List<String> data) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName))) {
            for (String line : data) {
                bw.write(line);
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    /* Считывание из файла */
    public String readFile(File file) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            return sb.toString();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    /* Получение файлов в папке */
    public List<File> getFiles() {
        try {
            List<File> files = new ArrayList<>();
            Path dir = Path.of(DATA_PATH);
            if (Files.exists(dir)) {
                return Files.walk(dir)
                        .map(Path::toFile)
                        .collect(Collectors.toList());
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return Collections.emptyList();
    }
}
