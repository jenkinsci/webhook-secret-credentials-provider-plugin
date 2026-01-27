package io.jenkins.plugins.webhookexternalstore;

import hudson.Extension;
import hudson.security.csrf.CrumbExclusion;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Ensure that the REST URL /webhook-credentials/update is excluded from CSRF protection
 * It's using different authentication method (Shared token for webhooks)
 */
@Extension
@SuppressWarnings("unused")
public class WebhookCrumbExclusion extends CrumbExclusion {

    @Override
    public boolean process(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        String path = request.getPathInfo();
        if (path != null && path.startsWith("/%s/update".formatted(WebhookEndpoint.WEBHOOK_PATH))) {
            chain.doFilter(request, response);
            return true;
        }

        return false;
    }
}
