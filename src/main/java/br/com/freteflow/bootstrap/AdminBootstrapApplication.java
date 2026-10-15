package br.com.freteflow.bootstrap;

import br.com.freteflow.FreteflowApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public class AdminBootstrapApplication {

    public static void main(String[] args) {
        try (ConfigurableApplicationContext context =
                     new SpringApplicationBuilder(FreteflowApplication.class)
                             .web(WebApplicationType.NONE)
                             .profiles("bootstrap")
                             .run(args)) {
            // O contexto é fechado ao terminar o bootstrap.
        }
    }
}
