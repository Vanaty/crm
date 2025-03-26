package site.easy.to.build.crm.exception;

import java.util.List;

public class ImportException extends RuntimeException {

    private final List<String> errors;

    public ImportException(String message, List<String> errors) {
        super(message);
        this.errors = errors;
    }

    public List<String> getErrors() {
        return errors;
    }
}