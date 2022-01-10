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
package edu.cnm.deepdive.animalsservice.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonView;
import edu.cnm.deepdive.animalsservice.view.ImageView;
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
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties( // TODO Replace with individual @JsonProperty annotations on fields.
    value = {"id", "created", "contributor"},
    allowGetters = true, ignoreUnknown = true
)
@JsonPropertyOrder({"id", "href", "created", "name", "description"})
@JsonView(ImageView.Partial.class)
@Component
public class Image {

  private static EntityLinks entityLinks;

  @NonNull
  @Id
  @GeneratedValue
  @Column(name = "image_id", updatable = false, columnDefinition = "UUID")
  private UUID id;

  // TODO Add an externalKey field for "public" UUID.
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
  @JsonIgnore
  @Column(nullable = false, updatable = false)
  private String name;


  @Column(length = 100)
  private String title;

  @JsonView(ImageView.Full.class)
  @Column(length = 1024)
  private String description;

  @JsonView(ImageView.Full.class)
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

  @Override
  public boolean equals(Object obj) {
    return (this == obj || (obj instanceof Image && this.id.equals(((Image) obj).id)));
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