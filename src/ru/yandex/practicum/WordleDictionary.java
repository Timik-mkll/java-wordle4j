package ru.yandex.practicum;

import java.util.*;

public class WordleDictionary {
    public static final int WORD_LENGTH = 5;
    public static final int RUSSIAN_ALPHABET_SIZE = 32;

    private final List<String> words;
    private final Set<String> wordSet;
    private final Random random;

    public WordleDictionary(List<String> words) {
        this.words = new ArrayList<>(words);
        this.wordSet = new HashSet<>(words);
        this.random = new Random();
    }

    // Конструктор для тестов
    public WordleDictionary(List<String> words, Random random) {
        this.words = new ArrayList<>(words);
        this.wordSet = new HashSet<>(words);
        this.random = random;
    }

    public static String normalize(String word) {
        if (word == null) {
            return "";
        }
        return word.trim().toLowerCase().replace('ё', 'е');
    }

    public boolean contains(String word) {
        return wordSet.contains(normalize(word));
    }

    public int size() {
        return words.size();
    }

    public List<String> getWords() {
        return words;
    }

    public String getRandomWord() {
        int index = random.nextInt(words.size());
        return words.get(index);
    }

    public static String evaluateGuess(String secret, String guess) {
        String normSecret = normalize(secret);
        String normGuess = normalize(guess);

        if (normSecret.length() != WORD_LENGTH || normGuess.length() != WORD_LENGTH) {
            throw new IllegalArgumentException(
                    "Оба слова должны состоять из " + WORD_LENGTH + " букв."
            );
        }

        char[] hint = new char[WORD_LENGTH];
        int[] availableSecretLetters = new int[RUSSIAN_ALPHABET_SIZE];

        for (int i = 0; i < WORD_LENGTH; i++) {
            char secretChar = normSecret.charAt(i);
            char guessChar = normGuess.charAt(i);

            if (secretChar == guessChar) {
                hint[i] = '+';
            } else {
                int secretLetterIndex = russianLetterIndex(secretChar);
                if (secretLetterIndex >= 0) {
                    availableSecretLetters[secretLetterIndex]++;
                }
            }
        }

        for (int i = 0; i < WORD_LENGTH; i++) {
            if (hint[i] == '+') {
                continue;
            }

            char guessChar = normGuess.charAt(i);
            int guessLetterIndex = russianLetterIndex(guessChar);

            if (guessLetterIndex >= 0 && availableSecretLetters[guessLetterIndex] > 0) {
                hint[i] = '^';
                availableSecretLetters[guessLetterIndex]--;
            } else {
                hint[i] = '-';
            }
        }

        return new String(hint);
    }

    public String suggestWord(List<String> previousGuesses, List<String> previousHints, Set<String> excludedWords) {
        if (previousGuesses == null || previousHints == null || previousGuesses.size() != previousHints.size()) {
            throw new IllegalArgumentException("Некорректная история попыток и подсказок.");
        }

        List<String> candidates = new ArrayList<>();
        List<String> fallbackCandidates = new ArrayList<>();

        for (String candidate : words) {
            if (isCompatibleWithHistory(candidate, previousGuesses, previousHints)) {
                fallbackCandidates.add(candidate);
                if (excludedWords == null || !excludedWords.contains(candidate)) {
                    candidates.add(candidate);
                }
            }
        }

        if (!candidates.isEmpty()) {
            return candidates.get(random.nextInt(candidates.size()));
        }
        if (!fallbackCandidates.isEmpty()) {
            return fallbackCandidates.get(random.nextInt(fallbackCandidates.size()));
        }
        return getRandomWord();
    }

    private boolean isCompatibleWithHistory(String candidate, List<String> guesses, List<String> hints) {
        for (int i = 0; i < guesses.size(); i++) {
            String guess = guesses.get(i);
            String hint = hints.get(i);
            if (!evaluateGuess(candidate, guess).equals(hint)) {
                return false;
            }
        }
        return true;
    }

    private static int russianLetterIndex(char ch) {
        if (ch >= 'а' && ch <= 'я') {
            return ch - 'а';
        }
        return -1;
    }
}
