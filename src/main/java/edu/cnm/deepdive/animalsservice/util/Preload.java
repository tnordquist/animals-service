package edu.cnm.deepdive.animalsservice.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.cnm.deepdive.animalsservice.model.dao.ImageRepository;
import edu.cnm.deepdive.animalsservice.model.entity.Image;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;

@Component
@Profile("preload")
public class Preload implements CommandLineRunner {

    public static final String INITIAL_DATA_RESOURCE = "preload/preload-data.json";
    private final ImageRepository repository;

    @Autowired
    public Preload(ImageRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) throws Exception {
        ClassPathResource resource = new ClassPathResource(INITIAL_DATA_RESOURCE);
        try (InputStream input = resource.getInputStream()) {
            ObjectMapper mapper = new ObjectMapper();
            List<Image> listQuestions = mapper.readValue(input, new TypeReference<List<Image>>(){});
            System.out.println("ListQuestions: " + listQuestions.toString());
        }
    }
}
