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

        if (!deployTo(project, DeploymentEnvironment.STAGING)) {
            sendEmail("Staging deployment failed");
            return;
        }

        if (!runSmokeTests(project)) {
            return; // O método runSmokeTests já cuida de enviar o e-mail específico de erro
        }

        if (!deployTo(project, DeploymentEnvironment.PRODUCTION)) {
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

        if (project.runTests().equals("success")) {
            log.info("Tests passed");
            return true;
        } else {
            log.error("Tests failed");
            return false;
        }
    }

    private boolean deployTo(Project project, DeploymentEnvironment env) {
        if (project.deploy(env).equals("success")) {
            log.info("Deployment to " + env + " successful");
            return true;
        } else {
            log.error("Deployment to " + env + " failed");
            return false;
        }
    }

    private boolean runSmokeTests(Project project) {
        TestStatus status = project.runSmokeTests();

        if (status == TestStatus.NO_TESTS) {
            log.error("No smoke tests");
            sendEmail("Pipeline failed - no smoke tests");
            return false;
        }

        if (status == TestStatus.FAILING_TESTS) {
            log.error("Smoke tests failed");
            sendEmail("Smoke tests failed");
            return false;
        }

        log.info("Smoke tests passed");
        return true;
    }

    private void sendEmail(String message) {
        if (config.sendEmailSummary()) {
            log.info("Sending email");
            emailer.send(message);
        } else {
            log.info("Email disabled");
        }
    }
}