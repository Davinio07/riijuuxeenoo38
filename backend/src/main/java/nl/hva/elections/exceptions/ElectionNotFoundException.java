package nl.hva.elections.exceptions;

public class ElectionNotFoundException extends RuntimeException {

    public ElectionNotFoundException(String message) {
        super(message); // Passes the message to the parent RuntimeException
    }
}
