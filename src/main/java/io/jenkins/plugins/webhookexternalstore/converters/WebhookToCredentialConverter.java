package io.jenkins.plugins.webhookexternalstore.converters;

import com.cloudbees.plugins.credentials.common.IdCredentials;
import hudson.ExtensionList;
import hudson.ExtensionPoint;
import io.jenkins.plugins.webhookexternalstore.WebhookPayload;
import io.jenkins.plugins.webhookexternalstore.exceptions.CredentialsConvertionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extension point for converting webhook payloads to credentials.
 * This allows plugins to define their own credential converters for different
 * credential types or to override the default behavior.
 */
public abstract class WebhookToCredentialConverter implements ExtensionPoint {

    /**
     * Logger instance for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(WebhookToCredentialConverter.class);

    /**
     * Determine if this converter can convert the webhook payload to a credential.
     *
     * @param type the credential type from the webhook payload
     * @return true if this converter can handle this type
     */
    public abstract boolean canConvert(String type);

    /**
     * Convert the webhook payload to a credential.
     *
     * @param payload the webhook payload
     * @return the converted credential
     * @throws CredentialsConvertionException if conversion fails
     */
    public abstract IdCredentials convert(WebhookPayload payload) throws CredentialsConvertionException;

    /**
     * Get all registered converters.
     *
     * @return the extension list of all converters
     */
    public static ExtensionList<WebhookToCredentialConverter> all() {
        return ExtensionList.lookup(WebhookToCredentialConverter.class);
    }

    /**
     * Find a converter that can handle the given credential type.
     *
     * @param type the credential type from the webhook payload
     * @return the first converter that can handle this type, or null if none found
     */
    public static WebhookToCredentialConverter lookup(String type) {
        for (WebhookToCredentialConverter converter : all()) {
            try {
                if (converter.canConvert(type)) {
                    LOG.debug(
                            "Found converter {} for credential type {}",
                            converter.getClass().getName(),
                            type);
                    return converter;
                }
            } catch (Exception e) {
                LOG.error(
                        "Error checking converter {} for type {}: {}",
                        converter.getClass().getName(),
                        type,
                        e.getMessage(),
                        e);
            }
        }
        LOG.info("No converter found for credential type {}", type);
        return null;
    }

    /**
     * Convert a webhook payload to a credential using the appropriate converter.
     *
     * @param payload the webhook payload
     * @return the converted credential
     * @throws CredentialsConvertionException if no converter found or conversion fails
     */
    public static IdCredentials convertFromPayload(WebhookPayload payload) throws CredentialsConvertionException {
        WebhookToCredentialConverter converter = lookup(payload.getType());

        if (converter == null) {
            throw new CredentialsConvertionException("No converter found for credential type: " + payload.getType());
        }

        try {
            return converter.convert(payload);
        } catch (CredentialsConvertionException e) {
            throw e;
        } catch (Exception e) {
            throw new CredentialsConvertionException(
                    "Failed to convert credential of type " + payload.getType() + " with converter "
                            + converter.getClass().getSimpleName(),
                    e);
        }
    }
}
