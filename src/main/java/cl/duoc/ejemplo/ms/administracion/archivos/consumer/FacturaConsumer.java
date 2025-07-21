package cl.duoc.ejemplo.ms.administracion.archivos.consumer;

import cl.duoc.ejemplo.ms.administracion.archivos.dto.FacturaDto;
import cl.duoc.ejemplo.ms.administracion.archivos.model.Factura;
import cl.duoc.ejemplo.ms.administracion.archivos.repository.FacturaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

@Component
@RequiredArgsConstructor
public class FacturaConsumer {
    private final FacturaRepository facturaRepository;
    //private final RabbitTemplate rabbitTemplate;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @RabbitListener(queues = "facturaQueue")
    public void consumeFactura(FacturaDto factura) throws Exception {
        System.out.println("[Consumer] Mensaje recibido: " + factura);
        try {
            String clienteId = factura.getClienteId();
            String fechaFolder = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
            String nombreArchivo = "factura-" + clienteId + "-" + System.currentTimeMillis() + ".pdf";
            String rutaRelativa = clienteId + "/" + fechaFolder + "/" + nombreArchivo;

            Path rutaLocal = Path.of("/mnt/efs", rutaRelativa);
            Files.createDirectories(rutaLocal.getParent());


            Factura facturaModelo = new Factura();
            facturaModelo.setClienteId(factura.getClienteId());
            facturaModelo.setFechaEmision(factura.getFechaEmision());
            facturaModelo.setDescripcion(factura.getDescripcion());
            facturaModelo.setMonto(factura.getMonto());
            facturaModelo.setNombreArchivo(nombreArchivo);

            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(rutaLocal.toFile()));
            document.open();
            document.add(new Paragraph("Factura ID: " + facturaModelo.getId()));
            document.add(new Paragraph("Cliente ID: " + facturaModelo.getClienteId()));
            document.add(new Paragraph("Monto: " + facturaModelo.getMonto()));
            document.add(new Paragraph("Fecha emisión: " + facturaModelo.getFechaEmision()));
            document.close();

            facturaRepository.save(facturaModelo);
        } catch (Exception e) {
            System.err.println("[Consumer] Error al procesar la factura: " + e.getMessage());
            e.printStackTrace();
            // Enviar a la cola de errores (DLQ)
            throw e;
            //rabbitTemplate.convertAndSend("facturaDLQ", factura);
        }
    }
}
