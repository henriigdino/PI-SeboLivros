package com.sebo.livros.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;

/** Padroniza o corpo de erro de toda a API. */
@RestControllerAdvice
public class RestExceptionHandler {

    public record ApiError(int status, String erro, String mensagem, Map<String, String> campos) { }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> validacao(MethodArgumentNotValidException ex) {
        Map<String, String> campos = new LinkedHashMap<>();
        for (FieldError erro : ex.getBindingResult().getFieldErrors()) {
            campos.putIfAbsent(erro.getField(), erro.getDefaultMessage());
        }
        return ResponseEntity.badRequest().body(new ApiError(
                400, "Validacao falhou", "Verifique os campos informados.", campos));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> regraDeNegocio(ResponseStatusException ex) {
        HttpStatusCode codigo = ex.getStatusCode();
        HttpStatus status = HttpStatus.valueOf(codigo.value());
        String mensagem = ex.getReason() == null ? status.getReasonPhrase() : ex.getReason();
        return ResponseEntity.status(codigo)
                .body(new ApiError(codigo.value(), status.getReasonPhrase(), mensagem, Map.of()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> conflito(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiError(
                409, "Conflict", "Registro ja existente ou vinculado a outro cadastro.", Map.of()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> jsonInvalido(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body(new ApiError(
                400, "Bad Request", "JSON invalido ou campos com formato incorreto.", Map.of()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> parametroInvalido(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.badRequest().body(new ApiError(
                400, "Bad Request", "Parametro invalido: " + ex.getName(), Map.of()));
    }
}
