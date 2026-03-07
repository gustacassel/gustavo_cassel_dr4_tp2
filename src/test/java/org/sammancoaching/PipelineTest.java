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
        when(config.sendEmailSummary()).thenReturn(true);
    }

    @Test
    void projectWithFailingUnitTests_doesNotDeploy() {
        Project project = Project.builder()
                .setTestStatus(TestStatus.FAILING_TESTS)
                .build();

        pipeline.run(project);

        verify(logger).error("Tests failed");
        verify(emailer).send("Unit tests failed");
    }

    @Test
    void projectWithPassingUnitTests_butNoSmokeTests_failsPipeline() {
        Project project = Project.builder()
                .setTestStatus(TestStatus.PASSING_TESTS)
                .setDeploysSuccessfullyToStaging(true)
                .setSmokeTestStatus(TestStatus.NO_TESTS)
                .build();

        pipeline.run(project);

        verify(emailer).send("Pipeline failed - no smoke tests");
        verify(logger, never()).info("Deployment to PRODUCTION successful");
    }

    @Test
    void projectWithPassingSmokeTests_deploysToProduction() {
        Project project = Project.builder()
                .setTestStatus(TestStatus.PASSING_TESTS)
                .setDeploysSuccessfullyToStaging(true)
                .setSmokeTestStatus(TestStatus.PASSING_TESTS)
                .setDeploysSuccessfully(true)
                .build();

        pipeline.run(project);

        verify(logger).info("Deployment to STAGING successful");
        verify(logger).info("Smoke tests passed");
        verify(logger).info("Deployment to PRODUCTION successful");
        verify(emailer).send("Deployment completed successfully");
    }
}