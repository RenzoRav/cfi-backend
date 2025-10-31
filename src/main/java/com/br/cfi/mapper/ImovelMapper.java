package com.br.cfi.mapper;

import com.br.cfi.dtos.endereco.EnderecoDTO;
import com.br.cfi.dtos.imovel.ImovelAtualizarDTO;
import com.br.cfi.dtos.imovel.ImovelDTO;
import com.br.cfi.dtos.imovel.ImovelGravarDTO;
import com.br.cfi.entity.Endereco;
import com.br.cfi.entity.Imovel;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ImovelMapper {

  public Imovel toEntity(ImovelGravarDTO d) {
    return Imovel.builder()
        .titulo(d.getTitulo())
        .descricao(d.getDescricao())
        .preco(d.getPreco())
        .endereco(toEndereco(d.getEndereco()))
        .tipo(d.getTipo())
        .publicado(Boolean.TRUE.equals(d.getPublicado()))
        .vendido(false)
        .build();
  }

  public void apply(Imovel e, ImovelAtualizarDTO d) {
    if (d.getTitulo() != null) e.setTitulo(d.getTitulo());
    if (d.getDescricao() != null) e.setDescricao(d.getDescricao());
    if (d.getPreco() != null) e.setPreco(d.getPreco());
    if (d.getEndereco() != null) e.setEndereco(toEndereco(d.getEndereco()));
    if (d.getTipo() != null) e.setTipo(d.getTipo());
    if (d.getVendido() != null) e.setVendido(d.getVendido());
    if (d.getPublicado() != null) e.setPublicado(d.getPublicado());
  }

  public ImovelDTO toDTO(Imovel e) {

    return ImovelDTO.builder()
        .id(e.getId())
        .titulo(e.getTitulo())
        .descricao(e.getDescricao())
        .preco(e.getPreco())
        .endereco(toEnderecoDTO(e.getEndereco()))
        .tipo(e.getTipo())
        .vendido(e.isVendido())
        .publicado(e.isPublicado())
        .build();
  }

  public Endereco toEndereco(EnderecoDTO d) {
    if (d == null) return null;
    return Endereco.builder()
        .logradouro(d.getLogradouro())
        .numero(d.getNumero())
        .complemento(d.getComplemento())
        .bairro(d.getBairro())
        .cidade(d.getCidade())
        .estado(d.getEstado())
        .cep(d.getCep())
        .build();
  }

  public EnderecoDTO toEnderecoDTO(Endereco e) {
    if (e == null) return null;
    return EnderecoDTO.builder()
        .logradouro(e.getLogradouro())
        .numero(e.getNumero())
        .complemento(e.getComplemento())
        .bairro(e.getBairro())
        .cidade(e.getCidade())
        .estado(e.getEstado())
        .cep(e.getCep())
        .build();
  }
}
