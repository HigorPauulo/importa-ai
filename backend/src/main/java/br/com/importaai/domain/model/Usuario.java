package br.com.importaai.domain.model;

import java.time.Instant;
import java.util.Objects;

public class Usuario {
    private final Long id;
    private final String nome;
    private final String email;
    private final String senhaHash;
    private final PerfilUsuario perfil;
    private final boolean ativo;
    private final Instant criadoEm;

    public Usuario(String nome,  String email, String senhaHash, PerfilUsuario perfil, Instant criadoEm) {
        this(null, nome, email, senhaHash, perfil, true, criadoEm);
    }

    public Usuario(Long id, String nome,  String email, String senhaHash, PerfilUsuario perfil, boolean ativo, Instant criadoEm) {
        this.id = id;
        this.nome = validarTextoObrigatorio(nome, "nome");
        this.email = validarTextoObrigatorio(email, "email");
        this.senhaHash = Objects.requireNonNull(senhaHash, "senhaHash não pode ser nulo");
        this.perfil = Objects.requireNonNull(perfil, "perfil não pode ser nulo");
        this.ativo = ativo;
        this.criadoEm = Objects.requireNonNull(criadoEm, "criadoEm não pode ser nulo");
    }

    private static String validarTextoObrigatorio(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(campo + " nao pode ser nulo ou vazio");
        }
        return valor;
    }

    public Usuario comPerfil(PerfilUsuario novoPerfil) {
        return new Usuario(id, nome, email, senhaHash, novoPerfil, ativo, criadoEm);
    }

    public Usuario comAtivo(boolean novoAtivo) {
        return new Usuario(id, nome, email, senhaHash, perfil, novoAtivo, criadoEm);
    }

    public Usuario comNome(String novoNome) {
        return new Usuario(id, novoNome, email, senhaHash, perfil, ativo, criadoEm);
    }

    public Usuario comEmail(String novoEmail) {
        return new Usuario(id, nome, novoEmail, senhaHash, perfil, ativo, criadoEm);
    }

    public Usuario comSenhaHash(String novoSenhaHash) {
        return new Usuario(id, nome, email, novoSenhaHash, perfil, ativo, criadoEm);
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public PerfilUsuario getPerfil() {
        return perfil;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }
}
