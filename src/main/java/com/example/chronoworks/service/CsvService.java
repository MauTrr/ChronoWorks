package com.example.chronoworks.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class CsvService {

    @Autowired
    private WebClient csvWebClient;

    public ResponseEntity<ByteArrayResource> descargarCSV() {

        byte[] contenido = csvWebClient.get()
                .uri("/api/clientes/export") // endpoint FastAPI
                .retrieve()
                .bodyToMono(byte[].class)
                .block();

        ByteArrayResource resource = new ByteArrayResource(contenido);

        return ResponseEntity.ok()
                .contentLength(contenido.length)
                .header("Content-Disposition", "attachment; filename=clientes.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(resource);
    }
}
