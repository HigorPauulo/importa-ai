package br.com.importaai.domain.port.out;

public interface EventPublisher {

    void publicar(String routingKey, Object evento);

}
