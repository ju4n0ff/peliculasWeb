package com.peliculas.backend.service;

import com.peliculas.backend.model.Resena;
import com.peliculas.backend.model.ResenaLike;
import com.peliculas.backend.model.Usuario;
import com.peliculas.backend.repository.CalificacionRepository;
import com.peliculas.backend.repository.FavoritoRepository;
import com.peliculas.backend.repository.ResenaLikeRepository;
import com.peliculas.backend.repository.ResenaComentarioRepository;
import com.peliculas.backend.repository.ResenaRepository;
import com.peliculas.backend.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final ResenaLikeRepository resenaLikeRepository;
    private final ResenaRepository resenaRepository;
    private final FavoritoRepository favoritoRepository;
    private final CalificacionRepository calificacionRepository;
    private final ResenaComentarioRepository resenaComentarioRepository;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            ResenaLikeRepository resenaLikeRepository,
            ResenaRepository resenaRepository,
            FavoritoRepository favoritoRepository,
            CalificacionRepository calificacionRepository,
            ResenaComentarioRepository resenaComentarioRepository) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.resenaLikeRepository = resenaLikeRepository;
        this.resenaRepository = resenaRepository;
        this.favoritoRepository = favoritoRepository;
        this.calificacionRepository = calificacionRepository;
        this.resenaComentarioRepository = resenaComentarioRepository;
    }

    public Usuario guardar(Usuario usuario) {
        usuario.setContrasena(passwordEncoder.encode(usuario.getContrasena()));
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario guardarOActualizar(Usuario usuario) {
        if (usuario.getUserId() != null) {
            Usuario existente = usuarioRepository.findByUserId(usuario.getUserId()).orElse(null);
            if (existente != null) {
                existente.setNombres(usuario.getNombres());
                existente.setApellidos(usuario.getApellidos());
                existente.setNombreUsuario(usuario.getNombreUsuario());
                existente.setEmail(usuario.getEmail());
                existente.setCelular(usuario.getCelular());
                existente.setRol(usuario.getRol());
                existente.setContrasena(passwordEncoder.encode(usuario.getContrasena()));
                return usuarioRepository.save(existente);
            }
        }
        return guardar(usuario);
    }

    public List<Usuario> listar() {
        return usuarioRepository.findAll();
    }

    public Optional<Usuario> buscarPorId(UUID userId) {
        return usuarioRepository.findByUserId(userId);
    }

    @Transactional
    public void eliminar(UUID userId) {
        Usuario usuario = usuarioRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<ResenaLike> votosDelUsuario = resenaLikeRepository.findAllByUserIdWithResena(userId);
        for (ResenaLike voto : votosDelUsuario) {
            Resena resena = voto.getResena();
            if (resena.getUserId().equals(userId)) {
                continue;
            }

            if ("LIKE".equals(voto.getTipoVoto())) {
                resena.setLikes(Math.max(0, resena.getLikes() - 1));
            } else if ("DISLIKE".equals(voto.getTipoVoto())) {
                resena.setDislikes(Math.max(0, resena.getDislikes() - 1));
            }
        }

        resenaLikeRepository.deleteAll(votosDelUsuario);
        resenaLikeRepository.deleteByResenaUserId(userId);
        resenaComentarioRepository.deleteByResenaUserId(userId);
        resenaRepository.deleteByUserId(userId);
        favoritoRepository.deleteByUserId(userId);
        calificacionRepository.deleteByUserId(userId);
        usuarioRepository.delete(usuario);
    }

    public Usuario actualizar(UUID userId, Usuario usuario) {
        Usuario existente = usuarioRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!existente.getEmail().equals(usuario.getEmail()) &&
            usuarioRepository.findByEmail(usuario.getEmail()).isPresent()) {
            throw new RuntimeException("El email ya está en uso");
        }

        if (!existente.getNombreUsuario().equals(usuario.getNombreUsuario()) &&
            usuarioRepository.findByNombreUsuario(usuario.getNombreUsuario()).isPresent()) {
            throw new RuntimeException("El username ya existe");
        }

        existente.setNombres(usuario.getNombres());
        existente.setApellidos(usuario.getApellidos());
        existente.setNombreUsuario(usuario.getNombreUsuario());
        existente.setEmail(usuario.getEmail());
        existente.setCelular(usuario.getCelular());
        existente.setRol(usuario.getRol());

        return usuarioRepository.save(existente);
    }

    public Optional<Usuario> buscarPorUsername(String nombreUsuario) {
        return usuarioRepository.findByNombreUsuario(nombreUsuario);
    }

    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    public Usuario login(String email, String contrasena) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!passwordEncoder.matches(contrasena, usuario.getContrasena())) {
           throw new RuntimeException("Contraseña incorrecta");
        }

        usuario.setUltimoAcceso(java.time.LocalDateTime.now());

        return usuarioRepository.save(usuario);
    }
}
