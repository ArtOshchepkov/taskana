package pro.taskana.doc.api;

import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.restdocs.RestDocsMockMvcConfigurationCustomizer;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import pro.taskana.RestHelper;
import pro.taskana.TaskanaSpringBootTest;
import pro.taskana.doc.api.BaseRestDocumentation.ResultHandlerConfiguration;

/** Base class for Rest Documentation tests. */
@TaskanaSpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Import(ResultHandlerConfiguration.class)
public abstract class BaseRestDocumentation {

  @LocalServerPort protected int port;

  @Autowired protected WebApplicationContext context;

  @Autowired protected MockMvc mockMvc;

  @Autowired protected RestHelper restHelper;

  @TestConfiguration
  static class ResultHandlerConfiguration {

    @Bean
    public RestDocsMockMvcConfigurationCustomizer restDocsMockMvcConfigurationCustomizer() {
      return configurer ->
          configurer
              .operationPreprocessors()
              .withRequestDefaults(prettyPrint())
              .withResponseDefaults(prettyPrint());
    }
  }
}
