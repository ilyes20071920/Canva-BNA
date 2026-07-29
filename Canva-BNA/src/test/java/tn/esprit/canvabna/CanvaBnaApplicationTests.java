package tn.esprit.canvabna;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@AutoConfigureMockMvc
class CanvaBnaApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @Test
    void dropAllTables() {
        System.out.println("--- DROPPING INCOMPATIBLE OLD SCHEMAS ---");
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0;");
        jdbcTemplate.execute("DROP TABLE IF EXISTS mandataires;");
        jdbcTemplate.execute("DROP TABLE IF EXISTS actionnaires;");
        jdbcTemplate.execute("DROP TABLE IF EXISTS comptes;");
        jdbcTemplate.execute("DROP TABLE IF EXISTS clients;");
        jdbcTemplate.execute("DROP TABLE IF EXISTS structures;");
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1;");
        System.out.println("--- TABLES DROPPED SUCCESSFULLY ---");
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_ADMIN"})
    void testDiagnosticComptes() throws Exception {
        System.out.println("--- RUNNING DIAGNOSTIC FOR POST /api/comptes ---");
        String payload = "{\"codeGuichet\":\"123\",\"codeProduit\":\"IM02\",\"numCompte\":\"192019\",\"agence\":\"146\",\"clientId\":1}";
        mockMvc.perform(post("/api/comptes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_ADMIN"})
    void testDiagnosticActionnaires() throws Exception {
        System.out.println("--- RUNNING DIAGNOSTIC FOR POST /api/actionnaires ---");
        String payload = "{\"nom\":\"Test Actionnaire\",\"nombreActions\":10,\"montant\":1000,\"pourcentageActions\":10.0,\"clientId\":1}";
        mockMvc.perform(post("/api/actionnaires")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_ADMIN"})
    void testDiagnosticMandataires() throws Exception {
        System.out.println("--- RUNNING DIAGNOSTIC FOR POST /api/mandataires ---");
        String payload = "{\"numMandat\":\"1520\",\"numDemande\":\"10\",\"typeMandat\":\"General\",\"agence\":\"146\",\"mandant\":\"ilyes\",\"dateCreation\":\"2026-07-14\",\"dateDebut\":\"2026-07-06\",\"dateFin\":\"2026-07-31\",\"compteCodeGuichet\":\"123\",\"compteCodeProduit\":\"IM02\",\"compteNumCompte\":\"192019\"}";
        mockMvc.perform(post("/api/mandataires")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andDo(print());
    }
}
