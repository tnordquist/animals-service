package edu.cnm.deepdive.animalsservice.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonView;
import java.net.URI;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;
import javax.annotation.PostConstruct;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.server.EntityLinks;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@SuppressWarnings("JpaDataSourceORMInspection")
@Entity
@Table(
    indexes = {
        @Index(columnList = "created"),
        @Index(columnList = "title")
    }
)
@JsonIgnoreProperties(
    value = {"id", "created", "contributor"},
    allowGetters = true, ignoreUnknown = true
)
@JsonPropertyOrder({"id", "href", "created", "name", "description"})
@Component
public class Image {

  private static EntityLinks entityLinks;

  @NonNull
  @Id
  @GeneratedValue(generator = "uuid2")
  @GenericGenerator(name = "uuid2", strategy = "uuid2")
  @Column(name = "image_id", nullable = false, updatable = false, columnDefinition = "CHAR(16) FOR BIT DATA")
  private UUID id;

  @NonNull
  @CreationTimestamp
  @Temporal(TemporalType.TIMESTAMP)
  @Column(nullable = false, updatable = false)
  private Date created;

  @NonNull
  @Column(nullable = false, updatable = false)
  @JsonIgnore
  private String path;

  @NonNull
  @UpdateTimestamp
  @Temporal(TemporalType.TIMESTAMP)
  @Column(nullable = false)
  private Date updated;

  @NonNull
  @Column(nullable = false, updatable = false)
  private String name;

  @Column(length = 100)
  private String title;

  @Column(length = 1024)
  private String description;


  @NonNull
  @JsonIgnore
  @Column(name = "resource_key", nullable = false, updatable = false)
  private String key;

  @NonNull
  @Column(nullable = false, updatable = false)
  private String contentType;

  @NonNull
  public UUID getId() {
    return id;
  }

  @NonNull
  public Date getCreated() {
    return created;
  }

  @NonNull
  public String getPath() {
    return path;
  }

  public void setPath(@NonNull String path) {
    this.path = path;
  }

  @NonNull
  public Date getUpdated() {
    return updated;
  }

  @NonNull
  public String getName() {
    return name;
  }

  public void setName(@NonNull String name) {
    this.name = name;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }


  @NonNull
  public String getKey() {
    return key;
  }

  public void setKey(@NonNull String key) {
    this.key = key;
  }

  /**
   * Returns the MIME type of this image.
   */
  public String getContentType() {
    return contentType;
  }

  public void setContentType(@NonNull String contentType) {
    this.contentType = contentType;
  }

  @Override
  public int hashCode() {
    //noinspection ConstantConditions
    return (id == null) ? 0 : id.hashCode();
  }

  @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
  @Override
  public boolean equals(Object obj) {
    return Objects.equals(this.id, ((Image) obj).id);
  }

  public URI getHref() {
    //noinspection ConstantConditions
    return (id != null) ? entityLinks.linkForItemResource(Image.class, id).toUri() : null;
  }

  @PostConstruct
  private void initHateoas() {
    //noinspection ResultOfMethodCallIgnored
    entityLinks.toString();
  }

  @Autowired
  public void setEntityLinks(
      @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") EntityLinks entityLinks) {
    Image.entityLinks = entityLinks;
  }
}