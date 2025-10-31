// src/main/java/com/br/cfi/controller/ImovelController.java
package com.br.cfi.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.br.cfi.dtos.imovel.ImovelAtualizarDTO;
import com.br.cfi.dtos.imovel.ImovelDTO;
import com.br.cfi.dtos.imovel.ImovelGravarDTO;
import com.br.cfi.service.ImovelService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/imoveis")
public class ImovelController {

  private final ImovelService imovelService;

  @PostMapping
  public ResponseEntity<ImovelDTO> criar(@Valid @RequestBody ImovelGravarDTO gravarDTO) {
    ImovelDTO criado = imovelService.criar(gravarDTO);
    return ResponseEntity.status(HttpStatus.CREATED).body(criado);
  }

  @GetMapping("/{id}")
  public ImovelDTO buscarPorId(@PathVariable Long id) {
    return imovelService.buscarPorId(id);
  }

  @GetMapping
  public Page<ImovelDTO> listarAdmin(Pageable pageable) {
    return imovelService.listarAdmin(pageable);
  }

  @PatchMapping("/{id}")
  public ImovelDTO atualizar(
      @PathVariable Long id,
      @Valid @RequestBody ImovelAtualizarDTO atualizarDTO
  ) {
    return imovelService.atualizar(id, atualizarDTO);
  }

  @DeleteMapping("/{id}/hard")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deletarDefinitivo(@PathVariable Long id) {
    imovelService.deletarPorId(id);
  }

  @GetMapping("/publicos")
public Page<ImovelDTO> listarPublicos(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") int size
) {
  return imovelService.listarPublicos(
      org.springframework.data.domain.PageRequest.of(page, size)
  );
}
}
