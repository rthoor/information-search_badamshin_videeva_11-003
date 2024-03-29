package task1;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FirstTask {
    private static final Pattern urlPattern = Pattern.compile(
            "(http|https):\\/\\/([\\w_-]+(?:(?:\\.[\\w_-]+)+))([\\w.,@?^=%&:\\/~+#-]*[\\w@?^=%&\\/~+#-])",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.119 YaBrowser/22.3.0.2430 Yowser/2.5 Safari/537.36";
    private static final String REFERRER = "https://www.google.com";
    /* Ссылка на страницу с ссылками */
    private static final String START_URL = "https://infoselection.ru/pokupki2/item/1009-300-luchshikh-onlajn-servisov-rossiya";
    /* Язык для фильтрации сайта (проверяется по атрибуту lang тэга html) */
    private static final String LANGUAGE = "RU";
    private static final Set<String> removeTags = Set.of("script", "style", "meta", "img", "video");

    /* Основной метод */
    public static void main(String[] args) {
        FirstTask firstTask = new FirstTask();
        // очищаем старые файлы и создаем новые файлы
        FileUtils.init();
        List<CustomURL> list = firstTask.getCustomURLs(START_URL);
        list = list.stream()
                .peek(customURL -> customURL.setBody(firstTask.getBody(customURL.getUrl())))
                .filter(c -> c.getBody() != null && !c.getBody().isEmpty())
                .collect(Collectors.toList());
        FileUtils.write(list);
    }

    /* Метод для получение тела сайта без лишних тэгов */
    private String getBody(String urlString) {
        try {
            Document doc = Jsoup.connect(urlString)
                    .userAgent(USER_AGENT)
                    .referrer(REFERRER)
                    .timeout(10000)
                    .get();
            if (doc.text().length() > 500 && checkLanguageByText(doc)) {
                for (String tag : removeTags) {
                    Elements elementsToRemove = doc.getElementsByTag(tag);
                    doc.getAllElements().removeAll(elementsToRemove);
                }
                return doc.body().toString();
            }
            return null;
        } catch (IOException e) {
            System.out.println(urlString + " : " + e.getMessage());
            return null;
        }
    }

    public boolean checkLanguageByTag(Element doc) {
        String lang = Optional.ofNullable(doc)
                .map(d -> d.getElementsByTag("html"))
                .map(Elements::first)
                .map(htmlElement -> htmlElement.attribute("lang"))
                .map(Attribute::getValue)
                .orElse(null);
        return LANGUAGE.equalsIgnoreCase(lang);
    }

    public boolean checkLanguageByText(Element doc) {
        List<String> letters = "RU".equals(LANGUAGE) ?
                Arrays.asList("а", "е", "и", "о") :
                Arrays.asList("a", "e", "i", "o");
        return letters.stream().allMatch(let -> doc.text().contains(let));
    }

    /* Метод получения всех ссылок с сайта-каталога */
    private List<CustomURL> getCustomURLs(String urlString) {
        try {
            Document doc = Jsoup.connect(urlString)
                    .userAgent(USER_AGENT)
                    .referrer(REFERRER)
                    .timeout(30000)
                    .get();
            return doc.getElementsByTag("a")
                    .stream()
                    .map(el -> el.attribute("href"))
                    .filter(Objects::nonNull)
                    .map(Attribute::getValue)
                    .filter(url -> url.matches(urlPattern.pattern()))
                    .map(url -> CustomURL.builder()
                            .url(url)
                            .topDomain(getTopDomain(url))
                            .build())
                    .distinct()
                    .toList();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return Collections.emptyList();
        }
    }

    /* Получение домена верхнего уровня для ссылки */
    private String getTopDomain(String urlString) {
        try {
            URI uri = new URI(urlString);
            String domain = uri.getHost();
            return domain.startsWith("www.") ? domain.substring(4) : domain;
        } catch (URISyntaxException e) {
            return null;
        }
    }
}