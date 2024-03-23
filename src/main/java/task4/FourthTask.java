package task4;

import com.github.demidko.aot.PartOfSpeech;
import com.github.demidko.aot.WordformMeaning;
import com.google.gson.Gson;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class FourthTask {
    // Путь к папке для чтения файлов
    public static final String DATA_PATH = "C:\\data";
    // Путь к папке для записи файлов
    private static final String DATA_DIRECTORY = "data/";


    // мап(ключ - Id файла, значение - мап(ключ - термин, значение - количество))
    public static Map<Integer, Map<String, Long>> terminMap = new HashMap<>();
    // мап(ключ - Id файла, значение - мап(ключ - лемма, значение - количество))
    public static Map<Integer, Map<String, Long>> lemmaMap = new HashMap<>();
    // мап(ключ - термин, значение - список индексов документов)
    public static Map<String, Set<Integer>> terminIndexesMap = new HashMap<>();
    // мап(ключ - лемма, значение - список индексов документов)
    public static Map<String, Set<Integer>> lemmaIndexesMap = new HashMap<>();
    // мап(ключ - Id файла, значение - количество слов)
    public static Map<Integer, Integer> wordsCountMap = new HashMap<>();

    private static int filesCount = 0;

    /* Основной метод */
    public static void main(String[] args) {
        FourthTask fourthTask = new FourthTask();
        Gson gson = new Gson();
        List<File> files = fourthTask.getFiles();
        filesCount = files.size();
        files.forEach(file -> {
            String text = fourthTask.readFile(file);
            if (text != null) {
                fourthTask.collectStatistics(file.getName(), text);
            }
        });
        fourthTask.countTfIDF();
    }

    /* Сбор статистики по словам */
    public void collectStatistics(String fileName, String text) {
        int index = Integer.parseInt(fileName.replace(".txt", ""));
        String[] words = text.toLowerCase().replaceAll("ё", "е").split(" ");
        wordsCountMap.put(index, words.length);
        terminMap.put(index, Arrays.stream(words)
                .map(WordformMeaning::lookupForMeanings)
                .filter(list -> list != null && !list.isEmpty())
                .filter(list -> {
                    WordformMeaning lemma = list.get(0).getLemma();
                    return !(PartOfSpeech.Pretext).equals(lemma.getPartOfSpeech()) && !(PartOfSpeech.Union).equals(lemma.getPartOfSpeech());
                })
                .map(list -> list.get(0).toString())
                .collect(Collectors.groupingBy(termin -> termin, Collectors.counting())));
        lemmaMap.put(index, Arrays.stream(words)
                .map(WordformMeaning::lookupForMeanings)
                .filter(list -> list != null && !list.isEmpty())
                .map(list -> list.get(0).getLemma())
                .filter(lemma -> !(PartOfSpeech.Pretext).equals(lemma.getPartOfSpeech()) && !(PartOfSpeech.Union).equals(lemma.getPartOfSpeech()))
                .map(lemma -> lemma.toString().toLowerCase())
                .collect(Collectors.groupingBy(lemma -> lemma, Collectors.counting())));
        for (String word : words) {
            List<WordformMeaning> list = WordformMeaning.lookupForMeanings(word);
            if (list != null && !list.isEmpty()) {
                WordformMeaning lemma = list.get(0).getLemma();
                if (!(PartOfSpeech.Pretext).equals(lemma.getPartOfSpeech()) && !(PartOfSpeech.Union).equals(lemma.getPartOfSpeech())) {
                    String keyWord = lemma.toString().toLowerCase();
                    if (lemmaIndexesMap.containsKey(keyWord)) {
                        lemmaIndexesMap.get(keyWord).add(index);
                    } else {
                        Set<Integer> set = new HashSet<>();
                        set.add(index);
                        lemmaIndexesMap.put(keyWord, set);
                    }
                    if (terminIndexesMap.containsKey(word)) {
                        terminIndexesMap.get(word).add(index);
                    } else {
                        Set<Integer> set = new HashSet<>();
                        set.add(index);
                        terminIndexesMap.put(word, set);
                    }
                }
            }
        }
    }

    /* Подсчет TF, TF-IDF */
    public void countTfIDF() {
        // алгоритм для терминов
        for (Integer id : terminMap.keySet()) {
            List<String> data = new ArrayList<>();
            // общее количество слов в текущем файле
            int totalWordsInFile = wordsCountMap.get(id);
            Map<String, Long> terminsForFile = terminMap.get(id);
            for (String word : terminsForFile.keySet()) {
                // количество встреч данного слово в текущем файла
                int countForWordInFile = terminsForFile.get(word).intValue();
                double tf = (double) countForWordInFile / (double) totalWordsInFile;
                double tfIdf = Math.log((double) filesCount / (double) terminIndexesMap.get(word).size());
                data.add(word + " " + tf + " " + tfIdf);
            }
            writeFile("termin-" + id + ".txt", data);
        }

        // алгоритм для лемм
        for (Integer id : lemmaMap.keySet()) {
            List<String> data = new ArrayList<>();
            // общее количество слов в текущем файле
            int totalWordsInFile = wordsCountMap.get(id);
            Map<String, Long> terminsForFile = lemmaMap.get(id);
            for (String word : terminsForFile.keySet()) {
                // количество встреч данного слово в текущем файла
                int countForWordInFile = terminsForFile.get(word).intValue();
                double tf = (double) countForWordInFile / (double) totalWordsInFile;
                double tfIdf = Math.log((double) filesCount / (double) lemmaIndexesMap.get(word).size());
                data.add(word + " " + tf + " " + tfIdf);
            }
            writeFile("lemma-" + id + ".txt", data);
        }
    }

    /* Запись в файл */
    public void writeFile(String fileName, List<String> data) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(DATA_DIRECTORY + fileName))) {
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
