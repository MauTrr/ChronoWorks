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

    // Tiempo entre correos para Mailtrap Free (1 email/seg)
    private static final long MAILTRAP_DELAY_MS = 1200; // 1.2 segundos

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
     * Envío masivo con control de rate limit y retry automático
     */
    @Async("mailTaskExecutor")
    public void sendMassiveEmail(List<String> destinatarios, String asunto, String contenidoHtml) {

        if (destinatarios == null || destinatarios.isEmpty()) {
            log.warn("Lista de destinatarios vacía, no se envía nada.");
            return;
        }

        log.info("Iniciando envío masivo a {} destinatarios...", destinatarios.size());

        for (String to : destinatarios) {

            boolean enviado = false;
            int intentos = 0;

            while (!enviado && intentos < 3) {
                try {
                    intentos++;
                    enviarUno(to, asunto, contenidoHtml, true);
                    enviado = true;

                } catch (MailException ex) {

                    // Caso límite Mailtrap: "Too many emails per second"
                    if (ex.getMessage().contains("Too many emails")) {
                        log.warn("Rate limit alcanzado. Reintentando en 1500ms... ({}/3)", intentos);
                        dormir(1500);
                    } else {
                        log.error("Fallo SMTP permanente con {}: {}", to, ex.getMessage());
                        break;
                    }

                } catch (Exception e) {
                    log.error("Error enviando a {}: {}", to, e.getMessage());
                    break;
                }
            }

            // Delay para respetar 1 email por segundo en Mailtrap Free
            dormir(MAILTRAP_DELAY_MS);
        }

        log.info("Envío masivo completado.");
    }

    /**
     * Envío individual con logs y control de errores
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

            helper.setText(contenido, esHtml);

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

    /**
     * Helper para controlar rate limit
     */
    private void dormir(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {}
    }
}
