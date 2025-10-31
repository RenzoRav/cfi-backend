package com.br.cfi.dtos.imagem;

import java.math.BigDecimal;

import com.br.cfi.dtos.endereco.EnderecoDTO;
import com.br.cfi.entity.types.TipoImovel;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImovelGravarDTO {

  private String titulo;

  @DecimalMin(value = "0.0", inclusive = false)
  private BigDecimal preco;

  @NotNull @Valid
  private EnderecoDTO endereco;

  @NotNull
  private TipoImovel tipo;

  private Boolean publicado;
}
