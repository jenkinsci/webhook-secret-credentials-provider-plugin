package io.jenkins.plugins.webhookexternalstore;

import com.cloudbees.plugins.credentials.Credentials;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsStore;
import com.cloudbees.plugins.credentials.CredentialsStoreAction;
import com.cloudbees.plugins.credentials.common.IdCredentials;
import com.cloudbees.plugins.credentials.domains.Domain;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.XmlFile;
import hudson.model.ModelObject;
import hudson.security.Permission;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.export.ExportedBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;

/**
 * Webhook credentials store
 */
public class WebhookCredentialsStore extends CredentialsStore {

    /**
     * Logger instance for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(WebhookCredentialsStore.class.getName());

    /**
     * The provider.
     */
    private final transient WebhookCredentialsProvider provider;

    /**
     * The store action.
     */
    private final transient WebhookCredentialsStoreAction action = new WebhookCredentialsStoreAction(this);

    /**
     * List of credentials for serialization. This is used to store the credentials in the configuration file and is not used at runtime.
     */
    private List<IdCredentials> credentials = new ArrayList<>();

    /**
     * Constructor.
     * @param provider the provider
     * @param context the context
     */
    public WebhookCredentialsStore(@NonNull WebhookCredentialsProvider provider, @NonNull ModelObject context) {
        super(WebhookCredentialsProvider.class);
        this.provider = provider;
        load();
    }

    @Override
    @NonNull
    public ModelObject getContext() {
        return Jenkins.get();
    }

    @Override
    public boolean hasPermission2(@NonNull Authentication authentication, @NonNull Permission permission) {
        if (!CredentialsProvider.VIEW.equals(permission)) {
            return false;
        }
        return Jenkins.get().getACL().hasPermission2(authentication, permission);
    }

    @Override
    @NonNull
    public List<Credentials> getCredentials(@NonNull Domain domain) {
        if (!Domain.global().equals(domain)) {
            return Collections.emptyList();
        }
        return new ArrayList<>(provider.getAllWebhookCredentials());
    }

    @Override
    public boolean addCredentials(@NonNull Domain domain, @NonNull Credentials credentials) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean updateCredentials(
            @NonNull Domain domain, @NonNull Credentials current, @NonNull Credentials replacement) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeCredentials(@NonNull Domain domain, @NonNull Credentials credentials) {
        throw new UnsupportedOperationException();
    }

    @Override
    @NonNull
    public List<Domain> getDomains() {
        return Collections.singletonList(Domain.global());
    }

    @Override
    @CheckForNull
    public CredentialsStoreAction getStoreAction() {
        return action;
    }

    /**
     * Saves the credentials to the configuration file.
     * @throws IOException if an error occurs while saving the credentials
     */
    @Override
    public synchronized void save() throws IOException {
        this.credentials = new ArrayList<>(provider.getAllWebhookCredentials());
        getConfigFile().write(this);
    }

    /**
     * Loads the credentials from the configuration file.
     */
    protected synchronized void load() {
        XmlFile xml = getConfigFile();
        if (xml.exists()) {
            try {
                xml.unmarshal(this);
                provider.setCredentials(this.credentials);
            } catch (IOException e) {
                LOG.error("Failed to load credentials from configuration file: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * Gets the configuration file that {@link WebhookCredentialsProvider} uses to store its credentials.
     *
     * @return the configuration file that {@link WebhookCredentialsProvider} uses to store its credentials.
     */
    public static XmlFile getConfigFile() {
        return new XmlFile(Jenkins.XSTREAM2, new File(Jenkins.get().getRootDir(), "webhook-credentials.xml"));
    }

    /**
     * Expose the store.
     */
    @ExportedBean
    public static class WebhookCredentialsStoreAction extends CredentialsStoreAction {

        /**
         * The store.
         */
        private final WebhookCredentialsStore store;

        /**
         * Constructor.
         * @param store the store
         */
        private WebhookCredentialsStoreAction(WebhookCredentialsStore store) {
            this.store = store;
        }

        @Override
        @NonNull
        public CredentialsStore getStore() {
            return store;
        }

        @Override
        public String getDisplayName() {
            return "Webhooks";
        }
    }
}
