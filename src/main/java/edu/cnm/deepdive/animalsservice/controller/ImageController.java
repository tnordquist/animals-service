/*
 *  Copyright 2021 CNM Ingenuity, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package edu.cnm.deepdive.animalsservice.controller;

import edu.cnm.deepdive.animalsservice.exception.NotFoundException;
import edu.cnm.deepdive.animalsservice.model.entity.Image;
import edu.cnm.deepdive.animalsservice.service.ImageService;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import org.springframework.core.io.Resource;
import org.springframework.hateoas.server.ExposesResourceFor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/images")
@ExposesResourceFor(Image.class)
public class ImageController {

  public static final String ATTACHMENT_DISPOSITION_FORMAT = "attachment; filename=\"%s\"";
  public static final String IMAGE_NOT_FOUND_REASON = "Image not found";
  private static final String UUID_PARAMETER_PATTERN = "/{id:[0-9a-fA-F\\-]{32,36}}";
  private static final String DESCRIPTION_PROPERTY_PATTERN =
      UUID_PARAMETER_PATTERN + "/description";
  private static final String CONTENT_PROPERTY_PATTERN =
      ParameterPatterns.UUID_PATH_PARAMETER_PATTERN + "/content";

  private final ImageService imageService;


  public ImageController(ImageService imageService) {
    this.imageService = imageService;
  }

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Image> post(@RequestParam MultipartFile file) {
    Image image = imageService.save(file);
    return ResponseEntity.created(image.getHref()).body(image);
  }

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, value = ParameterPatterns.UUID_PATH_PARAMETER_PATTERN)
  public Image get(@PathVariable UUID id) {
    return imageService.get(id)
        .orElseThrow(this::imageNotFound);
  }

  @DeleteMapping(value = ParameterPatterns.UUID_PATH_PARAMETER_PATTERN)
  public void delete(@PathVariable UUID id) {
    imageService.get(id)
        .ifPresent(imageService::delete);
  }

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public Iterable<Image> getImages() {
    return imageService.list();
  }

//  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
//  public Iterable<Image> search(@RequestParam(value = "q", required = false) String fragment) {
//    return imageService.search(fragment);
//  }

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
        .orElseThrow(this::imageNotFound);
  }

  @GetMapping(value = CONTENT_PROPERTY_PATTERN)
  public ResponseEntity<Resource> getContent(@PathVariable UUID id) {
    return imageService.get(id)
        .flatMap((image) -> imageService.getContent(image)
            .map((resource) -> ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, dispositionHeader(image.getName()))
                .header(HttpHeaders.CONTENT_TYPE, image.getContentType())
                .body(resource))
        )
        .orElseThrow(this::imageNotFound);
  }

  private ResponseStatusException imageNotFound() {
    return new ResponseStatusException(HttpStatus.NOT_FOUND, IMAGE_NOT_FOUND_REASON);
  }

  private String dispositionHeader(String filename) {
    return String.format(ATTACHMENT_DISPOSITION_FORMAT, filename);
  }
}
