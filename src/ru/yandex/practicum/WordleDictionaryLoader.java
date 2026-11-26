package ru.yandex.practicum;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class WordleDictionaryLoader {
    private final PrintWriter log;

    public WordleDictionaryLoader(PrintWriter log) {
        this.log = log;
    }

    public WordleDictionary loadDictionary(String filePath) throws IOException {
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("Путь к файлу словаря не задан.");
        }

        Set<String> uniqueWords = new LinkedHashSet<>();
        int totalLinesRead = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                totalLinesRead++;
                String normalized = WordleDictionary.normalize(line);
                if (isFiveLetterRussianWord(normalized)) {
                    uniqueWords.add(normalized);
                }
            }
        }

        if (uniqueWords.isEmpty()) {
            throw new IOException("В файле '" + filePath + "' не найдено ни одного корректного слова из "
                    + WordleDictionary.WORD_LENGTH + " букв.");
        }

        List<String> resultWords = new ArrayList<>(uniqueWords);
        if (log != null) {
            log.println("Загружен словарь из '" + filePath + "'. Прочитано строк: "
                    + totalLinesRead + ", отобрано 5-буквенных слов: " + resultWords.size());
            log.flush();
        }

        return new WordleDictionary(resultWords);
    }

    private boolean isFiveLetterRussianWord(String word) {
        if (word.length() != WordleDictionary.WORD_LENGTH) {
            return false;
        }
        for (int i = 0; i < word.length(); i++) {
            char ch = word.charAt(i);
            if (ch < 'а' || ch > 'я') {
                return false;
            }
        }
        return true;
    }
}