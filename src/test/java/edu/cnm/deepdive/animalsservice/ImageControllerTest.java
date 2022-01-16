package edu.cnm.deepdive.animalsservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.cnm.deepdive.animalsservice.service.ImageService;
import edu.cnm.deepdive.animalsservice.service.LocalFilesystemStorageService;
import org.h2.util.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
@SpringBootTest(classes = AnimalsServiceApplication.class)
class ImageControllerTest {

    private final ImageService imageService;
    private final ObjectMapper objectMapper;

    @MockBean
    private final LocalFilesystemStorageService localFileService;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Value("${rest-docs.scheme}")
    private String docScheme;

    @Value("${rest-docs.host}")
    private String docHost;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    private String contextPathPart;
    private MockMvc mockMvc;

    @Autowired
    ImageControllerTest(
            ObjectMapper objectMapper, ImageService imageService, LocalFilesystemStorageService localFileService) {
        this.objectMapper = objectMapper;
        this.imageService = imageService;
        this.localFileService = localFileService;
    }

    @BeforeEach
    public void setup(WebApplicationContext webApplicationContext,
                      RestDocumentationContextProvider restDocumentation) {
        contextPathPart = contextPath.startsWith("/") ? contextPath.substring(1) : contextPath;
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(
                        documentationConfiguration(restDocumentation)
                                .uris()
                                .withScheme(docScheme)
                                .withHost(docHost)
                                .withPort(443)
                )
                .build();
    }

    @AfterEach
    void tearDown(WebApplicationContext webApplicationContext,
                  RestDocumentationContextProvider restDocumentation) {
    }

//    @Test
//    public void whenPostAnimal_thenVerifyStatus() throws Exception {
//
//        FileInputStream input = new FileInputStream("/donkey.jpg");
//        MediaType mediaType = new MediaType("multipart", "form-data");
//        MockMultipartFile file = new MockMultipartFile(
//                "file",
//                "",
//        );

//        mockMvc
//                .perform(
//                        multipart("/images").file(file))
//                .andExpect(status().isOk());
//    }

    @Test
    void get() {
    }

    @Test
    void list() {
    }

    @Test
    void delete() {
    }

    @Test
    void getDescription() {
    }

    @Test
    void putDescription() {
    }

    @Test
    void getContent() {
    }
}