package com.br.cfi.dtos.user;

import com.br.cfi.entity.types.UserRole;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor 
@NoArgsConstructor
@Builder
public class UserGravarDTO {
    private String username;
    private String password;
    private UserRole role;
}
