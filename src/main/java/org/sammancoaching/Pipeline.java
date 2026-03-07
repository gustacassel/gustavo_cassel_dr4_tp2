package org.sammancoaching;

import org.sammancoaching.dependencies.*;

public class Pipeline {
    private final Config config;
    private final Emailer emailer;
    private final Logger log;

    public Pipeline(Config config, Emailer emailer, Logger log) {
        this.config = config;
        this.emailer = emailer;
        this.log = log;
    }

    public void run(Project project) {
        if (!runUnitTests(project)) {
            sendEmail("Unit tests failed");
            return;
        }

        if (!executeDeploy(project, DeploymentEnvironment.STAGING)) {
            sendEmail("Staging deployment failed");
            return;
        }

        if (!runSmokeTests(project)) return;

        if (!executeDeploy(project, DeploymentEnvironment.PRODUCTION)) {
            sendEmail("Production deployment failed");
            return;
        }

        sendEmail("Deployment completed successfully");
    }

    private boolean runUnitTests(Project project) {
        if (!project.hasTests()) {
            log.info("No tests");
            return true;
        }

        if (project.runTestsSuccessful()) {
            log.info("Tests passed");
            return true;
        }

        log.error("Tests failed");
        return false;
    }

    private boolean executeDeploy(Project project, DeploymentEnvironment env) {
        if (project.deploySuccessful(env)) {
            log.info("Deployment to " + env + " successful");
            return true;
        }

        log.error("Deployment to " + env + " failed");
        return false;
    }

    private boolean runSmokeTests(Project project) {
        TestStatus status = project.runSmokeTests();

        if (status == TestStatus.PASSING_TESTS) {
            log.info("Smoke tests passed");
            return true;
        }

        String errorMessage = (status == TestStatus.NO_TESTS)
                ? "Pipeline failed - no smoke tests"
                : "Smoke tests failed";

        log.error(errorMessage);
        sendEmail(errorMessage);
        return false;
    }

    private void sendEmail(String message) {
        if (config.sendEmailSummary()) {
            log.info("Sending email");
            emailer.send(message);
        }
    }
}