package com.br.cfi.dtos.imagem;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImagemAssetDTO {
  private Long id;                 

  @NotBlank
  private String url;               

  private String contentType;        

  private Long bytes;

  private Integer largura;           
  private Integer altura;           

  private boolean capa;              
  private Integer ordem;             
}
