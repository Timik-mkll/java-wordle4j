package ru.yandex.practicum;

import ru.yandex.practicum.exceptions.GameRuleException;
import ru.yandex.practicum.exceptions.InvalidWordFormatException;
import ru.yandex.practicum.exceptions.WordNotFoundInDictionaryException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Wordle {

    private static final String DICTIONARY_FILE = "words_ru.txt";
    private static final String LOG_FILE = "wordle.log";


    public static void main(String[] args) {
        try (PrintWriter log = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(LOG_FILE), StandardCharsets.UTF_8), true)) {

            runApplication(log);

        } catch (Exception e) {
            System.out.println("Произошла внутренняя ошибка работы программы. Подробности сохранены в лог-файл.");
            e.printStackTrace();
        }
    }

    private static void runApplication(PrintWriter log) {
        WordleDictionaryLoader loader = new WordleDictionaryLoader(log);
        WordleDictionary dictionary;

        try {
            dictionary = loader.loadDictionary(DICTIONARY_FILE);
        } catch (IOException | IllegalArgumentException e) {
            log.println("Ошибка загрузки словаря: " + e.getMessage());
            e.printStackTrace(log);
            log.flush();
            System.out.println("Не удалось загрузить словарь из файла '" + DICTIONARY_FILE + "'. Работа завершена.");
            return;
        }

        WordleGame game = new WordleGame(dictionary, log);
        runGameLoop(game, log);
    }

    private static void runGameLoop(WordleGame game, PrintWriter log) {
        Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);
        int remainingHints = 5;

        System.out.println("Добро пожаловать в Wordle!");
        System.out.println("Угадайте существительное из " + WordleDictionary.WORD_LENGTH
                + " букв за " + WordleGame.MAX_STEPS + " попыток.");
        System.out.println("Нажмите Enter на пустой строке, чтобы получить подсказку от компьютера.");

        while (!game.isGameOver()) {
            System.out.println("\nОсталось попыток: " + game.getSteps() + " | Осталось подсказок: " + remainingHints);
            System.out.print("> ");

            if (!scanner.hasNextLine()) {
                log.println("Входной поток завершён до окончания игры.");
                break;
            }

            String input = scanner.nextLine();
            String trimmedInput = input.trim();

            if (trimmedInput.equalsIgnoreCase("стоп")) {
                System.out.println("Вы вышли из игры досрочно.");
                log.println("Игрок досрочно завершил игру командой 'стоп'.");
                break;
            }

            if (trimmedInput.isEmpty()) {
                if (remainingHints <= 0) {
                    System.out.println("Подсказки закончились. Введите слово из 5 букв (или 'стоп' для выхода).");
                    continue;
                }
                remainingHints--;
                String suggestion = game.getHintWord();
                System.out.println("Подсказка компьютера: " + suggestion + " (осталось подсказок: " + remainingHints + ")");
                continue;
            }

            try {
                String hint = game.makeGuess(input);
                System.out.println("> " + hint);
                if (game.isWon()) {
                    System.out.println("Поздравляем! Вы угадали слово!");
                }
            } catch (WordNotFoundInDictionaryException e) {
                log.println("Игровое предупреждение (слово не в словаре): " + e.getMessage());
                System.out.println("Такого слова нет в словаре. Попробуйте другое слово (ход не засчитан).");
            } catch (InvalidWordFormatException e) {
                log.println("Игровое предупреждение (некорректный ввод): " + e.getMessage());
                System.out.println("Некорректный ввод: " + e.getMessage() + " (ход не засчитан).");
            } catch (GameRuleException e) {
                log.println("Игровое предупреждение: " + e.getMessage());
                System.out.println("Ошибка правила игры: " + e.getMessage() + " (ход не засчитан).");
            } catch (RuntimeException e) {
                log.println("Критическая ошибка в игровом цикле: " + e.getMessage());
                e.printStackTrace(log);
                log.flush();
                throw e;
            }
        }

        System.out.println("\nИгра окончена.");
        System.out.println("Загаданное слово: " + game.getAnswer());
        if (!game.isWon()) {
            System.out.println("Попытки закончились. В следующий раз обязательно получится!");
        }
    }
}
