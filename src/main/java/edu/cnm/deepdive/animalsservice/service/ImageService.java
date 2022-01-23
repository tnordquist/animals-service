package edu.cnm.deepdive.animalsservice.service;

import edu.cnm.deepdive.animalsservice.model.dao.ImageRepository;
import edu.cnm.deepdive.animalsservice.model.entity.Image;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.data.util.Streamable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

/**
 * Implements high-level operations on {@link Image} instances, including file store operations and
 * delegation to methods declared in {@link ImageRepository}.
 */
@Service
@Profile("service")
public class ImageService {

    private final ImageRepository imageRepository;
    private final StorageService storageService;

    private static final String UNTITLED_FILENAME = "untitled";

    @Autowired
    public ImageService(
            ImageRepository imageRepository, StorageService storageService) {
        this.imageRepository = imageRepository;
        this.storageService = storageService;
    }

    /**
     * Selects and returns a {@link Image} with the specified {@code key}, as the content of an {@link
     * Optional Optional&lt;Image&gt;}. If no such instance exists, the {@link Optional} is empty.
     *
     * @param key Unique identifier of the {@link Image}.
     * @return {@link Optional Optional&lt;Image&gt;} containing the selected image.
     */
    public Optional<Image> get(UUID key) {
        return imageRepository.findByExternalKey(key);
    }

    public Optional<String> updateDescription(UUID externalKey, String description) {
        return imageRepository.findByExternalKey(externalKey)
                .map((image) -> {
                    image.setDescription(description);
                    return imageRepository.save(image);
                })
                .map(Image::getDescription);

    }

    /**
     * Deletes the specified {@link Image} instance from the database and the file store. It's assumed
     * that any access control conditions have already been checked.
     *
     * @param image Previously persisted {@link Image} instance to be deleted.
     * @throws IOException If the file cannot be accessed (for any reason) from the specified {@code
     *                     reference}.
     */
    public void delete(Image image) throws IOException {
        storageService.delete(image.getPath());
        imageRepository.delete(image); // Delete unconditonally.
    }

    public Image save(@NonNull Image image) {
        return imageRepository.save(image);
    }

    public Image store(
            @NonNull MultipartFile file, String title, String description)
            throws IOException, HttpMediaTypeNotAcceptableException {
        String originalFilename = file.getOriginalFilename();
        String contentType = file.getContentType();
        String reference = storageService.store(file);
        Image image = new Image();
        image.setTitle(title);
        image.setDescription(description);
        image.setName(originalFilename != null ? originalFilename : UNTITLED_FILENAME);
        image.setContentType(contentType != null ? contentType : MediaType.APPLICATION_OCTET_STREAM_VALUE);
        image.setPath(reference);
        return imageRepository.save(image);
    }

    /**
     * Uses the opaque reference contained in {@code image} to return a consumer-usable {@link
     * Resource} to previously uploaded content.
     *
     * @param image {@link Image} entity instance referencing the uploaded content.
     * @return {@link Resource} usable in a response body (e.g. for downloading).
     * @throws IOException If the file content cannot&mdash;for any reason&mdash;be read from the file
     *                     store.
     */
    public Resource retrieve(Image image) throws IOException {
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
        } else images = imageRepository.findAllByOrderByTitleAsc();
        return images;
    }

    /**
     * Convenience class extending {@link ResponseStatusException}, for the purpose of including a
     * default HTTP response status &amp; message when the no-parameter constructor is used.
     */
    public static class ImageNotFoundException extends ResponseStatusException {

        private static final String IMAGE_NOT_FOUND_REASON = "Image not found";

        /**
         * Initializes this instance with a relevant message &amp; response status.
         */
        public ImageNotFoundException() {
            super(HttpStatus.NOT_FOUND, IMAGE_NOT_FOUND_REASON);
        }

    }
}

