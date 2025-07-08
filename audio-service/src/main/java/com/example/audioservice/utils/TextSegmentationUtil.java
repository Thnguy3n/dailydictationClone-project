package com.example.audioservice.utils;

import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class TextSegmentationUtil {
    private static final Map<String, List<String>> ALTERNATIVE_FORMS_MAP = new HashMap<>();

    static {
        addAlternatives("1st", "1st.", "first", "first.");
        addAlternatives("2nd", "2nd.", "second", "second.");
        addAlternatives("3rd", "3rd.", "third", "third.");
        addAlternatives("4th", "4th.", "fourth", "fourth.");
        addAlternatives("5th", "5th.", "fifth", "fifth.");
        addAlternatives("26th", "26th.", "twenty-sixth", "twenty-sixth.");
        addAlternatives("31st", "31st.", "thirty-first", "thirty-first.");

        addAlternatives("can't", "cannot");
        addAlternatives("won't", "will not");
        addAlternatives("I'm", "I am");
        addAlternatives("you're", "you are");
        addAlternatives("it's", "it is");
        addAlternatives("that's", "that is");

        addAlternatives("o'clock", "o clock");
        addAlternatives("a.m.", "am", "AM");
        addAlternatives("p.m.", "pm", "PM");
    }

    private static void addAlternatives(String... forms) {
        List<String> formList = Arrays.asList(forms);
        for (String form : forms) {
            ALTERNATIVE_FORMS_MAP.put(form.toLowerCase(), formList);
        }
    }

    private static final Pattern WORD_PATTERN = Pattern.compile(
            "\\b\\w+(?:['’]\\w+)*(?:[.!?,:;])?"
    );

    /**
     * Chia câu thành các segments với xử lý tối ưu
     */
    public static List<List<String>> segmentSentence(String fullSentence) {
        if (fullSentence == null || fullSentence.isBlank()) {
            return Collections.emptyList();
        }

        List<List<String>> segments = new ArrayList<>();
        Matcher matcher = WORD_PATTERN.matcher(fullSentence);

        while (matcher.find()) {
            String word = matcher.group();
            if (!word.isBlank()) {
                segments.add(getAlternatives(word));
            }
        }

        return segments;
    }

    /**
     * Lấy các dạng thay thế với cơ chế cache
     */
    private static List<String> getAlternatives(String word) {
        boolean endsWithDot = word.endsWith(".");
        String baseWord = endsWithDot ? word.substring(0, word.length() - 1) : word;
        String lcBaseWord = baseWord.toLowerCase();

        if (ALTERNATIVE_FORMS_MAP.containsKey(word.toLowerCase())) {
            return new ArrayList<>(ALTERNATIVE_FORMS_MAP.get(word.toLowerCase()));
        }

        if (endsWithDot && ALTERNATIVE_FORMS_MAP.containsKey(lcBaseWord)) {
            return new ArrayList<>(ALTERNATIVE_FORMS_MAP.get(lcBaseWord));
        }

        if (isNumber(word) || isOrdinalNumber(word) ||
                (endsWithDot && (isNumber(baseWord) || isOrdinalNumber(baseWord)))) {

            return handleNumbers(endsWithDot ? baseWord : word);
        }

        List<String> alternatives = new ArrayList<>();

        alternatives.add(word);

        String lcWord = word.toLowerCase();
        if (!word.equals(lcWord)) {
            alternatives.add(lcWord);
        }

        String ucWord = word.toUpperCase();
        if (!word.equals(ucWord)) {
            alternatives.add(ucWord);
        }

        if (endsWithDot) {
            alternatives.add(baseWord);

            if (!baseWord.equals(lcBaseWord)) {
                alternatives.add(lcBaseWord);
            }

            String ucBaseWord = baseWord.toUpperCase();
            if (!baseWord.equals(ucBaseWord)) {
                alternatives.add(ucBaseWord);
            }
        }

        return alternatives;
    }

    /**
     * Xử lý các trường hợp đặc biệt với số
     */
    private static List<String> handleNumbers(String number) {
        List<String> results = new ArrayList<>();
        results.add(number);

        // Xử lý số thường
        String wordForm = convertNumberToWord(number);
        if (wordForm != null) {
            results.add(wordForm);
        }

        // Xử lý số thứ tự
        String ordinalForm = convertToOrdinalWord(number);
        if (ordinalForm != null) {
            results.add(ordinalForm);
        }

        return results;
    }

    /**
     * Chuyển số thành chữ (hỗ trợ đến 999)
     */
    private static String convertNumberToWord(String number) {
        try {
            // Loại bỏ hậu tố thứ tự nếu có
            String cleanNumber = number.replaceAll("(?i)(st|nd|rd|th)", "");
            int n = Integer.parseInt(cleanNumber);
            if (n < 0 || n > 999) return null;

            if (n < 20) return BASIC_NUMBERS[n];
            if (n < 100) return convertTens(n);
            return convertHundreds(n);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String convertTens(int n) {
        if (n < 20) return BASIC_NUMBERS[n];
        int tens = n / 10;
        int units = n % 10;
        return TENS[tens] + (units > 0 ? "-" + BASIC_NUMBERS[units] : "");
    }

    private static String convertHundreds(int n) {
        int hundreds = n / 100;
        int remainder = n % 100;
        String result = BASIC_NUMBERS[hundreds] + " hundred";
        if (remainder > 0) result += " and " + convertNumberToWord(String.valueOf(remainder));
        return result;
    }

    /**
     * Chuyển số thứ tự thành chữ
     */
    private static String convertToOrdinalWord(String ordinal) {
        if (!isOrdinalNumber(ordinal)) {
            return null;
        }

        try {
            // Loại bỏ hậu tố và dấu chấm
            String cleanNum = ordinal.replaceAll("(?i)(st|nd|rd|th|\\.)", "");
            int n = Integer.parseInt(cleanNum);

            if (n < 0 || n > 999) return null;

            // Xử lý các số đặc biệt
            if (n == 0) return "zeroth";
            if (n <= 20) return ORDINAL_UP_TO_20[n];

            // Xử lý số tròn chục (20, 30, ...)
            if (n % 10 == 0) {
                int tens = n / 10;
                return TENS_ORDINAL[tens];
            }

            // Xử lý số có 2 chữ số (21-99)
            if (n < 100) {
                int tens = (n / 10) * 10;
                int units = n % 10;
                return TENS[tens / 10] + "-" + ORDINAL_UP_TO_20[units];
            }

            // Xử lý số có 3 chữ số
            int hundreds = n / 100;
            int remainder = n % 100;
            String base = BASIC_NUMBERS[hundreds] + " hundred";
            if (remainder == 0) {
                return base + "th";
            } else {
                return base + " and " + convertToOrdinalWord(String.valueOf(remainder));
            }
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // Dữ liệu hỗ trợ chuyển đổi số
    private static final String[] BASIC_NUMBERS = {
            "zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten",
            "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen", "eighteen", "nineteen"
    };

    private static final String[] TENS = {
            "", "", "twenty", "thirty", "forty", "fifty", "sixty", "seventy", "eighty", "ninety"
    };

    private static final String[] ORDINAL_UP_TO_20 = {
            "zeroth",
            "first", "second", "third", "fourth", "fifth", "sixth", "seventh", "eighth", "ninth", "tenth",
            "eleventh", "twelfth", "thirteenth", "fourteenth", "fifteenth", "sixteenth",
            "seventeenth", "eighteenth", "nineteenth", "twentieth"
    };

    private static final String[] TENS_ORDINAL = {
            "", "", "twentieth", "thirtieth", "fortieth", "fiftieth",
            "sixtieth", "seventieth", "eightieth", "ninetieth"
    };

    /**
     * Kiểm tra chuỗi số
     */
    private static boolean isNumber(String str) {
        return str.matches("\\d+");
    }

    /**
     * Kiểm tra số thứ tự
     */
    private static boolean isOrdinalNumber(String str) {
        return str.matches("(?i)\\d{1,3}(st|nd|rd|th)");
    }

    /**
     * Kiểm tra đáp án với cơ chế băm tối ưu
     */
    public static boolean checkAnswer(List<List<String>> wordSegments, List<String> userAnswers) {
        if (wordSegments.size() != userAnswers.size()) return false;

        for (int i = 0; i < wordSegments.size(); i++) {
            String answer = userAnswers.get(i).trim();
            Set<String> acceptableSet = new HashSet<>(
                    wordSegments.get(i).stream()
                            .map(String::toLowerCase)
                            .toList()
            );

            if (!acceptableSet.contains(answer.toLowerCase())) {
                return false;
            }
        }
        return true;
    }

    public static Map<String, Object> getDetailedResult(List<List<String>> wordSegments, List<String> userAnswers) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> wordResults = new ArrayList<>();

        boolean allCorrect = true;

        for (int i = 0; i < Math.max(wordSegments.size(), userAnswers.size()); i++) {
            Map<String, Object> wordResult = new HashMap<>();
            wordResult.put("index", i);

            if (i < userAnswers.size()) {
                wordResult.put("userAnswer", userAnswers.get(i));
            } else {
                wordResult.put("userAnswer", "");
            }

            if (i < wordSegments.size()) {
                List<String> acceptableAnswers = wordSegments.get(i);
                wordResult.put("acceptableAnswers", acceptableAnswers);

                String userAnswer = i < userAnswers.size() ? userAnswers.get(i).trim() : "";
                boolean isCorrect = acceptableAnswers.stream()
                        .anyMatch(answer -> answer.equalsIgnoreCase(userAnswer));

                wordResult.put("correct", isCorrect);
                if (!isCorrect) {
                    allCorrect = false;
                }
            } else {
                wordResult.put("acceptableAnswers", Arrays.asList());
                wordResult.put("correct", false);
                allCorrect = false;
            }

            wordResults.add(wordResult);
        }

        result.put("allCorrect", allCorrect);
        result.put("wordResults", wordResults);
        result.put("totalWords", wordSegments.size());
        result.put("correctWords", wordResults.stream()
                .mapToInt(wr -> (Boolean) wr.get("correct") ? 1 : 0)
                .sum());

        return result;
    }
}