package com.br.cfi.entity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.br.cfi.entity.types.TipoImovel;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "imovel", schema = "app")
public class Imovel {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String titulo;

  @Column(nullable = false, precision = 14, scale = 2)
  private BigDecimal preco;

  @Embedded
  private Endereco endereco;

  @Column(nullable = false)
  private String descricao;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private TipoImovel tipo;

  @Column(nullable = false) private boolean vendido;
  @Column(nullable = false) private boolean publicado;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private OffsetDateTime criadoEm;

  @UpdateTimestamp
  @Column(nullable = false)
  private OffsetDateTime atualizadoEm;
}
