package de.leidenheit.steeldartdetectormvp.detection;

public class LeidenheitException extends RuntimeException {

    public LeidenheitException(final String message, final Throwable cause) {
        super(message);
    }

    public LeidenheitException(final String message) {
        super(message);
    }
}
