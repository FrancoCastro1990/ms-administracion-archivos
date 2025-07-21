package cl.duoc.ejemplo.ms.administracion.archivos.consumer;

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
    private final RabbitTemplate rabbitTemplate;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @RabbitListener(queues = "facturaQueue")
    public void consumeFactura(Factura factura) {
        try {
            String clienteId = factura.getClienteId();
            String fechaFolder = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
            String nombreArchivo = "factura-" + clienteId + "-" + System.currentTimeMillis() + ".pdf";
            String rutaRelativa = clienteId + "/" + fechaFolder + "/" + nombreArchivo;

            Path rutaLocal = Path.of("/mnt/efs", rutaRelativa);
            Files.createDirectories(rutaLocal.getParent());

            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(rutaLocal.toFile()));
            document.open();
            document.add(new Paragraph("Factura ID: " + factura.getId()));
            document.add(new Paragraph("Cliente ID: " + factura.getClienteId()));
            document.add(new Paragraph("Monto: " + factura.getMonto()));
            document.add(new Paragraph("Fecha emisión: " + factura.getFechaEmision()));
            document.close();

            factura.setNombreArchivo(nombreArchivo);
            facturaRepository.save(factura);
        } catch (Exception e) {
            rabbitTemplate.convertAndSend("facturaDLQ", factura);
        }
    }
}
