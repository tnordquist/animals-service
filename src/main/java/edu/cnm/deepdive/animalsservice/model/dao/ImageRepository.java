package edu.cnm.deepdive.animalsservice.model.dao;

import edu.cnm.deepdive.animalsservice.model.entity.Image;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.util.Streamable;
import org.springframework.expression.spel.ast.OpAnd;

public interface ImageRepository extends JpaRepository<Image, UUID> {

  Optional<Image> findByExternalKey(UUID key);

  Optional<Image> findFirstById(UUID id);

  Iterable<Image> getAllByOrderByTitleAsc();

  Streamable<Image> findAllByOrderByTitleAsc();

  Streamable<Image> findAllByTitleContainsOrderByTitleAsc(String titleFragment);

  Streamable<Image> findAllByDescriptionContainsOrderByTitleAsc(String descriptionFragment);

}