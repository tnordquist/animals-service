package edu.cnm.deepdive.animalsservice.controller;

import edu.cnm.deepdive.animalsservice.exception.NotFoundException;
import edu.cnm.deepdive.animalsservice.model.entity.Image;
import edu.cnm.deepdive.animalsservice.service.ImageService;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import org.springframework.hateoas.server.ExposesResourceFor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/images")
@ExposesResourceFor(Image.class)
public class ImageController {

  public static final String UUID_PARAMETER_PATTERN = "/{id:[0-9a-fA-F\\-]{32,36}}";
  public static final String DESCRIPTION_PROPERTY_PATTERN = UUID_PARAMETER_PATTERN + "/description";
  private final ImageService imageService;

  public ImageController(ImageService imageService) {
    this.imageService = imageService;
  }

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Image> post(@RequestParam MultipartFile file) {
    Image image = imageService.save(file);
    return ResponseEntity.created(image.getHref()).body(image);
  }

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, value = UUID_PARAMETER_PATTERN)
  public Image get(@PathVariable UUID id) {
    return imageService.get(id)
        .orElseThrow(NotFoundException::new);
  }

  public void delete(@PathVariable UUID id) {
    imageService.get(id)
        .ifPresent(imageService::delete);
  }

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public Iterable<Image> search(@RequestParam(value = "q", required = false) String fragment) {
    return imageService.search(fragment);
  }

  @GetMapping(value = DESCRIPTION_PROPERTY_PATTERN, produces = {
      MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE})
  public String getDescription(@PathVariable UUID id) {
    return imageService.get(id)
        .map(Image::getDescription)
        .orElseThrow(NotFoundException::new);
  }

  @PutMapping(value = DESCRIPTION_PROPERTY_PATTERN, consumes = {MediaType.APPLICATION_JSON_VALUE,
      MediaType.TEXT_PLAIN_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE,
      MediaType.TEXT_PLAIN_VALUE})
  public String putDescription(@PathVariable UUID id, @RequestBody String description) {
    return imageService.get(id)
        .map((image) -> {
          image.setDescription(description);
          return imageService.save(image).getDescription();
        })
        .orElseThrow(NotFoundException::new);
  }
}
