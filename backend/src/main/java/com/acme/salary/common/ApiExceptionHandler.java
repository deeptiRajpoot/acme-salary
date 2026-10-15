package com.acme.salary.common;

import com.acme.salary.auth.BadCredentialsException;
import com.acme.salary.auth.ForbiddenException;
import jakarta.validation.ConstraintViolationException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/** Maps every failure to RFC 7807 problem+json; validation problems add an `errors` field map. */
@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(NotFoundException.class)
    ResponseEntity<Object> notFound(NotFoundException ex, WebRequest req) {
        return handleExceptionInternal(ex, problem(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), null),
                new HttpHeaders(), HttpStatus.NOT_FOUND, req);
    }

    @ExceptionHandler(ValidationException.class)
    ResponseEntity<Object> invalid(ValidationException ex, WebRequest req) {
        return handleExceptionInternal(ex,
                problem(HttpStatus.BAD_REQUEST, "Validation failed", "One or more fields are invalid", ex.getErrors()),
                new HttpHeaders(), HttpStatus.BAD_REQUEST, req);
    }

    @ExceptionHandler(BadCredentialsException.class)
    ResponseEntity<Object> unauthorized(BadCredentialsException ex, WebRequest req) {
        return handleExceptionInternal(ex, problem(HttpStatus.UNAUTHORIZED, "Unauthorized", ex.getMessage(), null),
                new HttpHeaders(), HttpStatus.UNAUTHORIZED, req);
    }

    @ExceptionHandler(ForbiddenException.class)
    ResponseEntity<Object> forbidden(ForbiddenException ex, WebRequest req) {
        return handleExceptionInternal(ex, problem(HttpStatus.FORBIDDEN, "Forbidden", ex.getMessage(), null),
                new HttpHeaders(), HttpStatus.FORBIDDEN, req);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<Object> constraint(ConstraintViolationException ex, WebRequest req) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getConstraintViolations().forEach(v -> {
            String path = v.getPropertyPath().toString();
            errors.put(path.substring(path.lastIndexOf('.') + 1), v.getMessage());
        });
        return handleExceptionInternal(ex,
                problem(HttpStatus.BAD_REQUEST, "Validation failed", "One or more parameters are invalid", errors),
                new HttpHeaders(), HttpStatus.BAD_REQUEST, req);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<Object> unexpected(Exception ex, WebRequest req) {
        log.error("Unhandled error", ex);
        return handleExceptionInternal(ex,
                problem(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "Unexpected error", null),
                new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, req);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest req) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(e -> errors.putIfAbsent(e.getField(), e.getDefaultMessage()));
        ex.getBindingResult().getGlobalErrors().forEach(e -> errors.putIfAbsent(e.getObjectName(), e.getDefaultMessage()));
        return handleExceptionInternal(ex,
                problem(HttpStatus.BAD_REQUEST, "Validation failed", "One or more fields are invalid", errors),
                headers, HttpStatus.BAD_REQUEST, req);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest req) {
        return handleExceptionInternal(ex,
                problem(HttpStatus.BAD_REQUEST, "Malformed request", "Request body is missing or malformed", null),
                headers, HttpStatus.BAD_REQUEST, req);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestPart(MissingServletRequestPartException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest req) {
        return handleExceptionInternal(ex,
                problem(HttpStatus.BAD_REQUEST, "Missing file", "Multipart field \"" + ex.getRequestPartName() + "\" is required",
                        Map.of(ex.getRequestPartName(), "required")),
                headers, HttpStatus.BAD_REQUEST, req);
    }

    /** Keeps framework-generated problems (404 route, 405, type mismatch...) in the same shape. */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
            HttpStatusCode status, WebRequest req) {
        Object payload = body;
        if (payload instanceof ProblemDetail pd && pd.getTitle() == null) {
            pd.setTitle(HttpStatus.valueOf(status.value()).getReasonPhrase());
        }
        return super.handleExceptionInternal(ex, payload, headers, status, req);
    }

    private static ProblemDetail problem(HttpStatus status, String title, String detail, Map<String, String> errors) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setTitle(title);
        if (errors != null) {
            pd.setProperty("errors", errors);
        }
        return pd;
    }
}
