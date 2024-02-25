package task1;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
/* Класс для сайтов

* Поля:
* - url - ссылка на сайт
* - topDomain - домен верхнего уровня (для проверки, что сайты действительно отличаются)
* - body - тело сайта
*  */
public class CustomURL {
        private String url;

        @EqualsAndHashCode.Include
        private String topDomain;

        private String body;
}
