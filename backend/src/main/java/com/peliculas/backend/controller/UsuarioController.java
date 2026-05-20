package com.peliculas.backend.controller;

import com.peliculas.backend.service.JwtService;
import com.peliculas.backend.dto.LoginRequest;
import com.peliculas.backend.dto.LoginResponse;
import com.peliculas.backend.model.Usuario;
import com.peliculas.backend.service.UsuarioService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.key}")
    private String supabaseKey;

    public UsuarioController(UsuarioService usuarioService, JwtService jwtService, ObjectMapper objectMapper) {
        this.usuarioService = usuarioService;
        this.jwtService = jwtService;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/registro")
    public ResponseEntity<?> registrar(@RequestBody Usuario usuario) {
        try {
            validarSupabaseConfigurado();

            if (usuarioService.buscarPorEmail(usuario.getEmail()).isPresent()) {
                return ResponseEntity.badRequest().body("El email ya esta en uso");
            }

            if (usuarioService.buscarPorUsername(usuario.getNombreUsuario()).isPresent()) {
                return ResponseEntity.badRequest().body("El nombre de usuario ya existe");
            }

            RestTemplate restTemplate = new RestTemplate();

            String url = supabaseAuthUrl("/auth/v1/signup");

            Map<String, String> requestBody = Map.of(
                    "email", usuario.getEmail(),
                    "password", usuario.getContrasena()
            );

            HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, crearSupabaseHeaders());
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            if (response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.CREATED) {
                usuario.setUserId(extraerUserIdSupabase(response.getBody()));
                usuario.setRol("USER");
                usuarioService.guardar(usuario);
                return ResponseEntity.ok("Usuario registrado. Revisa tu correo electronico para verificar la cuenta antes de iniciar sesion.");
            } else {
                return ResponseEntity.badRequest().body("Error al registrar en Supabase");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Usuario usuario) {
        if (usuario.getUserId() == null) {
            return ResponseEntity.badRequest().body("userId es obligatorio");
        }
        return ResponseEntity.ok(usuarioService.guardar(usuario));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            autenticarEnSupabase(request);

            Usuario usuario = usuarioService.login(request.getEmail(), request.getContrasena());
            String token = jwtService.generarToken(usuario.getEmail(), usuario.getRol());
            return ResponseEntity.ok(new LoginResponse(token, usuario.getUserId(), usuario.getNombreUsuario(), usuario.getRol()));
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(estadoErrorLoginSupabase(e)).body(mensajeErrorLoginSupabase(e));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    @GetMapping
    public List<Usuario> listar() {
        return usuarioService.listar();
    }

    @GetMapping("/{userId}")
    public Optional<Usuario> obtener(@PathVariable UUID userId) {
        return usuarioService.buscarPorId(userId);
    }

    @PutMapping("/{userId}")
    public Usuario actualizar(@PathVariable UUID userId, @RequestBody Usuario usuario) {
        return usuarioService.actualizar(userId, usuario);
    }

    @DeleteMapping("/{userId}")
    public void eliminar(@PathVariable UUID userId) {
        usuarioService.eliminar(userId);
    }

    private UUID extraerUserIdSupabase(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        String id = leerTexto(root, "id");

        if (id == null) {
            id = leerTexto(root.path("user"), "id");
        }

        if (id == null) {
            id = leerTexto(root.path("data").path("user"), "id");
        }

        if (id == null) {
            throw new IllegalStateException("Supabase no devolvio el user id");
        }

        return UUID.fromString(id);
    }

    private void autenticarEnSupabase(LoginRequest request) {
        validarSupabaseConfigurado();

        RestTemplate restTemplate = new RestTemplate();
        String url = supabaseAuthUrl("/auth/v1/token?grant_type=password");

        Map<String, String> requestBody = Map.of(
                "email", request.getEmail(),
                "password", request.getContrasena()
        );

        restTemplate.postForEntity(url, new HttpEntity<>(requestBody, crearSupabaseHeaders()), String.class);
    }

    private void validarSupabaseConfigurado() {
        if (supabaseUrl == null || supabaseUrl.isBlank() || supabaseKey == null || supabaseKey.isBlank()) {
            throw new IllegalStateException("Supabase Auth no esta configurado");
        }
    }

    private String supabaseAuthUrl(String path) {
        return supabaseUrl.replaceAll("/+$", "") + path;
    }

    private HttpHeaders crearSupabaseHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("apikey", supabaseKey);
        headers.setBearerAuth(supabaseKey);
        return headers;
    }

    private HttpStatus estadoErrorLoginSupabase(HttpClientErrorException e) {
        String response = e.getResponseBodyAsString().toLowerCase();

        if (response.contains("email_not_confirmed") || response.contains("email not confirmed")) {
            return HttpStatus.FORBIDDEN;
        }

        return HttpStatus.UNAUTHORIZED;
    }

    private String mensajeErrorLoginSupabase(HttpClientErrorException e) {
        String response = e.getResponseBodyAsString().toLowerCase();

        if (response.contains("email_not_confirmed") || response.contains("email not confirmed")) {
            return "Debes verificar tu correo antes de iniciar sesion";
        }

        return "Email o contrasena incorrectos";
    }

    @PostMapping("/reenviar-verificacion")
    public ResponseEntity<?> reenviarVerificacion(@RequestBody Map<String, String> request) {
        try {
            validarSupabaseConfigurado();

            String email = request.get("email");
            if (email == null || email.isBlank()) {
                return ResponseEntity.badRequest().body("El email es requerido");
            }

            RestTemplate restTemplate = new RestTemplate();
            String url = supabaseAuthUrl("/auth/v1/resend");

            Map<String, String> body = Map.of(
                "email", email,
                "type", "signup"
            );

            HttpEntity<Map<String, String>> httpRequest = new HttpEntity<>(body, crearSupabaseHeaders());
            ResponseEntity<String> response = restTemplate.postForEntity(url, httpRequest, String.class);

            if (response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.CREATED) {
                return ResponseEntity.ok("Correo de verificacion reenviado. Revisa tu bandeja de entrada.");
            } else {
                return ResponseEntity.badRequest().body("Error al reenviar el correo de verificacion");
            }
        } catch (HttpClientErrorException e) {
            String errorResponse = e.getResponseBodyAsString().toLowerCase();
            if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS || errorResponse.contains("rate_limit")) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("rate_limit:25");
            }
            if (errorResponse.contains("email_not_confirmed")) {
                return ResponseEntity.badRequest().body("El usuario no esta registrado o ya esta verificado");
            }
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    private String leerTexto(JsonNode node, String fieldName) {
        JsonNode value = node.path(fieldName);
        if (value.isMissingNode() || value.isNull() || value.asText().isBlank()) {
            return null;
        }
        return value.asText();
    }
}
