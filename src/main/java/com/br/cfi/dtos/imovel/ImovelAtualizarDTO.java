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
public class ImovelAtualizarDTO {

  private String titulo;
  private String descricao;
  private BigDecimal preco;
  private EnderecoDTO endereco;
  private TipoImovel tipo;
  private Boolean vendido;
  private Boolean publicado;
  private Boolean arquivado;
}
