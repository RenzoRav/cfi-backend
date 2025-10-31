package com.br.cfi.dtos.imovel;


import java.math.BigDecimal;

import com.br.cfi.dtos.endereco.EnderecoDTO;
import com.br.cfi.entity.types.TipoImovel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImovelDTO {

  private Long id;
  private String titulo;
  private BigDecimal preco;
  private String descricao;
  private EnderecoDTO endereco;
  private TipoImovel tipo;
  private boolean vendido;
  private boolean publicado;
}
