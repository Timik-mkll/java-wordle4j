package ru.yandex.practicum;

public class WordNotFoundInDictionaryException extends GameRuleException {
    public WordNotFoundInDictionaryException(String message) {
        super(message);
    }
}
