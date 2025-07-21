package cl.duoc.ejemplo.ms.administracion.archivos.service;

import cl.duoc.ejemplo.ms.administracion.archivos.config.RabbitMQConfig;
import cl.duoc.ejemplo.ms.administracion.archivos.dto.FacturaDto;
import cl.duoc.ejemplo.ms.administracion.archivos.model.Factura;
import cl.duoc.ejemplo.ms.administracion.archivos.repository.FacturaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FacturaService {

    private final FacturaRepository facturaRepository;
    private final AwsS3Service awsS3Service;

    private final RabbitTemplate rabbitTemplate;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    public void crearFactura(FacturaDto factura) {
        System.out.println("[Service] Enviando a cola: " + factura);
        rabbitTemplate.convertAndSend(RabbitMQConfig.FACTURA_EXCHANGE, "", factura);
    }

    public Optional<Factura> obtenerFactura(Long id) {
        return facturaRepository.findById(id);
    }

    public List<Factura> obtenerHistorialCliente(String clienteId) {
        return facturaRepository.findByClienteId(clienteId);
    }

    public List<Factura> obtenerTodasLasFacturas() {
        return facturaRepository.findAll();
    }

    public Factura actualizarFactura(Factura factura) {
        return facturaRepository.save(factura);
    }

    public void eliminarFactura(Long id) {
        facturaRepository.deleteById(id);
    }

    public void subirFactura(Long id) throws IOException {
        Factura factura = facturaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Factura no encontrada con ID: " + id));

        String clienteId = factura.getClienteId();
        String fechaFolder = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        String nombreArchivo = factura.getNombreArchivo();
        String rutaRelativa = clienteId + "/" + fechaFolder + "/" + nombreArchivo;

        Path rutaLocal = Path.of("/mnt/efs", rutaRelativa);
        if (!Files.exists(rutaLocal)) {
            throw new IOException("El archivo PDF de la factura no existe en la ruta esperada: " + rutaLocal);
        }

        awsS3Service.uploadFromPath(bucketName, rutaRelativa, rutaLocal);
    }
}
