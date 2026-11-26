package ru.yandex.practicum;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WordleGame {

    public static final int MAX_STEPS = 6;
    private String answer;
    private int steps;
    private WordleDictionary dictionary;
    private PrintWriter log;
    private final List<String> guesses;
    private final List<String> hints;
    private final Set<String> suggestedWords;
    private boolean won;

    public WordleGame(WordleDictionary dictionary, PrintWriter log) {
        this(dictionary, log, dictionary != null ? dictionary.getRandomWord() : null);
    }

    // Этот конструктор позволит протестировать игру в JUnit с заранее известным загаданным словом
    public WordleGame(WordleDictionary dictionary, PrintWriter log, String presetAnswer) {
        if (dictionary == null) {
            throw new IllegalArgumentException("Словарь не может быть null.");
        }
        String normalizedAnswer = WordleDictionary.normalize(presetAnswer);
        if (normalizedAnswer.length() != WordleDictionary.WORD_LENGTH) {
            throw new IllegalArgumentException("Загаданное слово должно состоять из "
                    + WordleDictionary.WORD_LENGTH + " букв.");
        }

        this.dictionary = dictionary;
        this.log = log;
        this.answer = normalizedAnswer;
        this.steps = MAX_STEPS;
        this.guesses = new ArrayList<>();
        this.hints = new ArrayList<>();
        this.suggestedWords = new HashSet<>();
        this.won = false;

        logState("Игра начата");
    }

    public String makeGuess(String rawInput) throws GameRuleException {
        if (isGameOver()) {
            throw new IllegalStateException("Игра уже завершена. Оставшихся ходов: " + steps + ", победа: " + won);
        }

        String normalized = WordleDictionary.normalize(rawInput);
        validateGuess(normalized);

        String hint = WordleDictionary.evaluateGuess(answer, normalized);
        steps--;
        guesses.add(normalized);
        hints.add(hint);

        if (normalized.equals(answer)) {
            won = true;
        }

        logState("Ход выполнен: слово='" + normalized + "', подсказка='" + hint + "'");
        return hint;
    }

    public String getHintWord() {
        if (isGameOver()) {
            throw new IllegalStateException("Нельзя запросить подсказку: игра уже завершена.");
        }

        Set<String> excluded = new HashSet<>(suggestedWords);
        excluded.addAll(guesses);

        String suggestion = dictionary.suggestWord(guesses, hints, excluded);
        suggestedWords.add(suggestion);
        logState("Выдана подсказка: '" + suggestion + "'");
        return suggestion;
    }

    private void validateGuess(String normalized) throws GameRuleException {
        if (normalized.isEmpty()) {
            throw new InvalidWordFormatException("Введена пустая строка.");
        }
        if (normalized.length() != WordleDictionary.WORD_LENGTH) {
            throw new InvalidWordFormatException("Слово должно состоять ровно из "
                    + WordleDictionary.WORD_LENGTH + " букв.");
        }
        for (int i = 0; i < normalized.length(); i++) {
            char ch = normalized.charAt(i);
            if (ch < 'а' || ch > 'я') {
                throw new InvalidWordFormatException("Слово должно содержать только русские буквы.");
            }
        }
        if (!dictionary.contains(normalized)) {
            throw new WordNotFoundInDictionaryException(normalized);
        }
    }

    public boolean isGameOver() {
        return won || steps <= 0;
    }

    public boolean isWon() {
        return won;
    }

    public int getSteps() {
        return steps;
    }

    public String getAnswer() {
        return answer;
    }

    public List<String> getGuesses() {
        return new ArrayList<>(guesses);
    }

    public List<String> getHints() {
        return new ArrayList<>(hints);
    }

    public String formatGameStatus() {
        StringBuilder sb = new StringBuilder();
        sb.append("Осталось попыток: ").append(steps).append(" из ").append(MAX_STEPS).append('\n');
        for (int i = 0; i < guesses.size(); i++) {
            sb.append("  ").append(i + 1).append(". ")
                    .append(guesses.get(i)).append(" -> ").append(hints.get(i)).append('\n');
        }
        return sb.toString().trim();
    }

    private void logState(String event) {
        if (log != null) {
            log.println("[WordleGame] " + event + " | steps=" + steps + " | won=" + won
                    + " | guessesCount=" + guesses.size());
            log.flush();
        }
    }
}
