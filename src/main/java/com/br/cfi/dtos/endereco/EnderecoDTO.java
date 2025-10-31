package com.br.cfi.dtos.endereco;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnderecoDTO {

  private String logradouro;

  private String numero;

  private String complemento;

  private String bairro;

  private String cidade;

  @NotBlank
  @Size(min = 2, max = 2) 
  private String estado;

  @NotBlank
  private String cep;
}
