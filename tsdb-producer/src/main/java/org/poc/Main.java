package org.poc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws SQLException, IOException {

        String host = getEnvOrDefault("DB_HOST", "localhost");
        String port = getEnvOrDefault("DB_PORT", "5432");
        String db = getEnvOrDefault("DB_NAME", "docs_ops");
        String user = getEnvOrDefault("DB_USER", "postgres");
        String pass = getEnvOrDefault("DB_PASS", "password");
        int intervalSeconds = Integer.parseInt(getEnvOrDefault("WORD_INTERVAL_SECONDS", "30"));
        boolean fastMode = Boolean.parseBoolean(getEnvOrDefault("FAST_MODE", "false"));

        if (fastMode) {
            logger.info("FAST_MODE enabled -> using 1s interval for testing");
            intervalSeconds = 1;
        }

        String jdbcUrl = String.format("jdbc:postgresql://%s:%s/%s", host, port, db);
        logger.info("JDBC URL: {}", jdbcUrl);

        Connection conn = DriverManager.getConnection(jdbcUrl, user, pass);
        conn.setAutoCommit(true);

        String insertSql = "INSERT INTO ops (doc_id, user_id, ts, op_type, position, content) VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(insertSql);

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3);
        List<String> files = List.of("data/worker1.txt", "data/worker2.txt", "data/worker3.txt");
        for (int i = 0; i < files.size(); i++) {
            String filePath = files.get(i);
            String userId = "user-" + (i + 1);
            String docId = "doc-1"; // all writing to same doc; change if needed

            WordProducer producer = new WordProducer(filePath, docId, userId, stmt, intervalSeconds);
            // initial delay randomized slightly, so they don't all fire at exact same moment
            long initialDelay = i * 2L;
            scheduler.scheduleAtFixedRate(
                    producer,
                    initialDelay,
                    intervalSeconds,
                    TimeUnit.SECONDS
            );
            logger.info("Scheduled producer for {} (user {}) every {}s", filePath, userId, intervalSeconds);
        }

    }

    private static String getEnvOrDefault(String key, String defaultValue) {
        return Optional.ofNullable(System.getenv(key)).orElse(defaultValue);
    }
}