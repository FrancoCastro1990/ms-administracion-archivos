package cl.duoc.ejemplo.ms.administracion.archivos.config;

import java.util.Map;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;



@Configuration

public class RabbitMQConfig {

	@Value("${RABBITMQ_HOST:localhost}")
	private String rabbitHost;

	@Value("${RABBITMQ_PORT:5672}")
	private int rabbitPort;

	@Value("${RABBITMQ_USER:guest}")
	private String rabbitUser;

	@Value("${RABBITMQ_PASS:guest}")
	private String rabbitPass;

	public static final String FACTURA_QUEUE = "facturaQueue";
	public static final String DLX_QUEUE = "dlx-queue";
	public static final String FACTURA_EXCHANGE = "facturaExchange";
	public static final String DLX_EXCHANGE = "dlx-exchange";
	public static final String DLX_ROUTING_KEY = "dlx-routing-key";


	@Bean
	Jackson2JsonMessageConverter messageConverter() {

		return new Jackson2JsonMessageConverter();
	}

	@Bean
	CachingConnectionFactory connectionFactory() {
		CachingConnectionFactory factory = new CachingConnectionFactory();
		factory.setHost(rabbitHost);
		factory.setPort(rabbitPort);
		factory.setUsername(rabbitUser);
		factory.setPassword(rabbitPass);
		return factory;
	}

	@Bean
	public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
		SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
		factory.setConnectionFactory(connectionFactory);
		factory.setDefaultRequeueRejected(false);
		factory.setErrorHandler(error -> {
			System.err.println("Error en el consumidor: " + error.getMessage());
			// Aquí podrías enviar a DLQ o manejar el error de otra forma
		});
		return factory;
	}


	@Bean
	Queue facturaQueue() {
		return new Queue(FACTURA_QUEUE, true, false, false,
				Map.of("x-dead-letter-exchange", DLX_EXCHANGE, "x-dead-letter-routing-key", DLX_ROUTING_KEY));
	}

	@Bean
	Queue dlxQueue() {

		return new Queue(DLX_QUEUE);
	}

	@Bean
	DirectExchange facturaExchange() {
		return new DirectExchange(FACTURA_EXCHANGE);
	}

	@Bean
	DirectExchange dlxExchange() {

		return new DirectExchange(DLX_EXCHANGE);
	}

	@Bean
	Binding binding(Queue facturaQueue, DirectExchange facturaExchange) {
		return BindingBuilder.bind(facturaQueue).to(facturaExchange).with("");
	}

	@Bean
	Binding dlxBinding() {

		return BindingBuilder.bind(dlxQueue()).to(dlxExchange()).with(DLX_ROUTING_KEY);
	}
}
