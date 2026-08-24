package br.com.freteflow;

import org.junit.jupiter.api.Test;

class SmokeTest extends AbstractIntegrationTest {

    @Test
    void contextLoads() {
        // Se o contexto do Spring sobe sem exceção, e o Flyway aplica as
        // migrations contra o container Postgres sem erro, este teste passa.
    }
}
