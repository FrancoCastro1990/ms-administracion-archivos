package cl.duoc.ejemplo.ms.administracion.archivos.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class FacturaDto {

    private String clienteId;
    private LocalDate fechaEmision;
    private String descripcion;
    private BigDecimal monto;
    private String nombreArchivo;

    // Constructor vacío
    public FacturaDto() {}

    // Getters y setters

    public String getClienteId() {
        return clienteId;
    }

    public void setClienteId(String clienteId) {
        this.clienteId = clienteId;
    }

    public LocalDate getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(LocalDate fechaEmision) {
        this.fechaEmision = fechaEmision;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public void setNombreArchivo(String nombreArchivo) {
        this.nombreArchivo = nombreArchivo;
    }
}
