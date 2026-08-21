package com.househost.observability;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.util.LogbackMDCAdapter;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP;
import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy;
import ch.qos.logback.core.util.Duration;
import ch.qos.logback.core.util.FileSize;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LogbackRollingPolicyTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void rotatesAndCompressesWhenControlledSizeBoundaryIsReached() throws Exception {
        LoggerContext loggerContext = new LoggerContext();
        loggerContext.setMDCAdapter(new LogbackMDCAdapter());
        loggerContext.start();
        Path activeLog = temporaryDirectory.resolve("househost.log");
        String archivePattern = temporaryDirectory
                .resolve("archive/househost-%d{yyyy-MM-dd}.%i.log.gz")
                .toString();

        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(loggerContext);
        encoder.setPattern("%msg%n");
        encoder.start();

        RollingFileAppender<ILoggingEvent> rollingFileAppender = new RollingFileAppender<>();
        rollingFileAppender.setContext(loggerContext);
        rollingFileAppender.setFile(activeLog.toString());
        rollingFileAppender.setEncoder(encoder);

        SizeAndTimeBasedRollingPolicy<ILoggingEvent> rollingPolicy = new SizeAndTimeBasedRollingPolicy<>();
        rollingPolicy.setContext(loggerContext);
        rollingPolicy.setParent(rollingFileAppender);
        rollingPolicy.setFileNamePattern(archivePattern);
        rollingPolicy.setMaxFileSize(FileSize.valueOf("1KB"));
        rollingPolicy.setMaxHistory(2);
        rollingPolicy.setTotalSizeCap(FileSize.valueOf("10MB"));
        rollingPolicy.start();
        SizeAndTimeBasedFNATP<ILoggingEvent> triggeringPolicy = (SizeAndTimeBasedFNATP<ILoggingEvent>)
                rollingPolicy.getTimeBasedFileNamingAndTriggeringPolicy();
        triggeringPolicy.stop();
        triggeringPolicy.setCheckIncrement(Duration.buildByMilliseconds(10));
        triggeringPolicy.start();

        rollingFileAppender.setRollingPolicy(rollingPolicy);
        rollingFileAppender.start();
        assertTrue(rollingPolicy.isStarted());
        assertTrue(rollingFileAppender.isStarted());

        String payload = "x".repeat(700);
        for (int index = 0; index < 8; index++) {
            LoggingEvent loggingEvent = new LoggingEvent();
            loggingEvent.setLoggerContext(loggerContext);
            loggingEvent.setLoggerName("controlled-rolling-test");
            loggingEvent.setLevel(Level.INFO);
            loggingEvent.setMessage("event=rolling.verification index=" + index + " payload=" + payload);
            loggingEvent.setThreadName(Thread.currentThread().getName());
            loggingEvent.setTimeStamp(System.currentTimeMillis());
            rollingFileAppender.doAppend(loggingEvent);
            Thread.sleep(120);
        }

        Path archiveDirectory = temporaryDirectory.resolve("archive");
        if (!Files.isDirectory(archiveDirectory)) {
            Thread.sleep(20);
            assertTrue(
                    Files.size(activeLog) > 1_024,
                    () -> loggerContext.getStatusManager().getCopyOfStatusList().toString()
            );
            assertTrue(rollingFileAppender.getTriggeringPolicy().isTriggeringEvent(activeLog.toFile(), null));
            rollingFileAppender.rollover();
        }

        rollingFileAppender.stop();
        rollingPolicy.stop();
        loggerContext.stop();

        assertTrue(Files.exists(activeLog));
        assertTrue(Files.isDirectory(archiveDirectory));
        try (var archivePathStream = Files.list(archiveDirectory)) {
            List<Path> archivePathList = archivePathStream
                    .filter(path -> path.getFileName().toString().endsWith(".log.gz"))
                    .toList();
            assertFalse(archivePathList.isEmpty());
            assertTrue(archivePathList.stream().allMatch(path -> fileHasContent(path)));
        }
    }

    private boolean fileHasContent(Path path) {
        try {
            return Files.size(path) > 0;
        } catch (Exception exception) {
            return false;
        }
    }
}
