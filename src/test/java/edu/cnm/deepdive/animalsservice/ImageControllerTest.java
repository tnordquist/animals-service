package edu.cnm.deepdive.animalsservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.cnm.deepdive.animalsservice.model.entity.Image;
import edu.cnm.deepdive.animalsservice.service.ImageService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.RestDocumentationContext;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.request.ParameterDescriptor;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultHandler;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static com.fasterxml.jackson.databind.type.LogicalType.Map;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
@SpringBootTest(classes = AnimalsServiceApplication.class)
class ImageControllerTest {

    private final ImageService imageService;
    private final ObjectMapper objectMapper;

    @Value("${rest-docs.scheme}")
    private String docScheme;

    @Value("${rest-docs.host}")
    private String docHost;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    private String contextPathPart;
    private MockMvc mockMvc;

    @Autowired
    ImageControllerTest(ImageService imageService, ObjectMapper objectMapper) {
        this.imageService = imageService;
        this.objectMapper = objectMapper;
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


    @Test
    public void postAnimal_valid() throws Exception {

        InputStream input = new DefaultResourceLoader()
                .getResource("images/donkey.jpg")
                .getInputStream();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "donkey.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                input
        );
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.set("title", "Donkey");
        mockMvc
                .perform(
                        multipart("/images")
                                .file(file)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .params(params)
                )
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.title", is("Donkey")))
                .andDo(
                        document(
                                "images/post-valid",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                relaxedRequestParts(partWithName("file").description("Image to be uploaded")),
                                relaxedRequestParameters(getPostParameters()),
                                relaxedResponseFields(getImageFields())
                        )
                );

    }

    @Test
    public void postAnimal_invalid() throws Exception {

        InputStream input = new DefaultResourceLoader()
                .getResource("images/donkey.jpg")
                .getInputStream();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "donkey.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                input
        );

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.set("titl", "Dog");

        mockMvc
                .perform(
                        multipart("/images")
                                .file(file)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .params(params)
                )
                .andExpect(status().isBadRequest())
                .andExpect(header().doesNotExist("Location"))
                .andDo(
                        document(
                                "images/post-invalid",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint())
                        )
                );

    }

    @Test
    void getAnimal_valid() throws Exception {
        InputStream input = new DefaultResourceLoader()
                .getResource("images/donkey.jpg")
                .getInputStream();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "donkey.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                input
        );
        Image image = imageService.store(file, "Donkey", "A domesticated ass.");
        mockMvc.perform(
                        get("/{contextPathPart}/images/{id}", contextPathPart, image.getExternalKey())
                                .contextPath(contextPath)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(image.getExternalKey().toString())))
                .andExpect(jsonPath("$.title", is("Donkey")))
                .andExpect(jsonPath("$.description", is("A domesticated ass.")))
                .andDo(
                        document(
                                "images/get-valid",
                                preprocessResponse(prettyPrint()),
                                pathParameters(getPathVariables()),
                                relaxedResponseFields(getImageFields())
                        )
                );
    }

    @Test
    void getAnimal_invalid() throws Exception {

        mockMvc.perform(
                        get("/{contextPathPart}/images/00000000000000000000000000", contextPathPart)
                                .contextPath(contextPath)
                )
                .andExpect(status().isNotFound())
                .andDo(
                        document(
                                "images/get-invalid"
                        )
                );
    }


    @Test
    void listAnimals_valid() throws Exception {

        InputStream input = new DefaultResourceLoader()
                .getResource("images/donkey.jpg")
                .getInputStream();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "donkey.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                input
        );
        imageService.store(file, "Donkey", "A domesticated ass.");

        input = new DefaultResourceLoader()
                .getResource("images/green-frog.jpg")
                .getInputStream();
        file = new MockMultipartFile(
                "file",
                "green-frog.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                input
        );
        imageService.store(file, "Green Frog", "A green frog commonly found in Virginia.");

        mockMvc.perform(
                        get("/{contextPathPart}/images", contextPathPart)
                                .contextPath(contextPath)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                )
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(2)
                )
                .andDo(
                        document(
                                "images/list-all",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                relaxedRequestParameters(getQueryParameters())
                        )
                );
    }

    @Test
    void listAnimals_invalid() throws Exception {

        InputStream input = new DefaultResourceLoader()
                .getResource("images/donkey.jpg")
                .getInputStream();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "donkey.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                input
        );
        imageService.store(file, "Donkey", "A domesticated ass.");

        input = new DefaultResourceLoader()
                .getResource("images/green-frog.jpg")
                .getInputStream();
        file = new MockMultipartFile(
                "file",
                "green-frog.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                input
        );
        imageService.store(file, "Green Frog", "A green frog commonly found in Virginia.");

        mockMvc.perform(
                        get("/{contextPathPart}/image", contextPathPart)
                                .contextPath(contextPath)
                )
                .andExpect(status().isNotFound())
                .andDo(
                        document(
                                "images/list-all",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                relaxedRequestParameters(getQueryParameters())
                        )
                );
    }

    @Test
    void deleteAnimal_valid() throws Exception {
        InputStream input = new DefaultResourceLoader()
                .getResource("images/donkey.jpg")
                .getInputStream();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "donkey.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                input
        );
        Image image = imageService.store(file, "Donkey", "A domesticated ass.");

        mockMvc.perform(
                        delete("/{contextPathPart}/images/{id}", contextPathPart, image.getExternalKey())
                                .contextPath(contextPath)
                )
                .andExpect(status().isNoContent())
                .andDo(
                        document(
                                "images/delete-valid",
                                pathParameters(getPathVariables())
                        )
                );
    }

    @Test
    void deleteAnimal_invalid() throws Exception {

        mockMvc.perform(
                        delete("/{contextPathPart}/images/000000000000000000000000000", contextPathPart)
                                .contextPath(contextPath)
                )
                .andExpect(status().isNotFound())
                .andDo(document("images/delete-invalid"));
    }

    @Test
    void getDescription_valid() throws Exception {

        InputStream input = new DefaultResourceLoader()
                .getResource("images/donkey.jpg")
                .getInputStream();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "donkey.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                input
        );
        Image image = imageService.store(file, "Donkey", "A domesticated ass.");
        mockMvc.perform(
                        get("/{contextPathPart}/images/{id}/description", contextPathPart,
                                image.getExternalKey())
                                .contextPath(contextPath)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is("A domesticated ass.")))
                .andDo(
                        document(
                                "images/get-valid",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(getPathVariables())

                        )
                );
    }

    @Test
    void putDescription_valid() throws Exception {

        InputStream input = new DefaultResourceLoader()
                .getResource("images/donkey.jpg")
                .getInputStream();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "donkey.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                input
        );
        Image image = imageService.store(file, "Donkey", "A wild ass.");
        String newDescription = "A new description";
        mockMvc.perform(
                        put("/{contextPathPart}/images/{id}/description", contextPathPart,
                                image.getExternalKey())
                                .contextPath(contextPath)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(newDescription))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(newDescription)))
                .andDo(
                        document(
                                "images/put-valid",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(getPathVariables())
                        )
                );
    }

   /* @Test
    void putDescription_invalid() throws Exception {

        InputStream input = new DefaultResourceLoader()
                .getResource("images/donkey.jpg")
                .getInputStream();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "donkey.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                input
        );
        Image image = imageService.store(file, "Donkey", "A wild ass.");
        String newDescription = "A new description";
        mockMvc.perform(
                        put("/{contextPathPart}/images/{id}/description", contextPathPart,
                                image.getExternalKey())
                                .contextPath(contextPath)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(newDescription))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(newDescription)))
                .andDo(
                        document(
                                "images/put-valid",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(getPathVariables())
                        )
                );
    }*/

    @Test
    void getContent_valid() throws Exception {

        Resource resource = new DefaultResourceLoader()
                .getResource("images/donkey.jpg");
        try (
                InputStream input = resource.getInputStream();
                InputStream checkStream = resource.getInputStream();
        ) {

            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "donkey.jpg",
                    MediaType.IMAGE_JPEG_VALUE,
                    input
            );
            Image image = imageService.store(file, "Donkey", "A domesticated ass.");

            byte[] checkContents = new byte[(int) resource.contentLength()];
            checkStream.read(checkContents);
            mockMvc.perform(
                            get("/{contextPathPart}/images/{id}/content", contextPathPart, image.getExternalKey())
                                    .contextPath(contextPath)
                    )
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.IMAGE_JPEG_VALUE))
                    .andExpect(content().bytes(checkContents))
                    .andDo(
                            document(
                                    "images/content-valid",
                                    preprocessRequest(prettyPrint()),
                                    pathParameters(getPathVariables()),
                                    (operation) -> {
                                        Map<String, Object> attributes = operation.getAttributes();
                                        RestDocumentationContext docContext = (RestDocumentationContext) attributes
                                                .get("org.springframework.restdocs.RestDocumentationContext");
                                        Path output = Path.of(docContext.getOutputDirectory().toString(), operation.getName(), "donkey.jpg");
                                        output.toFile().delete();
                                        Files.copy(new ByteArrayResource(operation.getResponse().getContent()).getInputStream(), output);
                                        System.out.println(attributes);
                                    }
                            )
                    );
        }
    }

    private List<ParameterDescriptor> getPathVariables() {
        return List.of(
                parameterWithName("id")
                        .description("Unique identifier of image."),
                parameterWithName("contextPathPart")
                        .ignored()
        );
    }

    private List<ParameterDescriptor> getPostParameters() {
        return List.of(
//                parameterWithName("file").description("Image to be uploaded with title and optional description."),
                parameterWithName("title").description("Title of uploaded image."),
                parameterWithName("description").description("Description of uploaded image.").optional()
        );
    }

    private List<ParameterDescriptor> getQueryParameters() {
        return List.of(
                parameterWithName("status")
                        .description(
                                "Status filter for selecting subset of codes: `ALL` (default), `UNSOLVED`, `SOLVED`.")
                        .optional()
        );
    }

    private List<FieldDescriptor> getImageFields() {
        return List.of(
                fieldWithPath("id")
                        .type(JsonFieldType.STRING)
                        .description("Unique identifier of image."),
                fieldWithPath("href")
                        .type(JsonFieldType.STRING)
                        .description("Resource URL of image."),
                fieldWithPath("created")
                        .type(JsonFieldType.STRING)
                        .description("Timestamp of initial creation of image."),
                fieldWithPath("updated")
                        .type(JsonFieldType.STRING)
                        .description("Timestamp of latest modification of image."),
                fieldWithPath("title")
                        .type(JsonFieldType.STRING)
                        .description("Title of the uploaded image."),
                fieldWithPath("description")
                        .type(JsonFieldType.STRING)
                        .description("Optional description for uploaded image.")
                        .optional()

        );
    }
}