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
public class ImovelGravarDTO {
    
    private String titulo;
    private BigDecimal preco;
    private EnderecoDTO endereco;
    private TipoImovel tipo;
    private String descricao;
    private Boolean publicado;
}
