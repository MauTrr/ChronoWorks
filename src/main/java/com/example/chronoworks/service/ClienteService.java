package com.example.chronoworks.service;

import com.example.chronoworks.dto.ClienteForm;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ClienteService {

    private final String PYTHON_URL = "https://chronoworks-pyhon.onrender.com/api/clientes/add";

    public void guardarCliente(ClienteForm cliente) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.postForObject(PYTHON_URL, cliente, String.class);
    }
}

