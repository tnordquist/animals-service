package edu.cnm.deepdive.animalsservice.service;

import edu.cnm.deepdive.animalsservice.model.dao.ImageRepository;
import edu.cnm.deepdive.animalsservice.model.entity.Image;
import java.util.Optional;
import java.util.UUID;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Service
public class ImageService {

  private final ImageRepository imageRepository;

  public ImageService(
      ImageRepository imageRepository) {
    this.imageRepository = imageRepository;
  }

  public Image save(@NonNull Image image) {
    return imageRepository.save(image);
  }

  public Iterable<Image> save(@NonNull Iterable<Image> galleries) {
    return imageRepository.saveAll(galleries);
  }

  /**
   * Selects and returns a {@link Image} with the specified {@code id}, as the content of an {@link
   * Optional Optional&lt;Image&gt;}. If no such instance exists, the {@link Optional} is empty.
   *
   * @param id Unique identifier of the {@link Image}.
   * @return {@link Optional Optional&lt;Image&gt;} containing the selected image.
   */
  public Optional<Image> get(@NonNull UUID id) {
    return imageRepository.findById(id);
  }

  /**
   * Selects and returns all images
   * @return images
   */
  public Iterable<Image> list() {
    return imageRepository.getAllByOrderByTitleAsc();
  }

}
