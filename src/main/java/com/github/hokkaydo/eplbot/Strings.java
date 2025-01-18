package com.github.hokkaydo.eplbot;

import kotlin.Pair;
import kotlin.TuplesKt;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * Utility class for strings management.
 * */
public class Strings {

    private Strings() {}
    private static final Map<String, String> STRINGS_MAP = new HashMap<>();

    private static final String STRING_NOT_FOUND = "Translation error. Please report this on [GitHub](https://github.com/Hokkaydo/EPLBot/issues)";

    /**
     * Load strings from locales. Allows i18n.
     * */
    public static void load() {
        try {
            String jarPath = Strings.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();

            try(JarFile jarFile = new JarFile(jarPath)) {
                Enumeration<JarEntry> entries = jarFile.entries();
                List<String> paths = new ArrayList<>();
                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    if(entry.getName().startsWith("locales/") && entry.getName().endsWith(".json")) {
                        String filename = entry.getName().split("/")[1];
                        loadLang(filename, Strings.class.getClassLoader().getResourceAsStream(entry.getName()));
                        paths.add(filename);
                    }
                }
                String languagesStr = String.join(", ", paths);
                Main.LOGGER.info("Loaded {} strings from {} files ({})", STRINGS_MAP.size(), paths.size(), languagesStr);

            }

        } catch (IOException | URISyntaxException | JSONException e) {
            Main.LOGGER.error("Error while loading strings", e);
        }
    }

    /**
     * Load strings from a given file using DFS
     * @param filename the name of the file containing the strings
     * @param stream the {@link InputStream} of the file
     * */
    private static void loadLang(String filename, InputStream stream) {
        JSONObject object = new JSONObject(new JSONTokener(stream));
        String lang = filename.split("\\.")[0];
        Main.LOGGER.info("Loading strings for lang : {}", lang);
        JSONArray names = object.names();

        Queue<Pair<String, JSONObject>> queue = new ArrayDeque<>();
        queue.add(TuplesKt.to(lang, object));

        // parse the first layer of the JSON object, init the DFS
        parseLayer(names, object, lang, queue);

        while (!queue.isEmpty()) {
            Pair<String, JSONObject> pair = queue.poll();
            String base = pair.getFirst();
            JSONObject obj = pair.getSecond();
            names = obj.names();
            parseLayer(names, obj, base, queue);
        }
    }

    /**
     * Code extract for parsing a single layer of a JSON object in the DFS algorithm
     * */
    private static void parseLayer(JSONArray names, JSONObject object, String base, Queue<Pair<String, JSONObject>> queue) {
        for (int i = 0; i < names.length(); i++) {
            String key = names.getString(i);
            if (object.get(key) instanceof JSONObject)
                queue.add(TuplesKt.to(base + "." + key, object.getJSONObject(key)));
            else
                STRINGS_MAP.put(base + "." + key, object.getString(key));
        }
    }

    /**
     * Get a string entry from its key
     * @param key the key of the string
     * @return the string corresponding to the key or {@link #STRING_NOT_FOUND} if the key is not found
     * */
    public static String getString(String key) {
        key = "fr." + key; // Default language
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
