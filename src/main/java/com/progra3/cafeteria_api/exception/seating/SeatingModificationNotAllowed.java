package com.progra3.cafeteria_api.exception.seating;

public class SeatingModificationNotAllowed extends RuntimeException {
  public SeatingModificationNotAllowed(String message) {
    super(message);
  }
}
