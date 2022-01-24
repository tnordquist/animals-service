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

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.cnm.deepdive.animalsservice.exception.NotFoundException;
import edu.cnm.deepdive.animalsservice.model.entity.Image;
import edu.cnm.deepdive.animalsservice.service.ImageService;
import edu.cnm.deepdive.animalsservice.service.ImageService.ImageNotFoundException;
import edu.cnm.deepdive.animalsservice.view.ImageView;

import java.io.IOException;
import java.util.UUID;
import java.util.function.Function;

import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.hateoas.server.ExposesResourceFor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/images")
@ExposesResourceFor(Image.class)
@Profile("service")
public class ImageController {

    public static final String ATTACHMENT_DISPOSITION_FORMAT = "attachment; filename=\"%s\"";
    public static final String IMAGE_NOT_FOUND_REASON = "Image not found";
    public static final String NOT_RETRIEVED_MESSAGE = "Unable to retrieve previously uploaded file";
    private static final String UUID_PARAMETER_PATTERN = "/{externalKey:[0-9a-fA-F\\-]{32,36}}";
    private static final String DESCRIPTION_PROPERTY_PATTERN =
            UUID_PARAMETER_PATTERN + "/description";
    private static final String CONTENT_PROPERTY_PATTERN =
            ParameterPatterns.UUID_PATH_PARAMETER_PATTERN + "/content";
    private static final String NOT_STORED_MESSAGE = "Unable to store uploaded content";
    private static final String NOT_WHITELISTED_MESSAGE = "Upload MIME type not in whitelist";
    private static final String FILE_STORE_FAILURE_MESSAGE = "File store error";

    private final ImageService imageService;
    private final ObjectMapper mapper;


    public ImageController(ImageService imageService,
                           ObjectMapper mapper) {
        this.imageService = imageService;
        this.mapper = mapper;
    }

    @JsonView(ImageView.Full.class)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Image> post(
            @RequestParam MultipartFile file,
            @RequestParam String title,
            @RequestParam(required = false) String description
    ) {
        try {

            Image image = imageService.store(file, title, description);
            return ResponseEntity.created(image.getHref()).body(image);
        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, NOT_STORED_MESSAGE, e);
        } catch (HttpMediaTypeNotAcceptableException e) {
            throw new ResponseStatusException(
                    HttpStatus.UNSUPPORTED_MEDIA_TYPE, NOT_WHITELISTED_MESSAGE, e);
        }
    }

    @JsonView(ImageView.Full.class)
    @GetMapping(value = UUID_PARAMETER_PATTERN, produces = MediaType.APPLICATION_JSON_VALUE)
    public Image get(@PathVariable UUID externalKey) {
        return imageService
                .get(externalKey)
                .orElseThrow(this::imageNotFound);
    }

    @JsonView(ImageView.Partial.class)
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Iterable<Image> list() {
        return imageService.list();
    }

    @DeleteMapping(value = BaseParameterPatterns.UUID_PATH_PARAMETER_PATTERN)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID externalKey) {
        imageService.get(externalKey)
                .ifPresentOrElse(
                        image -> {
                            try {
                                imageService.delete(image);
                            } catch (IOException e) {
                                throw new ResponseStatusException(
                                        HttpStatus.INTERNAL_SERVER_ERROR, FILE_STORE_FAILURE_MESSAGE, e);
                            }
                        },
                        () -> {
                            throw new ImageNotFoundException();
                        }
                );
    }

    @GetMapping(value = DESCRIPTION_PROPERTY_PATTERN, produces = {
            MediaType.APPLICATION_JSON_VALUE})
    public String getDescription(@PathVariable UUID externalKey) {
        return imageService.get(externalKey)
                .map(new Function<Image, String>() {
                    @Override
                    public String apply(Image image) {
                        return image.getDescription();
                    }
                })
                .orElseThrow(NotFoundException::new);
    }

    @PutMapping(value = DESCRIPTION_PROPERTY_PATTERN, consumes = {
            MediaType.APPLICATION_JSON_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public String putDescription(@PathVariable UUID externalKey, @RequestBody String description)
            throws JsonProcessingException {
        String fixedDescription = mapper.readValue(description, String.class);
        return imageService.updateDescription(externalKey, fixedDescription)
                .map((newDescription) -> {
                    try {
                        return mapper.writeValueAsString(newDescription);
                    } catch (JsonProcessingException e) {
                        throw new IllegalArgumentException(e);
                    }
                })
                .orElseThrow(this::imageNotFound);
    }

    @GetMapping(value = CONTENT_PROPERTY_PATTERN)
    public ResponseEntity<Resource> getContent(
            @PathVariable UUID externalKey) {
        return imageService.get(externalKey)
                .map((image) -> {
                    try {
                        return ResponseEntity.ok()
                                .header(HttpHeaders.CONTENT_DISPOSITION, dispositionHeader(image.getName()))
                                .header(HttpHeaders.CONTENT_TYPE, image.getContentType())
                                .body(imageService.retrieve(image));
                    } catch (IOException e) {
                        throw new ResponseStatusException(
                                HttpStatus.INTERNAL_SERVER_ERROR, NOT_RETRIEVED_MESSAGE, e);
                    }
                })
                .orElseThrow(ImageNotFoundException::new);
    }

    private ResponseStatusException imageNotFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, IMAGE_NOT_FOUND_REASON);
    }

    private String dispositionHeader(String filename) {
        return String.format(ATTACHMENT_DISPOSITION_FORMAT, filename);
    }
}
