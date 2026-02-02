package io.jenkins.plugins.webhookexternalstore.converters;

import com.cloudbees.jenkins.plugins.sshcredentials.impl.BasicSSHUserPrivateKey;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.common.IdCredentials;
import io.jenkins.plugins.webhookexternalstore.WebhookPayload;
import io.jenkins.plugins.webhookexternalstore.exceptions.CredentialsConvertionException;
import java.util.logging.Logger;
import org.jenkinsci.plugins.variant.OptionalExtension;

/**
 * Converter for string/token credentials using "secretText" type and {@link BasicSSHUserPrivateKey}.
 */
@OptionalExtension(requirePlugins = {"ssh-credentials"})
@SuppressWarnings("unused")
public class WebhookBasicSSHUserPrivateKeyCredentialConverter extends WebhookToCredentialConverter {

    /**
     * Logger instance for this class.
     */
    private static final Logger LOGGER =
            Logger.getLogger(WebhookBasicSSHUserPrivateKeyCredentialConverter.class.getName());

    @Override
    public boolean canConvert(String type) {
        return "basicSSHUserPrivateKey".equals(type);
    }

    @Override
    public IdCredentials convert(WebhookPayload payload) throws CredentialsConvertionException {
        String id = payload.getId();
        String description = payload.getDescription();

        String username = payload.getSecretValue("username");
        if (username == null) {
            throw new CredentialsConvertionException("Missing required username in secret for secretText credentials");
        }
        String privateKey = payload.getSecretValue("privateKey");
        if (privateKey == null) {
            throw new CredentialsConvertionException(
                    "Missing required privateKey in secret for secretText credentials");
        }
        String passphrase = payload.getSecretValue("passphrase");

        return new BasicSSHUserPrivateKey(
                CredentialsScope.GLOBAL,
                id,
                username,
                new BasicSSHUserPrivateKey.DirectEntryPrivateKeySource(privateKey),
                passphrase,
                description);
    }
}
