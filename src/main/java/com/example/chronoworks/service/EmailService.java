package com.example.chronoworks.service;

import com.sendgrid.*;
import com.example.chronoworks.model.Empleado;
import com.example.chronoworks.repository.EmpleadoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class EmailService {

    private final Logger log = LoggerFactory.getLogger(EmailService.class);
    private final EmpleadoRepository empleadoRepository;

    @Value("${spring.sendgrid.api-key}")
    private String apiKey;

    @Value("${app.mail.from}")
    private String fromEmail;

    private static final long SENDGRID_DELAY = 1200; // evita rate-limit

    public EmailService(EmpleadoRepository empleadoRepository) {
        this.empleadoRepository = empleadoRepository;
    }

    /**
     * Obtener correos por roles
     */
    public List<String> obtenerCorreosPorRoles(List<String> roles) {

        if (roles == null || roles.isEmpty()) return List.of();

        return empleadoRepository.findByNombreRolIn(roles).stream()
                .map(Empleado::getCorreo)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(c -> !c.isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Envío masivo vía SendGrid (asíncrono)
     */
    @Async("mailTaskExecutor")
    public void sendMassiveEmail(List<String> destinatarios, String subject, String htmlContent) {

        log.info("Enviando correos via SendGrid → {} destinatarios", destinatarios.size());

        for (String to : destinatarios) {
            try {
                sendSingle(to, subject, htmlContent);
                Thread.sleep(SENDGRID_DELAY);

            } catch (Exception e) {
                log.error("Error enviando a {}: {}", to, e.getMessage());
            }
        }

        log.info("Envío masivo SendGrid finalizado.");
    }

    /**
     * Enviar un correo individual
     */
    public void sendSingle(String to, String subject, String htmlContent) throws IOException {

        Email from = new Email(fromEmail);
        Email receiver = new Email(to);

        Content content = new Content("text/html", htmlContent);
        Mail mail = new Mail(from, subject, receiver, content);

        SendGrid sg = new SendGrid(apiKey);
        Request request = new Request();

        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());

        Response response = sg.api(request);

        log.info("SendGrid {} → Status {}", to, response.getStatusCode());
    }
}


