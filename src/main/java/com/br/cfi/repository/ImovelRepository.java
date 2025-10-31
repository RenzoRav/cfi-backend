package com.br.cfi.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.br.cfi.entity.Imovel;
import com.br.cfi.entity.types.TipoImovel;

public interface ImovelRepository extends JpaRepository<Imovel, Long> {

  Page<Imovel> findByPublicadoTrue(Pageable pageable);

  Page<Imovel> findByPublicadoTrueAndTipo(TipoImovel tipo, Pageable pageable);
}
