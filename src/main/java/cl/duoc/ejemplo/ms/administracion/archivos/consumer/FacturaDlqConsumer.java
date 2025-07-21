package cl.duoc.ejemplo.ms.administracion.archivos.consumer;

import cl.duoc.ejemplo.ms.administracion.archivos.dto.FacturaDto;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;

@Component
public class FacturaDlqConsumer {
    private static final Logger log = LoggerFactory.getLogger(FacturaDlqConsumer.class);

    @RabbitListener(queues = "dlx-queue")
    public void consumeDLQ(FacturaDto factura,
                           @Headers Map<String, Object> headers) {
        log.error("[DLQ] Mensaje fallido recibido: {}", factura);

        // Extraer info de x-death para saber por qué falló y cuántas veces
        if (headers.containsKey("x-death")) {
            List<Map<String, Object>> xDeath = (List<Map<String, Object>>) headers.get("x-death");
            Map<String, Object> deathInfo = xDeath.get(0);

            log.error("[DLQ] Info x-death: reason={}, count={}, queue={}",
                    deathInfo.get("reason"),
                    deathInfo.get("count"),
                    deathInfo.get("queue"));
        } else {
            log.error("[DLQ] No se encontró información x-death en el mensaje");
        }
    }
}
