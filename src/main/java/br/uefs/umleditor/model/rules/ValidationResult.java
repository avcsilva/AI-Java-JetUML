package br.uefs.umleditor.model.rules;

/**
 * Outcome of checking whether an edge may be created: valid, rejected
 * silently, or rejected with an error message shown to the user (RF18).
 */
public record ValidationResult(Status status, String message) {

    public enum Status { VALID, SILENT_FAILURE, ERROR }

    public static final ValidationResult VALID = new ValidationResult(Status.VALID, "");
    public static final ValidationResult SILENT_FAILURE = new ValidationResult(Status.SILENT_FAILURE, "");

    public static ValidationResult error(String message) {
        return new ValidationResult(Status.ERROR, message);
    }

    public boolean isValid() {
        return status == Status.VALID;
    }

    public boolean isError() {
        return status == Status.ERROR;
    }
}
