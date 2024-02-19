package ru;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Main {
    private static final Pattern urlPattern = Pattern.compile(
            "(http|https):\\/\\/([\\w_-]+(?:(?:\\.[\\w_-]+)+))([\\w.,@?^=%&:\\/~+#-]*[\\w@?^=%&\\/~+#-])",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.119 YaBrowser/22.3.0.2430 Yowser/2.5 Safari/537.36";
    private static final String REFERRER = "https://www.google.com";
    /* Ссылка на страницу с ссылками */
    private static final String START_URL = "https://vc.ru/flood/170561-chto-delat-esli-skuchno-500-ssylok-sobrannyh-za-polgoda";
    /* Язык для фильтрации сайта (проверяется по атрибуту lang тэга html) */
    private static final String LANGUAGE = "EN";

    /* Основной метод */
    public static void main(String[] args) {
        Main main = new Main();
        // очищаем старые файлы и создаем новые файлы
        FileUtils.init();
        List<CustomURL> list = main.getCustomURLs(START_URL);
        list = list.stream()
                .peek(customURL -> customURL.setBody(main.getBody(customURL.getUrl())))
                .filter(c -> c.getBody() != null && !c.getBody().isEmpty())
                .collect(Collectors.toList());
        FileUtils.write(list);
    }

    /* Метод для получение тела сайта */
    private String getBody(String urlString) {
        try {
            Document doc = Jsoup.connect(urlString)
                    .userAgent(USER_AGENT)
                    .referrer(REFERRER)
                    .timeout(10000)
                    .get();
            String lang = Optional.ofNullable(doc)
                    .map(d -> d.getElementsByTag("html"))
                    .map(Elements::first)
                    .map(htmlElement -> htmlElement.attribute("lang"))
                    .map(Attribute::getValue)
                    .orElse(null);
            if (LANGUAGE.equalsIgnoreCase(lang)) {
                return doc.body().text();
            }
            return null;
        } catch (IOException e) {
            System.out.println(urlString + " : " + e.getMessage());
            return null;
        }
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
                    .map(el -> el.attribute("href").getValue())
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