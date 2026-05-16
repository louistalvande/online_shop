package com.shop;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Generates {@code openapi.yaml} from the running Spring context without an external server.
 *
 * <p>Run with: {@code ./mvnw -Dtest=OpenApiGeneratorTest test}
 * The generated file is written to {@code backend/openapi.yaml}.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("openapi")
class OpenApiGeneratorTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * Calls the springdoc endpoint and writes the YAML spec to {@code openapi.yaml}.
     *
     * @throws Exception if the request fails or the file cannot be written
     */
    @Test
    void generateOpenApiYaml() throws Exception {
        String spec = mockMvc.perform(get("/api-docs.yaml"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        Path output = Path.of("openapi.yaml");
        Files.writeString(output, spec, StandardCharsets.UTF_8);
        System.out.println("openapi.yaml written to: " + output.toAbsolutePath());
    }
}
