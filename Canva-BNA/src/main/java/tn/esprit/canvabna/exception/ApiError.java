package tn.esprit.canvabna.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Standardised API error envelope returned by {@link GlobalExceptionHandler}.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {

    private String timestamp;
    private int status;
    private String error;
    private String message;
    private String path;

    /** Only populated for validation errors — maps field names to error messages. */
    private Map<String, String> fieldErrors;
}
