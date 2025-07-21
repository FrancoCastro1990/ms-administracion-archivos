package cl.duoc.ejemplo.ms.administracion.archivos.controller;

import cl.duoc.ejemplo.ms.administracion.archivos.dto.FacturaDto;
import cl.duoc.ejemplo.ms.administracion.archivos.model.Factura;
import cl.duoc.ejemplo.ms.administracion.archivos.service.FacturaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/facturas")
public class FacturaController {

    private final FacturaService facturaService;

    public FacturaController(FacturaService facturaService) {
        this.facturaService = facturaService;
    }

    @PostMapping
    public ResponseEntity<String> crearFactura(@RequestBody FacturaDto facturaDto) {
        FacturaDto factura = new FacturaDto();
        factura.setClienteId(facturaDto.getClienteId());
        factura.setFechaEmision(facturaDto.getFechaEmision());
        factura.setDescripcion(facturaDto.getDescripcion());
        factura.setMonto(facturaDto.getMonto());
        factura.setNombreArchivo(facturaDto.getNombreArchivo());
        System.out.println("[Controller] Recibido DTO: " + facturaDto);
        facturaService.crearFactura(factura);
        return ResponseEntity.ok("Factura creada y enviada a la cola");
    }

    @GetMapping("/{id}")
    public ResponseEntity<Factura> obtenerFactura(@PathVariable Long id) {
        Optional<Factura> factura = facturaService.obtenerFactura(id);
        return factura.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/historial/{clienteId}")
    public ResponseEntity<List<Factura>> obtenerHistorialPorCliente(@PathVariable String clienteId) {
        return ResponseEntity.ok(facturaService.obtenerHistorialCliente(clienteId));
    }

    @GetMapping
    public ResponseEntity<List<Factura>> obtenerTodasLasFacturas() {
        return ResponseEntity.ok(facturaService.obtenerTodasLasFacturas());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Factura> actualizarFactura(@PathVariable Long id, @RequestBody FacturaDto facturaDto) {
        Optional<Factura> existente = facturaService.obtenerFactura(id);
        if (existente.isEmpty()) return ResponseEntity.notFound().build();

        Factura factura = existente.get();
        factura.setClienteId(facturaDto.getClienteId());
        factura.setFechaEmision(facturaDto.getFechaEmision());
        factura.setDescripcion(facturaDto.getDescripcion());
        factura.setMonto(facturaDto.getMonto());
        factura.setNombreArchivo(facturaDto.getNombreArchivo());

        return ResponseEntity.ok(facturaService.actualizarFactura(factura));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarFactura(@PathVariable Long id) {
        facturaService.eliminarFactura(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/upload")
    public ResponseEntity<String> subirFactura(@PathVariable Long id) throws IOException {
        facturaService.subirFactura(id);
        return ResponseEntity.ok("Archivo subido correctamente a S3");
    }
}
