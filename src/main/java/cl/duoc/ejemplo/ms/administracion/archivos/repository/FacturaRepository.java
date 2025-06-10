package cl.duoc.ejemplo.ms.administracion.archivos.repository;

import cl.duoc.ejemplo.ms.administracion.archivos.model.Factura;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FacturaRepository extends JpaRepository<Factura, Long> {
    List<Factura> findByClienteId(String clienteId);
}
