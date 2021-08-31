package edu.cnm.deepdive.animalsservice.service;

import edu.cnm.deepdive.animalsservice.configuration.UploadConfiguration;
import edu.cnm.deepdive.animalsservice.configuration.UploadConfiguration.FilenameProperties;
import edu.cnm.deepdive.animalsservice.configuration.UploadConfiguration.TimestampProperties;
import edu.cnm.deepdive.animalsservice.model.dao.ImageRepository;
import edu.cnm.deepdive.animalsservice.model.entity.Image;
import edu.cnm.deepdive.animalsservice.service.StorageService.FilenameTranslation;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.util.Streamable;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ImageService {

  private final ImageRepository imageRepository;
  private final StorageService storageService;

  @Autowired
  public ImageService(
      ImageRepository imageRepository, StorageService storageService) {
    this.imageRepository = imageRepository;
    this.storageService = storageService;
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

  public void delete(Image image) {
    imageRepository.delete(image);
  }

  public Image save(@NonNull Image image) {
    return imageRepository.save(image);
  }

  public Image store(MultipartFile file) throws IOException {
    FilenameTranslation translation = storageService.store(file);
    Image image = new Image();
    image.setName(translation.getOriginalFilename());
    image.setPath(translation.getNewFilename());
    image.setContentType(file.getContentType());
    return imageRepository.save(image);
  }

  public Resource retrieve(Image image) throws MalformedURLException {
    return storageService.retrieve(image.getPath());
  }

  /**
   * Selects and returns all images
   *
   * @return images
   */
  public Iterable<Image> list() {
    return imageRepository.getAllByOrderByTitleAsc();
  }

  public Streamable<Image> search(String fragment) {
    Streamable<Image> images;
    if (fragment != null) {
      images = Streamable.of(
          imageRepository
              .findAllByTitleContainsOrderByTitleAsc(fragment)
              .and(imageRepository.findAllByDescriptionContainsOrderByTitleAsc(fragment))
              .toSet()
      );
    } else {
      images = imageRepository.getAllByOrderByTitleAsc();
    }
    return images;
  }

}

