package com.example.chronoworks.service;

import com.example.chronoworks.model.Empleado;
import com.example.chronoworks.repository.EmpleadoRepository;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final EmpleadoRepository empleadoRepository;
    private final String defaultFrom;
    private final Logger log = LoggerFactory.getLogger(EmailService.class);

    public EmailService(JavaMailSender mailSender,
                        EmpleadoRepository empleadoRepository,
                        @Value("${app.mail.from:no-reply@mi-dominio.com}") String defaultFrom) {
        this.mailSender = mailSender;
        this.empleadoRepository = empleadoRepository;
        this.defaultFrom = defaultFrom;
    }

    /**
     * Obtiene lista de correos de empleados según roles
     */
    public List<String> obtenerCorreosPorRoles(List<String> roles) {
        if (roles == null || roles.isEmpty()) {
            return List.of();
        }
        return empleadoRepository.findByNombreRolIn(roles).stream()
                .map(Empleado::getCorreo)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(c -> !c.isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Envío masivo: itera destinatarios y envía individualmente.
     */
    @Async("mailTaskExecutor") // 👈 aquí usamos tu executor específico
    public void sendMassiveEmail(List<String> destinatarios, String asunto, String contenidoHtml) {
        if (destinatarios == null || destinatarios.isEmpty()) {
            log.warn("Lista de destinatarios vacía, no se envía nada.");
            return;
        }
        for (String to : destinatarios) {
            try {
                enviarUno(to, asunto, contenidoHtml, true); // siempre HTML
            } catch (Exception e) {
                log.error("Error enviando email a {}: {}", to, e.getMessage(), e);
            }
        }
    }

    /**
     * Enviar un correo individual
     */
    public void enviarUno(String to, String asunto, String contenido, boolean esHtml) {
        try {
            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    mime,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );

            helper.setTo(to);
            helper.setSubject(asunto);
            helper.setFrom(defaultFrom);

            if (esHtml) {
                helper.setText(contenido, true);
            } else {
                helper.setText(contenido, false);
            }

            mailSender.send(mime);
            log.info("Correo enviado a {}", to);

        } catch (MailException ex) {
            log.error("Fallo SMTP enviando correo a {}: {}", to, ex.getMessage(), ex);
            throw ex;
        } catch (Exception ex) {
            log.error("Error general enviando correo a {}: {}", to, ex.getMessage(), ex);
            throw new RuntimeException("Error enviando correo a " + to, ex);
        }
    }
}
