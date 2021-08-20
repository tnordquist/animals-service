package edu.cnm.deepdive.animalsservice.model.dao;

import edu.cnm.deepdive.animalsservice.model.entity.Image;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository<Image, UUID> {

  Iterable<Image> getAllByOrderByTitleAsc();

}