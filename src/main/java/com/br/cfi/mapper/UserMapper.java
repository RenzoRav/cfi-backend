package com.br.cfi.mapper;

import com.br.cfi.dtos.user.UserAtualizarDTO;
import com.br.cfi.dtos.user.UserDTO;
import com.br.cfi.dtos.user.UserGravarDTO;
import com.br.cfi.entity.User;

public final class UserMapper {

    private UserMapper() {}

    public static UserDTO toDTO(User entity) {
        if (entity == null) return null;

        UserDTO dto = new UserDTO();
        dto.setId(entity.getId());
        dto.setUsername(entity.getUsername());
        dto.setPassword(entity.getPassword());
        dto.setRole(entity.getRole());

        return dto;
    }

    public static User fromEntity(UserGravarDTO dto) {
        if (dto == null) return null;

        User user = new User();
        user.setUsername(dto.getUsername() == null ? null : dto.getUsername().trim());
        user.setPassword(dto.getPassword());
        user.setRole(dto.getRole());

        return user;
    }

    public static void apply(User entity, UserAtualizarDTO dto) {
        if (entity == null || dto == null) return;
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            entity.setPassword(dto.getPassword());
        }
    }
}
