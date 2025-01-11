package com.github.hokkaydo.eplbot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for strings management.
 * */
public class Strings {

    private Strings() {}
    private static final Map<String, String> STRINGS_MAP = new HashMap<>();

    /**
     * Load strings from strings.json file. Allows i18n.
     * */
    public static void load() throws JSONException {
        InputStream stream = Strings.class.getClassLoader().getResourceAsStream("strings.json");
        assert stream != null;
        JSONObject object = new JSONObject(new JSONTokener(stream));
        if(object.isEmpty()) return;
        JSONArray names = object.names();
        for (int i = 0; i < names.length(); i++) {
            String key = names.getString(i);
            STRINGS_MAP.put(key, object.getString(key));
        }
    }
    private static final String STRING_NOT_FOUND = "Erreur de traduction. Veuillez la signaler à la modération";

    /**
     * Get a string entry from its key
     * @param key the key of the string
     * @return the string corresponding to the key or {@link #STRING_NOT_FOUND} if the key is not found
     * */
    public static String getString(String key) {
        if(!STRINGS_MAP.containsKey(key)) {
            Main.LOGGER.warn("Missing string : {}", key);
            return STRING_NOT_FOUND;
        }
        return STRINGS_MAP.get(key);
    }

    /**
     * Capitalize the first letter of a string
     * @param str the string to capitalize
     * @return the string with the first letter capitalized
     * */
    public static String capsFirstLetter(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /**
     * Rabbit-Karp algorithm for pattern string search in a later given large text
     * @param pattern the pattern to search
     * @return a {@link RabinKarp} object to search pattern in texts
     * */
    public static RabinKarp getSearcher(String pattern) {
        return new RabinKarp(pattern);
    }

    /**
     * Rabbit-Karp algorithm for pattern string search in a later given large text
     * */
    public static class RabinKarp {

        private static final long Q = 31;  // a prime number
        private static final int R = 256;         // alphabet size
        private final long patternHash;
        private final int m;               // pattern length
        private final String pattern;
        private long rm;

        RabinKarp(String pattern) {
            this.m = pattern.length();
            this.rm = 1;
            for (int i = 1; i <= m - 1; i++)
                rm = (R * rm) % Q;             // R^(M-1) % Q
            patternHash = hash(pattern);
            this.pattern = pattern;
        }

        /**
         * Compute {@link String} hash mod Q
         *
         * @param text text to compute hash for
         * @return computed hash
         */
        private long hash(String text) {
            long h = 0;
            for (int i = 0; i < m; i++)
                h = (R * h + text.charAt(i)) % Q;
            return h;
        }

        /**
         * Search for occurrences of a {@link #pattern} in given text
         *
         * @param text text to search pattern's occurrences in
         * @return a {@link List<Integer>} of first index of pattern's occurrences in given text
         */
        public List<Integer> search(String text) {
            if (pattern.equals(text)) return Collections.singletonList(0);
            int n = text.length();
            long textHash = hash(text);
            List<Integer> occurrences = new ArrayList<>();
            for (int i = m; i < n; i++) {
                textHash = (textHash + Q - rm * text.charAt(i - m) % Q) % Q;
                textHash = (textHash * R + text.charAt(i)) % Q;
                if (patternHash == textHash && text.substring(i - m + 1, i + 1).equals(pattern))
                    occurrences.add(i - m + 1);
            }
            return occurrences;
        }

    }

}
