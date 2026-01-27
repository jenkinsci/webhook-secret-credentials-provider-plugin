package io.jenkins.plugins.webhookexternalstore.exceptions;

/**
 * Exception thrown when a webhook payload cannot be converted to a credential.
 */
public class CredentialsConvertionException extends Exception {

    /**
     * Constructs a new exception with the specified message.
     * @param message the detail message
     */
    public CredentialsConvertionException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified message and cause.
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public CredentialsConvertionException(String message, Throwable cause) {
        super(message, cause);
    }
}
