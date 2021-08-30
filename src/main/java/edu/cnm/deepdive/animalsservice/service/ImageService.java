package edu.cnm.deepdive.animalsservice.service;

import edu.cnm.deepdive.animalsservice.configuration.UploadConfiguration;
import edu.cnm.deepdive.animalsservice.configuration.UploadConfiguration.FilenameProperties;
import edu.cnm.deepdive.animalsservice.configuration.UploadConfiguration.TimestampProperties;
import edu.cnm.deepdive.animalsservice.model.dao.ImageRepository;
import edu.cnm.deepdive.animalsservice.model.entity.Image;
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
  private final Random rng;

  private final Path uploadDirectory;
  private final Set<String> contentTypes;
  private final DateFormat formatter;
  private final String unknownFilename;
  private final String filenameFormat;
  private final int randomizerLimit;

  @Autowired
  public ImageService(
      ImageRepository imageRepository, UploadConfiguration uploadConfiguration,
      ApplicationHome applicationHome, Random rng) {
    this.imageRepository = imageRepository;
    this.rng = rng;
    FilenameProperties filenameProperties = uploadConfiguration.getFilename();
    TimestampProperties timestampProperties = filenameProperties.getTimestamp();
    String uploadPath = uploadConfiguration.getPath();
    uploadDirectory = (uploadConfiguration.isApplicationHome())
        ? applicationHome.getDir().toPath().resolve(uploadPath)
        : Path.of(uploadPath);
    contentTypes = new HashSet<>(uploadConfiguration.getContentTypes());
    unknownFilename = filenameProperties.getUnknown();
    filenameFormat = filenameProperties.getFormat();
    randomizerLimit = filenameProperties.getRandomizerLimit();
    formatter = new SimpleDateFormat(timestampProperties.getFormat());
    formatter.setTimeZone(TimeZone.getTimeZone(timestampProperties.getTimeZone()));
  }

  @PostConstruct
  private void initUploads() {
    //noinspection ResultOfMethodCallIgnored
    uploadDirectory.toFile().mkdirs();
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
    if (!contentTypes.contains(file.getContentType())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "Unsupported MIME type in uploaded content.");
    }
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
      //noinspection ConstantConditions
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

  public Optional<Resource> getContent(Image image) {
    try {
      Path file = uploadDirectory.resolve(image.getPath());
      return Optional.of(new UrlResource(file.toUri()));
    } catch (MalformedURLException e) {
      return Optional.empty();
    }
  }
}

