package edu.cnm.deepdive.animalsservice.exception;

import org.springframework.context.annotation.Profile;

import java.util.NoSuchElementException;

@Profile("service")
public class NotFoundException extends NoSuchElementException {

  public static final String DEFAULT_MESSAGE = "Resource not found";

  public NotFoundException() {
    this(DEFAULT_MESSAGE);
  }

  public NotFoundException(String message) {
    super(message);
  }

}
