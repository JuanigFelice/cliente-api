package com.banco.cliente_api.exception;

public class ClientesPorProductoNotFoundException extends RuntimeException {
    public ClientesPorProductoNotFoundException(String codigoProducto) {
        super("No se encontraron clientes para el producto bancario: " + codigoProducto);
    }
}
