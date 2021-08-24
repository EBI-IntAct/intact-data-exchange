package uk.ac.ebi.intact.dataexchange.dbimporter.processor;

public class DuplicateEntityException extends RuntimeException {

    public DuplicateEntityException() {
        super();
    }

    public DuplicateEntityException(String message) {
        super(message);
    }

}
