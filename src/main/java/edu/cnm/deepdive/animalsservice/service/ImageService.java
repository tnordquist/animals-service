package edu.cnm.deepdive.animalsservice.service;

import edu.cnm.deepdive.animalsservice.model.dao.ImageRepository;
import edu.cnm.deepdive.animalsservice.model.entity.Image;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.Random;
import java.util.TimeZone;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImageService {

  private final ImageRepository imageRepository;
  private final ApplicationHome applicationHome;
  private final Random rng;

  @Value("${upload.use-application-home}")
  private boolean useApplicationHome;
  @Value("${upload.path}")
  private String path;
  @Value("${upload.timestamp.format}")
  private String timestampFormat;
  @Value("${upload.timestamp.time-zone}")
  private String timeZone;
  @Value("${upload.filename.unknown}")
  private String unknownFilename;
  @Value("${upload.filename.format}")
  private String filenameFormat;
  @Value("${upload.filename.randomizer-limit}")
  private int randomizerLimit;

  private Path uploadDirectory;
  private DateFormat formatter;

  public ImageService(
      ImageRepository imageRepository,
      ApplicationHome applicationHome, Random rng) {
    this.imageRepository = imageRepository;
    this.applicationHome = applicationHome;
    this.rng = rng;
  }

  @PostConstruct
  private void initUploads() {
    if (useApplicationHome) {
      uploadDirectory = applicationHome.getDir().toPath().resolve(path);
    } else {
      uploadDirectory = Path.of(path);
    }
    //noinspection ResultOfMethodCallIgnored
    uploadDirectory.toFile().mkdirs();
    formatter = new SimpleDateFormat(timestampFormat);
    formatter.setTimeZone(TimeZone.getTimeZone(timeZone));
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

  public Image save(MultipartFile file) {
    try {
      String originalFileName = file.getOriginalFilename();
      if (originalFileName == null) {
        originalFileName = unknownFilename;
      }
      String newFileName = String.format(filenameFormat, formatter.format(new Date()),
          rng.nextInt(randomizerLimit), getExtension(originalFileName));
      Files.copy(file.getInputStream(), uploadDirectory.resolve(newFileName));
      Image image = new Image();
      image.setName(new File(originalFileName).getName());
      image.setPath(newFileName);
      image.setContentType(file.getContentType());
      return imageRepository.save(image);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @NonNull
  private String getExtension(@NonNull String filename) {
    int position;
    return ((position = filename.lastIndexOf('.')) >= 0) ? filename.substring(position + 1) : "";
  }

  /**
   * Selects and returns all images
   *
   * @return images
   */
  public Iterable<Image> list() {
    return imageRepository.getAllByOrderByTitleAsc();
  }

  public Iterable<Image> search(String fragment) {
    Iterable<Image> images;
    if (fragment != null) {
      images = imageRepository.findAllByNameContainsOrderByNameAsc(fragment)
          .and(imageRepository.findAllByDescriptionContainsOrderByNameAsc(fragment))
          .stream()
          .distinct()
          .collect(Collectors.toList());
    } else {
      images = imageRepository.getAllByOrderByTitleAsc();
    }
    return images;
  }

}

