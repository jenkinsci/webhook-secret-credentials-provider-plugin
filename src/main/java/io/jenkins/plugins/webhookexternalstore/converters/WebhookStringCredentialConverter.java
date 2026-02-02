package io.jenkins.plugins.webhookexternalstore.converters;

import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.common.IdCredentials;
import hudson.util.Secret;
import io.jenkins.plugins.webhookexternalstore.WebhookPayload;
import io.jenkins.plugins.webhookexternalstore.exceptions.CredentialsConvertionException;
import java.util.logging.Logger;
import org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl;
import org.jenkinsci.plugins.variant.OptionalExtension;

/**
 * Converter for string/token credentials using "secretText" type and {@link StringCredentialsImpl}.
 */
@OptionalExtension(requirePlugins = {"plain-credentials"})
@SuppressWarnings("unused")
public class WebhookStringCredentialConverter extends WebhookToCredentialConverter {

    /**
     * Logger instance for this class.
     */
    private static final Logger LOGGER = Logger.getLogger(WebhookStringCredentialConverter.class.getName());

    @Override
    public boolean canConvert(String type) {
        return "secretText".equals(type);
    }

    @Override
    public IdCredentials convert(WebhookPayload payload) throws CredentialsConvertionException {
        String id = payload.getId();
        String description = payload.getDescription();

        String secretToken = payload.getSecretValue("token");
        if (secretToken == null) {
            throw new CredentialsConvertionException("Missing required token in secret for secretText credentials");
        }

        return new StringCredentialsImpl(CredentialsScope.GLOBAL, id, description, Secret.fromString(secretToken));
    }
}
