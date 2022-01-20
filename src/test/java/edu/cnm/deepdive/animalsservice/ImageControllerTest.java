package edu.cnm.deepdive.animalsservice;

import edu.cnm.deepdive.animalsservice.model.entity.Image;
import edu.cnm.deepdive.animalsservice.service.ImageService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.request.ParameterDescriptor;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;

import java.io.InputStream;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedResponseFields;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
@SpringBootTest(classes = AnimalsServiceApplication.class)
class ImageControllerTest {

    private final ImageService imageService;

    @Value("${rest-docs.scheme}")
    private String docScheme;

    @Value("${rest-docs.host}")
    private String docHost;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    private String contextPathPart;
    private MockMvc mockMvc;

    @Autowired
    ImageControllerTest(ImageService imageService) {
        this.imageService = imageService;
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
    public void postAnimal_invalid() throws Exception {

        InputStream input = new DefaultResourceLoader()
                .getResource("images/koala.jpg")
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
                .andExpect(status().isNotFound())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(jsonPath("$.status", is(404)))
                .andDo(
                        document(
                                "images/post-invalid",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                relaxedRequestParts(partWithName("file").description("Image to be uploaded")),
                                relaxedRequestParameters(getPostParameters()),
                                relaxedResponseFields(CommonFieldDescriptors.getExceptionFields())
                        )
                );

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
    void listAnimals_all() throws Exception{

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
        imageService.list();
        mockMvc.perform(
                get("/{contextPathPart}/images", contextPathPart)
                        .contextPath(contextPath)
        )
                .andExpect(status().isOk());
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
    void getDescription() {
    }

    @Test
    void putDescription() {
    }

    @Test
    void getContent() {
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