package task5;

import com.github.demidko.aot.PartOfSpeech;
import com.github.demidko.aot.WordformMeaning;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class FifthTask {
    // Путь к папке для чтения файлов
    public static final String DATA_PATH = "C:\\data";

    // мап(ключ - Id файла, значение - мап(ключ - лемма, значение - количество))
    public static Map<Integer, Map<String, Long>> lemmaMap = new HashMap<>();
    // мап(ключ - лемма, значение - список индексов документов)
    public static Map<String, Set<Integer>> lemmaIndexesMap = new HashMap<>();
    // мап(ключ - Id файла, значение - количество слов)
    public static Map<Integer, Integer> wordsCountMap = new HashMap<>();
    // мап(ключ - файл, значение - мап(ключ - лемма, значение - TF))
    public static Map<Integer, Map<String, BigDecimal>> fileWordTFMap = new HashMap<>();

    private static int filesCount = 0;

    /* Основной метод */
    public static void main(String[] args) {
        FifthTask fifthTask = new FifthTask();
        List<File> files = fifthTask.getFiles();
        filesCount = files.size();
        files.forEach(file -> {
            String text = fifthTask.readFile(file);
            if (text != null) {
                fifthTask.collectStatistics(file.getName(), text);
            }
        });
        fifthTask.countTfIDF();

        fifthTask.searchMethod();
    }

    /* Поиск */
    public void searchMethod() {
        searchByWord("делать");
        searchByWord("делаю");
        searchByWord("язык");
        searchByWord("спасибо");
    }

    public void searchByWord(String word) {
        List<String> result = search(word);
        System.out.println("Результат для слова: " + word);
        result.forEach(System.out::println);
        System.out.println();
    }


    public List<String> search(String word) {
        List<WordformMeaning> list = WordformMeaning.lookupForMeanings(word);
        if (list != null && !list.isEmpty()) {
            WordformMeaning lemma = list.get(0).getLemma();
            if (!(PartOfSpeech.Pretext).equals(lemma.getPartOfSpeech()) && !(PartOfSpeech.Union).equals(lemma.getPartOfSpeech())) {
                String keyWord = lemma.toString().toLowerCase();
                return getTFTopForWord(keyWord);
            }
            return Collections.emptyList();
        }
        return Collections.emptyList();
    }

    public List<String> getTFTopForWord(String word) {
        return fileWordTFMap.keySet().stream()
                .filter(key -> fileWordTFMap.get(key).get(word) != null)
                .map(key -> Map.entry(key, fileWordTFMap.get(key).get(word)))
                .sorted((o1, o2) -> o2.getValue().compareTo(o1.getValue()))
                .limit(10)
                .map(entry -> entry.getKey() + " - " + entry.getValue())
                .collect(Collectors.toList());
    }

    /* Сбор статистики по словам */
    public void collectStatistics(String fileName, String text) {
        int index = Integer.parseInt(fileName.replace(".txt", ""));
        String[] words = text.toLowerCase().replaceAll("ё", "е").split(" ");
        wordsCountMap.put(index, words.length);
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
                }
            }
        }
    }

    /* Подсчет TF, TF-IDF */
    public void countTfIDF() {
        // алгоритм для лемм
        for (Integer id : lemmaMap.keySet()) {
            // общее количество слов в текущем файле
            int totalWordsInFile = wordsCountMap.get(id);
            Map<String, Long> lemmasForFile = lemmaMap.get(id);
            Map<String, BigDecimal> tfidfForFile = new HashMap<>();
            for (String word : lemmasForFile.keySet()) {
                // количество встреч данного слово в текущем файла
                int countForWordInFile = lemmasForFile.get(word).intValue();
                float tf = (float) countForWordInFile / (float) totalWordsInFile;
                tfidfForFile.put(word, BigDecimal.valueOf(tf).setScale(4, RoundingMode.HALF_UP));
            }
            fileWordTFMap.put(id, tfidfForFile);
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
