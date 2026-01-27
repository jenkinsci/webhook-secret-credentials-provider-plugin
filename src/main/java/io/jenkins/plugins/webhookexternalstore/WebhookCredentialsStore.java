package io.jenkins.plugins.webhookexternalstore;

import com.cloudbees.plugins.credentials.Credentials;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsStore;
import com.cloudbees.plugins.credentials.CredentialsStoreAction;
import com.cloudbees.plugins.credentials.domains.Domain;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.ModelObject;
import hudson.security.Permission;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.export.ExportedBean;
import org.springframework.security.core.Authentication;

/**
 * Webhook credentials store
 */
public class WebhookCredentialsStore extends CredentialsStore {

    /**
     * The provider.
     */
    private final WebhookCredentialsProvider provider;

    /**
     * The store action.
     */
    private final WebhookCredentialsStoreAction action = new WebhookCredentialsStoreAction(this);

    private final ModelObject context;

    public WebhookCredentialsStore(@NonNull WebhookCredentialsProvider provider, @NonNull ModelObject context) {
        super(WebhookCredentialsProvider.class);
        this.provider = provider;
        this.context = context;
    }

    @Override
    @NonNull
    public ModelObject getContext() {
        return context;
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
