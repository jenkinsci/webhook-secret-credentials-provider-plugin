package io.jenkins.plugins.webhookexternalstore;

import com.cloudbees.plugins.credentials.Credentials;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsStore;
import com.cloudbees.plugins.credentials.common.IdCredentials;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import hudson.Extension;
import hudson.model.ItemGroup;
import hudson.model.ModelObject;
import hudson.security.ACL;
import io.jenkins.plugins.webhookexternalstore.converters.WebhookToCredentialConverter;
import io.jenkins.plugins.webhookexternalstore.exceptions.CredentialsConvertionException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import jenkins.model.Jenkins;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;

/**
 * CredentialsProvider that provides credentials received via webhooks.
 * <p>
 * This provider manages credentials that are created/updated through webhook calls
 * and makes them available throughout Jenkins.
 */
@Extension
public class WebhookCredentialsProvider extends CredentialsProvider {

    /**
     * Logger instance for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(WebhookCredentialsProvider.class.getName());

    /**
     * Memory store for webhook credentials, keyed by credential ID.
     */
    private final ConcurrentHashMap<String, IdCredentials> credentials = new ConcurrentHashMap<>();

    /**
     * Cache of CredentialsStore instances per ModelObject context.
     */
    private final Map<ModelObject, WebhookCredentialsStore> lazyStoreCache = new HashMap<>();

    @Override
    public CredentialsStore getStore(ModelObject object) {
        if (object == null) {
            object = Jenkins.get();
        }
        return lazyStoreCache.computeIfAbsent(object, this::createStore);
    }

    @Override
    public @NonNull <C extends Credentials> List<C> getCredentialsInItemGroup(
            @NonNull Class<C> type,
            @Nullable ItemGroup itemGroup,
            @Nullable Authentication authentication,
            @NonNull List<DomainRequirement> domainRequirements) {
        ArrayList<C> list = new ArrayList<>();
        if (ACL.SYSTEM2.equals(authentication)) {
            for (IdCredentials cred : credentials.values()) {
                if (type.isInstance(cred)) {
                    list.add(type.cast(cred));
                }
            }
        }
        return list;
    }

    private WebhookCredentialsStore createStore(ModelObject object) {
        return new WebhookCredentialsStore(this, object);
    }

    /**
     * Add or update a credential from a webhook payload.
     *
     * @param payload the webhook payload
     * @throws CredentialsConvertionException if the payload cannot be converted to a credential
     */
    public void addOrUpdateCredential(WebhookPayload payload) throws CredentialsConvertionException {
        LOG.debug("Processing webhook payload for credential ID: {}", payload.getId());
        IdCredentials credential = WebhookToCredentialConverter.convertFromPayload(payload);
        String credentialId = payload.getId();
        credentials.put(credentialId, credential);
    }

    /**
     * Get all webhook credentials.
     *
     * @return a list of all webhook sourced credentials
     */
    public List<IdCredentials> getAllWebhookCredentials() {
        return new ArrayList<>(credentials.values());
    }

    /**
     * Get the singleton instance of this provider.
     *
     * @return the webhook credentials provider instance
     */
    public static WebhookCredentialsProvider getInstance() {
        return Jenkins.get().getExtensionList(CredentialsProvider.class).stream()
                .filter(WebhookCredentialsProvider.class::isInstance)
                .map(WebhookCredentialsProvider.class::cast)
                .findFirst()
                .orElseThrow();
    }

    @Override
    public String getIconClassName() {
        return "symbol-webhook plugin-webhook-secret-credentials-provider";
    }
}
