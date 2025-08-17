package org.poc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

public class WordProducer implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(WordProducer.class);

    private final List<String> words;
    private int idx = 0;
    private final String docId;
    private final String userId;
    private final PreparedStatement stmt;
    private final long intervalSeconds;

    public WordProducer(String filePath, String docId, String userId, PreparedStatement stmt, long intervalSeconds) throws IOException {
        this.docId = docId;
        this.userId = userId;
        this.stmt = stmt;
        this.intervalSeconds = intervalSeconds;

        String content = Files.readString(Paths.get(filePath));
        this.words = Arrays.stream(content.split("\\s+"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(java.util.stream.Collectors.toList());
        if (words.isEmpty()) {
            throw new IllegalArgumentException("No words found in " + filePath);
        }
        logger.info("Loaded {} words from {}", words.size(), filePath);
    }

    @Override
    public synchronized void run() {
        try {
            String word = words.get(idx);
            long position = idx; // naive position
            // set params: doc_id, user_id, ts, op_type, position, content
            stmt.setString(1, docId);
            stmt.setString(2, userId);
            stmt.setTimestamp(3, Timestamp.from(Instant.now()));
            stmt.setString(4, "insert");
            stmt.setLong(5, position);
            stmt.setString(6, word);
            stmt.executeUpdate();

            logger.info("Inserted word '{}' for {} at pos {}", word, userId, position);

            idx = (idx + 1) % words.size(); // wrap around if end reached
        } catch (SQLException e) {
            logger.error("DB insert failed: {}", e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Producer error: {}", e.getMessage(), e);
        }
    }

}
