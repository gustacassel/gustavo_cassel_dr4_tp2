package org.sammancoaching;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sammancoaching.dependencies.*;

import static org.mockito.Mockito.*;

class PipelineTest {
    private Config config;
    private Emailer emailer;
    private Logger logger;
    private Pipeline pipeline;

    @BeforeEach
    void setUp() {
        config = mock(Config.class);
        emailer = mock(Emailer.class);
        logger = mock(Logger.class);
        pipeline = new Pipeline(config, emailer, logger);

        // Configuramos para sempre enviar email nos testes por padrão
        when(config.sendEmailSummary()).thenReturn(true);
    }

    @Test
    void projectWithNoTests_deploysSuccessfully() {
        Project project = Project.builder()
                .setTestStatus(TestStatus.NO_TESTS)
                .setDeploysSuccessfully(true)
                .build();

        pipeline.run(project);

        verify(logger).info("No tests");
        verify(logger).info("Deployment successful");
        verify(emailer).send("Deployment completed successfully");
    }

    @Test
    void projectWithFailingTests_doesNotDeploy() {
        Project project = Project.builder()
                .setTestStatus(TestStatus.FAILING_TESTS)
                .build();

        pipeline.run(project);

        verify(logger).error("Tests failed");
        verify(emailer).send("Tests failed");
        verify(logger, never()).info("Deployment successful");
    }

    @Test
    void projectWithPassingTests_deployFails() {
        Project project = Project.builder()
                .setTestStatus(TestStatus.PASSING_TESTS)
                .setDeploysSuccessfully(false)
                .build();

        pipeline.run(project);

        verify(logger).info("Tests passed");
        verify(logger).error("Deployment failed");
        verify(emailer).send("Deployment failed");
    }
}