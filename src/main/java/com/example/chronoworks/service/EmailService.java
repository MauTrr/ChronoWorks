package com.example.chronoworks.service;

import com.example.chronoworks.model.Empleado;
import com.example.chronoworks.repository.EmpleadoRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.eclipse.angus.mail.smtp.SMTPSendFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
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

    @Autowired
    private ApplicationContext context; // ⭐ NECESARIO PARA ACTIVAR @Async

    private final Logger log = LoggerFactory.getLogger(EmailService.class);

    // Mailtrap Free → máximo 1 correo cada ~3 segundos
    private static final long MAILTRAP_DELAY_MS = 3200;

    public EmailService(JavaMailSender mailSender,
                        EmpleadoRepository empleadoRepository,
                        @Value("${app.mail.from:no-reply@mi-dominio.com}") String defaultFrom) {
        this.mailSender = mailSender;
        this.empleadoRepository = empleadoRepository;
        this.defaultFrom = defaultFrom;
    }

    /**
     * Método correcto para iniciar envío masivo desde el controller.
     * Aquí sí se activa @Async.
     */
    public void iniciarEnvio(List<String> destinatarios, String asunto, String html) {
        // ⭐ ACTIVA EL PROXY ASYNC CORRECTAMENTE
        context.getBean(EmailService.class)
                .sendMassiveEmail(destinatarios, asunto, html);
    }

    /**
     * Obtener correos de empleados según roles
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
     * Envío masivo ASÍNCRONO
     */
    @Async("mailTaskExecutor")
    public void sendMassiveEmail(List<String> destinatarios, String asunto, String contenidoHtml) {

        if (destinatarios == null || destinatarios.isEmpty()) {
            log.warn("Lista vacía. No se enviaron correos.");
            return;
        }

        log.info("Iniciando envío masivo. {} destinatarios.", destinatarios.size());

        for (String to : destinatarios) {
            enviarConRateLimit(to, asunto, contenidoHtml);
        }

        log.info("Envío masivo completado.");
    }

    /**
     * Controlador de rate-limit + reintentos
     */
    private void enviarConRateLimit(String to, String asunto, String contenidoHtml) {
        int intentos = 0;

        while (intentos < 3) {
            try {
                enviarUno(to, asunto, contenidoHtml, true);
                dormir(MAILTRAP_DELAY_MS);  // delay obligatorio entre correos
                return;

            } catch (MailException ex) {
                if (esRateLimit(ex)) {
                    intentos++;
                    log.warn("Rate limit alcanzado. Reintentando en 3500ms... ({}/3)", intentos);
                    dormir(3500);
                } else {
                    log.error("Fallo SMTP enviando correo a {}: {}", to, ex.getMessage());
                    return;
                }

            } catch (Exception ex) {
                log.error("Error general enviando correo a {}: {}", to, ex.getMessage());
                return;
            }
        }

        log.error("No se pudo enviar el correo a {} después de 3 intentos por rate limit.", to);
    }

    /**
     * Enviar un correo individual
     */
    public void enviarUno(String to, String asunto, String contenido, boolean esHtml)
            throws MessagingException {

        MimeMessage mime = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(
                mime,
                MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                StandardCharsets.UTF_8.name()
        );

        helper.setTo(to);
        helper.setFrom(defaultFrom);
        helper.setSubject(asunto);

        if (esHtml) {
            helper.setText(contenido, true);
        } else {
            helper.setText(contenido, false);
        }

        mailSender.send(mime);
        log.info("Correo enviado a {}", to);
    }

    /**
     * Detectar error de rate-limit
     */
    private boolean esRateLimit(Exception ex) {
        Throwable causa = ex.getCause();

        return (causa instanceof SMTPSendFailedException failed) &&
                failed.getMessage() != null &&
                failed.getMessage().contains("Too many emails per second");
    }

    private void dormir(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {}
    }
}

