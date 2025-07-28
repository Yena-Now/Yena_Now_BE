package com.example.yenanow.gallery.repository;

import com.example.yenanow.gallery.entity.Ncut;
import com.example.yenanow.gallery.entity.Visibility;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NcutRepository extends JpaRepository<Ncut, String> {

  Page<Ncut> findByUserUuid(String userUuid, Pageable pageable);

  Page<Ncut> findByUserUuidAndVisibility(String userUuid, Visibility visibility, Pageable pageable);

  Page<Ncut> findByVisibility(Visibility visibility, Pageable pageable);

  Page<Ncut> findByUserUuidInAndVisibilityIn(
      List<String> userUuids,
      List<Visibility> visibilities,
      Pageable pageable
  );

}
