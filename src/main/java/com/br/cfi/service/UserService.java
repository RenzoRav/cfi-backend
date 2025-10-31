package com.br.cfi.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.br.cfi.dtos.user.UserAtualizarDTO;
import com.br.cfi.dtos.user.UserDTO;
import com.br.cfi.dtos.user.UserGravarDTO;
import com.br.cfi.entity.User;
import com.br.cfi.mapper.UserMapper;
import com.br.cfi.repository.UserRepository;


@Service
public class UserService {

    @Autowired
    private UserRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<UserDTO> listar() {
        return repository.findAll().stream()
            .map(UserMapper::toDTO)
            .toList();
    }

    public UserDTO buscar(Long id) {
        User user = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado: " + id));
        return UserMapper.toDTO(user);
    }

    public UserDTO criar(UserGravarDTO dto) {
        User entity = UserMapper.fromEntity(dto);
        entity.setPassword(passwordEncoder.encode(entity.getPassword()));

        try {
            return UserMapper.toDTO(repository.save(entity));
        } catch (DataIntegrityViolationException erro) {
            throw new RuntimeException("Usuario já existe", erro);
        }
    }

    public UserDTO atualizarSenha(Long id, UserAtualizarDTO dto) {
        User entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado: " + id));

        if (dto.getPassword() == null || dto.getPassword().isBlank()) {
            throw new RuntimeException("A nova senha não pode estar vazia");
        }

        entity.setPassword(passwordEncoder.encode(dto.getPassword()));
        return UserMapper.toDTO(repository.save(entity));
    }
}
