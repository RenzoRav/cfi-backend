package com.br.cfi.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.br.cfi.dtos.imovel.ImovelAtualizarDTO;
import com.br.cfi.dtos.imovel.ImovelDTO;
import com.br.cfi.dtos.imovel.ImovelGravarDTO;
import com.br.cfi.entity.Imovel;
import com.br.cfi.mapper.ImovelMapper;
import com.br.cfi.repository.ImovelRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ImovelService {

  private final ImovelRepository imovelRepository;

  @Transactional
  public ImovelDTO criar(ImovelGravarDTO gravarDTO) {
    Imovel imovel = ImovelMapper.toEntity(gravarDTO);
    imovel = imovelRepository.save(imovel);
    return ImovelMapper.toDTO(imovel);
  }

  @Transactional(readOnly = true)
  public ImovelDTO buscarPorId(Long id) {
    Imovel imovel = imovelRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Imóvel não encontrado"));
    return ImovelMapper.toDTO(imovel);
  }

  @Transactional(readOnly = true)
  public Page<ImovelDTO> listarAdmin(Pageable pageable) {
    return imovelRepository.findAll(pageable).map(ImovelMapper::toDTO);
  }

  @Transactional(readOnly = true)
  public Page<ImovelDTO> listarPublicos(Pageable pageable) {
    return imovelRepository.findByPublicadoTrue(pageable).map(ImovelMapper::toDTO);
  }

  @Transactional
  public ImovelDTO atualizar(Long id, ImovelAtualizarDTO atualizarDTO) {
    Imovel imovel = imovelRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Imóvel não encontrado"));

    ImovelMapper.apply(imovel, atualizarDTO);

    imovel = imovelRepository.save(imovel);
    return ImovelMapper.toDTO(imovel);
  }

  @Transactional
  public void deletarPorId(Long id) {
    if (!imovelRepository.existsById(id)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Imóvel não encontrado");
    }
    imovelRepository.deleteById(id);
  }
}
