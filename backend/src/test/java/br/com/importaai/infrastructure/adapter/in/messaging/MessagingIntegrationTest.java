package br.com.importaai.infrastructure.adapter.in.messaging;

import br.com.importaai.infrastructure.adapter.out.persistence.repository.EventoProcessadoJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.RabbitMQContainer;

@SpringBootTest
abstract class MessagingIntegrationTest {

    @SuppressWarnings("resource")
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("importaai")
            .withUsername("importaai")
            .withPassword("importaai123");

    @SuppressWarnings("resource")
    static final RabbitMQContainer RABBIT = new RabbitMQContainer("rabbitmq:3.13-management");

    static {
        MYSQL.start();
        RABBIT.start();
    }

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        // MySQL dinâmico
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);

        // RabbitMQ dinâmico (Testcontainers usa guest/guest por padrão)
        registry.add("spring.rabbitmq.host", RABBIT::getHost);
        registry.add("spring.rabbitmq.port", RABBIT::getAmqpPort);
        registry.add("spring.rabbitmq.username", () -> "guest");
        registry.add("spring.rabbitmq.password", () -> "guest");
    }

    @Autowired protected EventoProcessadoJpaRepository eventoRepo;
    @Autowired protected RabbitTemplate rabbitTemplate;

    @BeforeEach
    void limparTabelaEventoProcessado() {
        eventoRepo.deleteAll();
    }
}
