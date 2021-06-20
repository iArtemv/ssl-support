# `ОбщегоНазначения.ФиксированныеДанные` (`Common.FixedData`) 

Описание функции:

> Фиксирует данные типов Структура, Соответствие, Массив с учетом вложенности.


## Типизация возвращаемых значений

Функция возвращает ФиксированнаяСтруктура, ФиксированноеСоответствие, ФиксированныйМассив - фиксированные данные,
аналогичные переданным в параметре Данные.

Пример:

```bsl
Процедура Тест()

    ФиксированнаяСтруктура = ОбщегоНазначения.ФиксированныеДанные(Новый Структура("Ключ1","Значение1"));

КонецПроцедуры

Процедура Tест2()

    Параметры = СложнаяСтруктура();
    ФиксированныйМассив = ОбщегоНазначения.ФиксированныеДанные(Параметры);
    // Часть кода для документации
    Для каждого Элемент из ФиксированныйМассив Цикл
        Если Элемент.Ключ1 = 10 ИЛИ Элемент.Ключ2 = "Значение1" Тогда
             // Продолжаем...
        КонецЕсли
    КонецЦикла;

КонецПроцедуры

// Сложная структура.
// 
// Возвращаемое значение:
//  Массив из Структура:
//   * Ключ1 - Число - описание,
//   * Ключ2 - Строка - описание.
//
Функция СложнаяСтруктура()
    Возврат Новый Структура("Ключ1, Ключ2", 10, "Значение1");
КонецФункции
```
