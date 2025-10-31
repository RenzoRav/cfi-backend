package com.br.cfi.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.br.cfi.dtos.user.UserAtualizarDTO;
import com.br.cfi.dtos.user.UserDTO;
import com.br.cfi.dtos.user.UserGravarDTO;
import com.br.cfi.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/usuarios")
public class UserController {

    @Autowired
    private UserService service;

    @GetMapping
    public ResponseEntity<List<UserDTO>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> buscar(@PathVariable("id") Long id) {
        return ResponseEntity.ok(service.buscar(id));
    }

    @PostMapping
    public ResponseEntity<UserDTO> criar(@Valid @RequestBody UserGravarDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(dto));
    }

    @PutMapping("/{id}/senha")
    public ResponseEntity<UserDTO> atualizarSenha(@PathVariable("id") Long id, @Valid @RequestBody UserAtualizarDTO dto) {
        return ResponseEntity.ok(service.atualizarSenha(id, dto));
    }
}
