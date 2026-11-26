package ru.yandex.practicum;

public class InvalidWordFormatException extends GameRuleException {
    public InvalidWordFormatException(String message) {
        super(message);
    }
}
