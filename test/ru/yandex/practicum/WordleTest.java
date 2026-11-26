package ru.yandex.practicum;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.exceptions.GameRuleException;
import ru.yandex.practicum.exceptions.InvalidWordFormatException;
import ru.yandex.practicum.exceptions.WordNotFoundInDictionaryException;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WordleTest {

    private static PrintWriter testLog;
    private WordleDictionary dictionary;
    private WordleGame game;

    @BeforeAll
    static void setUpAll() {
        testLog = new PrintWriter(System.out, true, StandardCharsets.UTF_8);
    }

    @BeforeEach
    void setUp() {
        dictionary = new WordleDictionary(List.of(
                "герой", "гонец", "гриль", "голос", "город", "гроза", "глаза", "конец", "покой"
        ));
        game = new WordleGame(dictionary, testLog, "герой");
    }

    @Test
    @DisplayName("Нормализация слова: убираем пробелы, приводим к нижнему регистру и меняем ё на е")
    void shouldNormalizeWordsWithYoAndCase() {
        assertEquals("елка", WordleDictionary.normalize("  ЁЛКА "));
        assertEquals("герой", WordleDictionary.normalize("ГЕРОЙ"));
    }

    @Test
    void shouldEvaluateExactAndMisplacedLetters() {
        String hint = WordleDictionary.evaluateGuess("герой", "гонец");
        assertEquals("+^-^-", hint);
    }

    @Test
    void shouldEvaluateExactMatch() {
        assertEquals("+++++", WordleDictionary.evaluateGuess("герой", "герой"));
    }

    @Test
    void shouldThrowWhenWordNotInDictionaryAndNotDecreaseSteps() {
        assertThrows(WordNotFoundInDictionaryException.class, () -> game.makeGuess("поезд"));
        assertEquals(WordleGame.MAX_STEPS, game.getSteps());
        assertFalse(game.isGameOver());
    }

    @Test
    void shouldThrowWhenWordLengthIsInvalid() {
        assertThrows(InvalidWordFormatException.class, () -> game.makeGuess("кот"));
        assertThrows(InvalidWordFormatException.class, () -> game.makeGuess("программа"));
        assertEquals(WordleGame.MAX_STEPS, game.getSteps());
    }

    @Test
    void shouldDecreaseStepsAndWinWhenWordGuessed() throws GameRuleException {
        String hint = game.makeGuess("гонец");
        assertEquals("+^-^-", hint);
        assertEquals(5, game.getSteps());
        assertFalse(game.isGameOver());

        String winHint = game.makeGuess("герой");
        assertEquals("+++++", winHint);
        assertEquals(4, game.getSteps());
        assertTrue(game.isWon());
        assertTrue(game.isGameOver());
    }

    @Test
    void shouldLoseAfterSixFailedAttempts() throws GameRuleException {
        for (int i = 0; i < WordleGame.MAX_STEPS; i++) {
            game.makeGuess("гонец");
        }
        assertEquals(0, game.getSteps());
        assertTrue(game.isGameOver());
        assertFalse(game.isWon());
    }

    @Test
    void shouldProvideValidSuggestionAfterGuess() throws GameRuleException {
        game.makeGuess("гонец");
        String suggestion = game.getHintWord();
        assertNotNull(suggestion);
        assertEquals(WordleDictionary.WORD_LENGTH, suggestion.length());
        assertTrue(dictionary.contains(suggestion));
    }
}