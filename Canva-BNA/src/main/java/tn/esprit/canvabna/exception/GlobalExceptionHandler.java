package tn.esprit.canvabna.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler — maps domain and security exceptions to standardised JSON responses.
 *
 * <p>All responses follow the structure:
 * <pre>
 * {
 *   "timestamp": "...",
 *   "status": 401,
 *   "error": "Unauthorized",
 *   "message": "...",
 *   "path": "/api/auth/login"
 * }
 * </pre>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ---------------------------------------------------------------- Auth errors

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCredentials(
            final BadCredentialsException ex,
            final HttpServletRequest request) {
        log.warn("Bad credentials attempt on path: {}", request.getRequestURI());
        return buildResponse(HttpStatus.UNAUTHORIZED, "Invalid credentials", request);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ApiError> handleUserNotFound(
            final UsernameNotFoundException ex,
            final HttpServletRequest request) {
        log.warn("User not found: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiError> handleDisabled(
            final DisabledException ex,
            final HttpServletRequest request) {
        log.warn("Disabled account access attempt on path: {}", request.getRequestURI());
        return buildResponse(HttpStatus.FORBIDDEN, "Account is disabled", request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(
            final AccessDeniedException ex,
            final HttpServletRequest request) {
        log.warn("Access denied on path: {}", request.getRequestURI());
        return buildResponse(HttpStatus.FORBIDDEN, "Access denied: insufficient permissions", request);
    }

    // ---------------------------------------------------------------- JWT errors

    @ExceptionHandler(io.jsonwebtoken.ExpiredJwtException.class)
    public ResponseEntity<ApiError> handleExpiredJwt(
            final io.jsonwebtoken.ExpiredJwtException ex,
            final HttpServletRequest request) {
        log.warn("Expired JWT on path: {}", request.getRequestURI());
        return buildResponse(HttpStatus.UNAUTHORIZED, "JWT token has expired", request);
    }

    @ExceptionHandler({
            io.jsonwebtoken.MalformedJwtException.class,
            io.jsonwebtoken.security.SecurityException.class,
            io.jsonwebtoken.UnsupportedJwtException.class
    })
    public ResponseEntity<ApiError> handleInvalidJwt(
            final RuntimeException ex,
            final HttpServletRequest request) {
        log.warn("Invalid JWT on path {}: {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(HttpStatus.UNAUTHORIZED, "Invalid JWT token", request);
    }

    // ---------------------------------------------------------------- Validation errors

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(
            final MethodArgumentNotValidException ex,
            final HttpServletRequest request) {
        final Map<String, String> fieldErrors = new HashMap<>();
        for (final FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        log.debug("Validation failed: {}", fieldErrors);

        final ApiError error = ApiError.builder()
                .timestamp(Instant.now().toString())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Failed")
                .message("Request validation failed")
                .path(request.getRequestURI())
                .fieldErrors(fieldErrors)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // ---------------------------------------------------------------- Custom domain errors

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleResourceNotFound(
            final ResourceNotFoundException ex,
            final HttpServletRequest request) {
        log.warn("Resource not found on path {}: {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(
            final IllegalArgumentException ex,
            final HttpServletRequest request) {
        log.warn("Illegal argument on path {}: {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    // ---------------------------------------------------------------- Data integrity errors

    /**
     * Handles duplicate composite key violations (e.g. trying to add a compte that already exists).
     * Returns HTTP 409 Conflict with a descriptive message instead of a generic 500.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrity(
            final DataIntegrityViolationException ex,
            final HttpServletRequest request) {
        log.warn("Data integrity violation on path {}: {}", request.getRequestURI(), ex.getMostSpecificCause().getMessage());
        return buildResponse(HttpStatus.CONFLICT,
                "Cet enregistrement existe déjà ou viole une contrainte d'intégrité de la base de données.",
                request);
    }

    // ---------------------------------------------------------------- Catch-all

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneral(
            final Exception ex,
            final HttpServletRequest request) {
        log.error("Unhandled exception on path {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", request);
    }

    // ---------------------------------------------------------------- Builder

    private ResponseEntity<ApiError> buildResponse(
            final HttpStatus status,
            final String message,
            final HttpServletRequest request) {
        final ApiError error = ApiError.builder()
                .timestamp(Instant.now().toString())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(status).body(error);
    }
}
