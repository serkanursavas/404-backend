package com.squad.squad.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AuditTableInitializer implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger log = LoggerFactory.getLogger(AuditTableInitializer.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        List<String> logTables = jdbcTemplate.queryForList(
                "SELECT table_name FROM information_schema.tables " +
                "WHERE table_schema='public' AND table_name LIKE '%_log_table'",
                String.class);

        for (String table : logTables) {
            boolean hasColumn = !jdbcTemplate.queryForList(
                    "SELECT column_name FROM information_schema.columns " +
                    "WHERE table_name=? AND column_name='action_type'",
                    String.class, table).isEmpty();

            if (!hasColumn) {
                jdbcTemplate.execute(
                        "ALTER TABLE " + table + " ADD COLUMN action_type TEXT " +
                        "GENERATED ALWAYS AS " +
                        "(CASE revtype WHEN 0 THEN 'I' WHEN 1 THEN 'U' WHEN 2 THEN 'D' END) STORED");
                log.info("Added action_type column to {}", table);
            }
        }
    }
}
