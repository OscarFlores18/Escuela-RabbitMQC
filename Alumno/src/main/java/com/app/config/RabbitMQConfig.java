package com.app.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "alumno.exchange";

    // 1) Alta de alumno
    public static final String Q_ALTA = "alumno.alta.queue";
    public static final String RK_ALTA = "alumno.alta.routingkey";

    // 2) Modificacion de alumno
    public static final String Q_MODIFICACION = "alumno.modificacion.queue";
    public static final String RK_MODIFICACION = "alumno.modificacion.routingkey";

    // 3) Baja de alumno
    public static final String Q_BAJA = "alumno.baja.queue";
    public static final String RK_BAJA = "alumno.baja.routingkey";

    // 4) Auditoria: recibe TODOS los eventos de alumno (patron alumno.#)
    public static final String Q_AUDITORIA = "alumno.auditoria.queue";
    public static final String RK_AUDITORIA = "alumno.#";

    // 5) Estado de curso (lo publica Cursos y lo consume Alumno)
    public static final String Q_CURSO_ESTADO = "curso.estado.queue";
    public static final String RK_CURSO_ESTADO = "curso.estado.routingkey";

    // ---- Colas (duraderas) ----

    @Bean
    public Queue queueAlta() {
        return new Queue(Q_ALTA, true);
    }

    @Bean
    public Queue queueModificacion() {
        return new Queue(Q_MODIFICACION, true);
    }

    @Bean
    public Queue queueBaja() {
        return new Queue(Q_BAJA, true);
    }

    @Bean
    public Queue queueAuditoria() {
        return new Queue(Q_AUDITORIA, true);
    }

    @Bean
    public Queue queueCursoEstado() {
        return new Queue(Q_CURSO_ESTADO, true);
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE);
    }

    // ---- Bindings ----

    @Bean
    public Binding bindingAlta(Queue queueAlta, TopicExchange exchange) {
        return BindingBuilder.bind(queueAlta).to(exchange).with(RK_ALTA);
    }

    @Bean
    public Binding bindingModificacion(Queue queueModificacion, TopicExchange exchange) {
        return BindingBuilder.bind(queueModificacion).to(exchange).with(RK_MODIFICACION);
    }

    @Bean
    public Binding bindingBaja(Queue queueBaja, TopicExchange exchange) {
        return BindingBuilder.bind(queueBaja).to(exchange).with(RK_BAJA);
    }

    @Bean
    public Binding bindingAuditoria(Queue queueAuditoria, TopicExchange exchange) {
        return BindingBuilder.bind(queueAuditoria).to(exchange).with(RK_AUDITORIA);
    }

    @Bean
    public Binding bindingCursoEstado(Queue queueCursoEstado, TopicExchange exchange) {
        return BindingBuilder.bind(queueCursoEstado).to(exchange).with(RK_CURSO_ESTADO);
    }

    // Convierte automáticamente los objetos Java a JSON al enviarlos/recibirlos
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

}
