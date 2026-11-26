package ru.yandex.practicum.exceptions;

public class InvalidWordFormatException extends GameRuleException {
    public InvalidWordFormatException(String message) {
        super(message);
    }
}
