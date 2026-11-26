package ru.yandex.practicum.exceptions;

public class WordNotFoundInDictionaryException extends GameRuleException {
    public WordNotFoundInDictionaryException(String message) {
        super(message);
    }
}
