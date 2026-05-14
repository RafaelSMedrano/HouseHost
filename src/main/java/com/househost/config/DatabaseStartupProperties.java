package com.househost.config;

import java.io.Console;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public final class DatabaseStartupProperties {

    private static final String DEFAULT_MYSQL_URL = "jdbc:mysql://localhost:3306/househost?createDatabaseIfNotExist=true&serverTimezone=UTC";
    private static final String DEFAULT_USERNAME = "root";

    private DatabaseStartupProperties() {
    }

    public static void configure() {
        Map<String, String> dotenv = readDotenv();

        String url = firstConfiguredValue("spring.datasource.url", "HOUSEHOST_DB_URL", dotenv, DEFAULT_MYSQL_URL);
        String username = firstConfiguredValue("spring.datasource.username", "HOUSEHOST_DB_USERNAME", dotenv, DEFAULT_USERNAME);
        String password = firstConfiguredValue("spring.datasource.password", "HOUSEHOST_DB_PASSWORD", dotenv, "");

        if (isMysqlUrl(url) && isPlaceholderPassword(password)) {
            password = askMysqlPassword(username);
        }

        System.setProperty("spring.datasource.url", url);
        System.setProperty("spring.datasource.username", username);
        System.setProperty("spring.datasource.password", password);
    }

    private static String firstConfiguredValue(String systemPropertyName, String environmentName, Map<String, String> dotenv, String defaultValue) {
        String systemValue = System.getProperty(systemPropertyName);
        if (hasText(systemValue)) {
            return systemValue.trim();
        }

        String environmentValue = System.getenv(environmentName);
        if (hasText(environmentValue)) {
            return environmentValue.trim();
        }

        String dotenvValue = dotenv.get(environmentName);
        if (hasText(dotenvValue)) {
            return dotenvValue.trim();
        }

        return defaultValue;
    }

    private static Map<String, String> readDotenv() {
        Path dotenvPath = Path.of(".env");
        if (!Files.isRegularFile(dotenvPath)) {
            return Map.of();
        }

        Map<String, String> values = new HashMap<>();
        try {
            for (String line : Files.readAllLines(dotenvPath)) {
                String trimmedLine = line.trim();
                if (trimmedLine.isEmpty() || trimmedLine.startsWith("#")) {
                    continue;
                }

                int separatorIndex = trimmedLine.indexOf('=');
                if (separatorIndex <= 0) {
                    continue;
                }

                String key = trimmedLine.substring(0, separatorIndex).trim();
                String value = trimmedLine.substring(separatorIndex + 1).trim();
                values.put(key, unquote(value));
            }
        } catch (IOException ignored) {
            return Map.of();
        }

        return values;
    }

    private static String askMysqlPassword(String username) {
        String prompt = "Senha do MySQL para " + username + " (Enter para senha vazia): ";
        Console console = System.console();
        if (console != null) {
            char[] password = console.readPassword(prompt);
            return password == null ? "" : new String(password);
        }

        System.out.print(prompt);
        try {
            return new Scanner(System.in).nextLine();
        } catch (RuntimeException exception) {
            return "";
        }
    }

    private static boolean isMysqlUrl(String url) {
        return url != null && url.startsWith("jdbc:mysql:");
    }

    private static boolean isPlaceholderPassword(String password) {
        return !hasText(password)
                || "coloque_sua_senha_aqui".equals(password)
                || "troque_esta_senha".equals(password);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static String unquote(String value) {
        if (value.length() >= 2) {
            char first = value.charAt(0);
            char last = value.charAt(value.length() - 1);
            if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
                return value.substring(1, value.length() - 1);
            }
        }

        return value;
    }
}
