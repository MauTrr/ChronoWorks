package com.example.chronoworks.controller;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class ExportController {

    private final String PYTHON_EXPORT_URL = "https://chronoworks-python.onrender.com/api/clientes/export";


    @GetMapping("/clientes/export")
    public ResponseEntity<Resource> exportClientes() {
        RestTemplate restTemplate = new RestTemplate();

        byte[] csvBytes = restTemplate.getForObject(PYTHON_EXPORT_URL, byte[].class);
        ByteArrayResource resource = new ByteArrayResource(csvBytes);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=clientes.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(resource);
    }
}

