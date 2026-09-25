package tn.esprit.canvabna.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DbMigration {

    private final JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void migrate() {
        try {
            // 1. Trouver les clés étrangères sur sollicitations.client_id
            List<String> fkNames = jdbcTemplate.query(
                    "SELECT CONSTRAINT_NAME FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE " +
                            "WHERE TABLE_SCHEMA = 'canva_bna' AND TABLE_NAME = 'sollicitations' " +
                            "AND COLUMN_NAME = 'client_id' AND REFERENCED_TABLE_NAME IS NOT NULL",
                    (rs, rowNum) -> rs.getString("CONSTRAINT_NAME"));

            // Dropper la FK (car MySQL empêche souvent de dropper un index si une FK
            // l'utilise)
            for (String fk : fkNames) {
                log.info("Suppression FK: {}", fk);
                jdbcTemplate.execute("ALTER TABLE sollicitations DROP FOREIGN KEY " + fk);
            }

            // 2. Trouver les index uniques sur client_id (la contrainte)
            List<String> indexNames = jdbcTemplate.query(
                    "SELECT INDEX_NAME FROM INFORMATION_SCHEMA.STATISTICS " +
                            "WHERE TABLE_SCHEMA = 'canva_bna' AND TABLE_NAME = 'sollicitations' " +
                            "AND COLUMN_NAME = 'client_id' AND NON_UNIQUE = 0",
                    (rs, rowNum) -> rs.getString("INDEX_NAME"));

            // Dropper les index uniques (ce qui droppe la contrainte)
            for (String idx : indexNames) {
                if (!idx.equalsIgnoreCase("PRIMARY")) {
                    log.info("Suppression Index Unique: {}", idx);
                    try {
                        jdbcTemplate.execute("ALTER TABLE sollicitations DROP INDEX " + idx);
                    } catch (Exception e) {
                        try {
                            jdbcTemplate.execute("ALTER TABLE sollicitations DROP CONSTRAINT " + idx);
                        } catch (Exception e2) {
                        }
                    }
                }
            }

            // 3. Recréer la Foreign Key (standard, sans unicité) si on en a supprimé au
            // moins une
            if (!fkNames.isEmpty() || !indexNames.isEmpty()) {
                log.info("Recréation de la FK sans contrainte d'unicité.");
                try {
                    jdbcTemplate.execute("ALTER TABLE sollicitations ADD CONSTRAINT fk_sollicitation_client_new " +
                            "FOREIGN KEY (client_id) REFERENCES clients(id)");
                } catch (Exception e) {
                    log.warn("FK existait peut-être déjà ou erreur ignorée : {}", e.getMessage());
                }
            }

        } catch (Exception e) {
            log.warn("DbMigration a échoué silencieusement : {}", e.getMessage());
        }
    }
}
