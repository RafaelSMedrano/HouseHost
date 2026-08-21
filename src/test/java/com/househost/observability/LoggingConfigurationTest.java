package com.househost.observability;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

class LoggingConfigurationTest {

    @Test
    void rollingConfigurationDefinesRequiredFilesAndLimits() throws Exception {
        String logbackConfiguration;
        try (InputStream inputStream = resource("/logback-spring.xml")) {
            logbackConfiguration = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }

        assertTrue(logbackConfiguration.contains("${LOG_PATH}/househost.log"));
        assertTrue(logbackConfiguration.contains("${LOG_PATH}/househost-error.log"));
        assertTrue(logbackConfiguration.contains("househost-%d{yyyy-MM-dd}.%i.log.gz"));
        assertTrue(logbackConfiguration.contains("househost-error-%d{yyyy-MM-dd}.%i.log.gz"));
        assertTrue(logbackConfiguration.contains("<maxFileSize>${MAX_FILE_SIZE}</maxFileSize>"));
        assertTrue(logbackConfiguration.contains("<maxHistory>${MAX_HISTORY}</maxHistory>"));
        assertTrue(logbackConfiguration.contains("<totalSizeCap>${GENERAL_TOTAL_SIZE_CAP}</totalSizeCap>"));
        assertTrue(logbackConfiguration.contains("<totalSizeCap>${ERROR_TOTAL_SIZE_CAP}</totalSizeCap>"));
        assertTrue(logbackConfiguration.contains("<appender-ref ref=\"CONSOLE\"/>"));
        assertTrue(logbackConfiguration.contains("UserDetailsServiceAutoConfiguration"));

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        Document document = documentBuilderFactory.newDocumentBuilder().parse(resource("/logback-spring.xml"));
        assertEquals("configuration", document.getDocumentElement().getTagName());
    }

    @Test
    void applicationPropertiesExposeDocumentedDefaults() throws Exception {
        Properties properties = new Properties();
        try (InputStream inputStream = resource("/application.properties")) {
            properties.load(inputStream);
        }

        assertEquals("${HOUSEHOST_LOG_PATH:./logs}", properties.getProperty("househost.logging.path"));
        assertEquals("${HOUSEHOST_LOG_MAX_FILE_SIZE:20MB}", properties.getProperty("househost.logging.max-file-size"));
        assertEquals("${HOUSEHOST_LOG_MAX_HISTORY:30}", properties.getProperty("househost.logging.max-history"));
        assertEquals(
                "${HOUSEHOST_LOG_GENERAL_TOTAL_SIZE_CAP:1536MB}",
                properties.getProperty("househost.logging.general-total-size-cap")
        );
        assertEquals(
                "${HOUSEHOST_LOG_ERROR_TOTAL_SIZE_CAP:512MB}",
                properties.getProperty("househost.logging.error-total-size-cap")
        );
    }

    private InputStream resource(String path) {
        InputStream inputStream = LoggingConfigurationTest.class.getResourceAsStream(path);
        assertNotNull(inputStream, "Missing classpath resource " + path);
        return inputStream;
    }
}
