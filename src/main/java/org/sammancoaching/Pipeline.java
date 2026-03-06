package org.sammancoaching;

import org.sammancoaching.dependencies.Config;
import org.sammancoaching.dependencies.Emailer;
import org.sammancoaching.dependencies.Logger;
import org.sammancoaching.dependencies.Project;

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
            sendEmail("Tests failed");
            return;
        }

        if (!deployProduction(project)) {
            sendEmail("Deployment failed");
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

    private boolean deployProduction(Project project) {
        if (project.deploy().equals("success")) {
            log.info("Deployment successful");
            return true;
        } else {
            log.error("Deployment failed");
            return false;
        }
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