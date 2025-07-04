package com.banco.cliente_api.exception;

public class ClienteNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/*public ClienteNotFoundException(String dni) {
        super("Cliente con DNI " + dni + " no encontrado.");
    }*/
	
	public ClienteNotFoundException(String dni) {
        super("Cliente no encontrado con DNI: " + dni);
    }

    public ClienteNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}