package task3;

import java.util.*;
import java.util.stream.Collectors;

public class SearchService {
    private Map<String, Set<Integer>> map;
    public Set<Integer> allIndexes;
    public SearchService(Map<String, Set<Integer>> map, Set<Integer> allIndexes) {
        this.allIndexes = allIndexes;
        this.map = map;
    }

    public Set<Integer> search(String value) {
        try {
            return parseConditionFromString(value.toLowerCase()).stream()
                    .sorted()
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        } catch (Exception e) {
            System.err.println("Недопустимый формат");
            return Collections.emptySet();
        }
    }

    /* Алгоритм */
    public Set<Integer> parseConditionFromString(String input) {
        // Удаляем пробелы, оставляем только скобки, операторы и слова
        List<String> elements = Arrays.stream(input
                .replaceAll("\\(", "( ")
                .replaceAll("\\)", " )")
                .split(" "))
                .filter(x -> !x.isEmpty() && !x.isBlank())
                .toList();
        List<Object> total = new ArrayList<>(elements);

        // Пока есть закрывающая скобка, ищем открывающую. Проводим операции внутри скобок
        while (total.contains(")")) {
            int closeIndex = total.indexOf(")");
            int openIndex = 0;
            for (int i = closeIndex; i >= 0; i--) {
                if (total.get(i).equals("(")) {
                    openIndex = i;
                    break;
                }
            }
            List<Object> current = total.subList(openIndex + 1, closeIndex);

            // Вызов метода обработки элементов внутри скобок
            Set<Integer> resultOfSublist = getIndexesOfText(current);
            total.set(openIndex, resultOfSublist);
            int indexToRemove = openIndex + 1;
            int removeCount = closeIndex - openIndex;
            for (int j = 0; j < removeCount; j++) {
                total.remove(indexToRemove);
            }
        }
        return getIndexesOfText(total);
    }

    /* Обработка внутри скобок */
    private Set<Integer> getIndexesOfText(List<Object> elements) {
        List<Object> total = new ArrayList<>(elements);
        int size = total.size();

        // Ищем все НЕ и обрабатываем соседний элемент (после)
        for (int i = 0; i < size; i++) {
            if (total.get(i).equals("not")) {
                final int j = i;
                Set<Integer> setForWord = map.containsKey(elements.get(j + 1)) ? map.get(elements.get(j + 1)) : new HashSet<>();
                Set<Integer> set = allIndexes.stream()
                        .filter(ind -> !setForWord.contains(ind))
                        .collect(Collectors.toSet());
                total.set(i, set);
                total.remove(i + 1);
                size--;
            }
        }

        // Ищем все И и обрабатываем соседние элементы (до и после)
        for (int i = 0; i < size; i++) {
            if (total.get(i).equals("and")) {
                Object prev = total.get(i - 1);
                Object next = total.get(i + 1);
                Set<Integer> prevSet;
                Set<Integer> nextSet;
                if (prev instanceof String) {
                    String s = (String) prev;
                    prevSet = map.containsKey(s) ? map.get(s) : new HashSet<>();
                } else {
                    prevSet = (Set<Integer>) prev;
                }
                if (next instanceof String) {
                    String s = (String) next;
                    nextSet = map.containsKey(s) ? map.get(s) : new HashSet<>();
                } else {
                    nextSet = (Set<Integer>) next;
                }
                prevSet.retainAll(nextSet);
                total.set(i - 1, prevSet);
                total.remove(i);
                total.remove(i);
                size = size - 2;
            }
        }

        // Ищем все ИЛИ и обрабатываем соседние элементы (до и после)
        for (int i = 0; i < size; i++) {
            if (total.get(i).equals("or")) {
                Object prev = total.get(i - 1);
                Object next = total.get(i + 1);
                Set<Integer> prevSet;
                Set<Integer> nextSet;
                if (prev instanceof String) {
                    String s = (String) prev;
                    prevSet = map.containsKey(s) ? map.get(s) : new HashSet<>();
                } else {
                    prevSet = (Set<Integer>) prev;

                }
                if (next instanceof String) {
                    String s = (String) next;
                    nextSet = map.containsKey(s) ? map.get(s) : new HashSet<>();
                } else {
                    nextSet = (Set<Integer>) next;
                }
                prevSet.addAll(nextSet);
                total.set(i - 1, prevSet);
                total.remove(i);
                total.remove(i);
                size = size - 2;
            }
        }
        // Случай, когда элемент только один - одно слова
        if (total.get(0) instanceof String) {
            String s = (String) total.get(0);
            return map.containsKey(s) ? map.get(s) : new HashSet<>();
        }
        return (Set<Integer>) total.get(0);
    }
}
