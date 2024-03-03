package com.persona.controller;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.persona.modelo.Persona;
import com.persona.repository.PersonaRepository;

@RestController
@CrossOrigin(origins ="http://localhost:4200")
@RequestMapping({"personas"})
public class PersonaController {

    @Autowired
    private PersonaRepository repository;

    // Credenciales de usuario autorizado
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "teknei";

    @GetMapping("listar")
    public ResponseEntity<?> listarPersonas(@RequestHeader("Authorization") String authorizationHeader) {
        if (authorizeUser(authorizationHeader)) {
            List<Persona> personas = (List<Persona>) repository.findAll();
            return ResponseEntity.ok(personas);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No está autenticado para estos procesos");
        }
    }

    @PostMapping
    public ResponseEntity<Object> agregarPersona(@RequestBody Persona persona, @RequestHeader("Authorization") String authorizationHeader) throws JsonProcessingException {
        if (authorizeUser(authorizationHeader)) {
            persona.setEdad(persona.getEdad() + 1);
            Persona personaGuardada = repository.save(persona);
            // Crear un objeto que contenga el mensaje
            Map<String, String> response = new HashMap<>();
            response.put("", "El registro de " + personaGuardada.getNombre() + ", próximamente tendrás " + (personaGuardada.getEdad()) + " años.");
            // Convertir el objeto a JSON
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonResponse = objectMapper.writeValueAsString(response);
            // Devolver la respuesta JSON
            return ResponseEntity.ok(jsonResponse);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No está autenticado para estos procesos");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPersonaById(@PathVariable int id, @RequestHeader("Authorization") String authorizationHeader) {
        if (authorizeUser(authorizationHeader)) {
            Optional<Persona> persona = repository.findById(id);
            return persona.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No está autenticado para estos procesos");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> modificarPersona(@RequestBody Persona persona, @PathVariable int id, @RequestHeader("Authorization") String authorizationHeader) {
        if (authorizeUser(authorizationHeader)) {
            persona.setId(id);
            Persona personaModificada = repository.save(persona);
            return ResponseEntity.ok(personaModificada);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No está autenticado para estos procesos");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> eliminarPersona(@PathVariable int id, @RequestHeader("Authorization") String authorizationHeader) throws JsonProcessingException {
        if (authorizeUser(authorizationHeader)) {
            repository.deleteById(id);
            // Crear un objeto que contenga el mensaje
            Map<String, String> response = new HashMap<>();
            response.put("mensaje", "Persona eliminada correctamente");
            // Convertir el objeto a JSON
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonResponse = objectMapper.writeValueAsString(response);
            // Devolver la respuesta JSON
            return ResponseEntity.ok(jsonResponse);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No está autenticado para estos procesos");
        }
    }

    private boolean authorizeUser(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Basic ")) {
            String base64Credentials = authorizationHeader.substring("Basic ".length());
            String credentials = new String(Base64.getDecoder().decode(base64Credentials));
            String[] parts = credentials.split(":", 2);
            if (parts.length == 2 && parts[0].equals(USERNAME) && parts[1].equals(PASSWORD)) {
                return true;
            }
        }
        return false;
    }
}