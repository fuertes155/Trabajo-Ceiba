package com.ceiba.alquiler.exception;

import com.ceiba.alquiler.dto.ErrorResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Esta clase es como mi "red de seguridad".
 * Atrapa cualquier error que ocurra en la aplicación y lo convierte
 * en una respuesta JSON para que el cliente no vea errores del servidor.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Si alguien manda un JSON incompleto o malo, lo atrapo aquí.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationErrors(MethodArgumentNotValidException ex) {
        String mensajes = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        ErrorResponseDTO error = new ErrorResponseDTO(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                mensajes);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Error 409: Para cuando intentan alquilar una bicicleta que no está
     * DISPONIBLE.
     */
    @ExceptionHandler(BicicletaNoDisponibleException.class)
    public ResponseEntity<ErrorResponseDTO> handleBicicletaNoDisponible(BicicletaNoDisponibleException ex) {
        ErrorResponseDTO error = new ErrorResponseDTO(
                HttpStatus.CONFLICT.value(),
                "Conflict",
                ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /**
     * Error 404: Cuando buscan una bicicleta que no existe.
     */
    @ExceptionHandler(BicicletaNoEncontradaException.class)
    public ResponseEntity<ErrorResponseDTO> handleBicicletaNoEncontrada(BicicletaNoEncontradaException ex) {
        ErrorResponseDTO error = new ErrorResponseDTO(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Error 404: Cuando intentan finalizar un alquiler que no existe.
     */
    @ExceptionHandler(AlquilerNoEncontradoException.class)
    public ResponseEntity<ErrorResponseDTO> handleAlquilerNoEncontrado(AlquilerNoEncontradoException ex) {
        ErrorResponseDTO error = new ErrorResponseDTO(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Error 409: Para que no finalicen un alquiler dos veces.
     */
    @ExceptionHandler(AlquilerYaFinalizadoException.class)
    public ResponseEntity<ErrorResponseDTO> handleAlquilerYaFinalizado(AlquilerYaFinalizadoException ex) {
        ErrorResponseDTO error = new ErrorResponseDTO(
                HttpStatus.CONFLICT.value(),
                "Conflict",
                ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /**
     * Maneja excepciones de argumento ilegal (ej: tipo de bicicleta inválido).
     * Retorna 400 Bad Request.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDTO> handleIllegalArgument(IllegalArgumentException ex) {
        ErrorResponseDTO error = new ErrorResponseDTO(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Maneja cualquier excepción no prevista.
     * Retorna 500 Internal Server Error.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGenericException(Exception ex) {
        ErrorResponseDTO error = new ErrorResponseDTO(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "Ha ocurrido un error interno en el servidor. Por favor, intente más tarde.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
