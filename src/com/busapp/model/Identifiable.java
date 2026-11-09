package com.busapp.model;

import java.io.Serializable;

/** Anything stored in a Repository must have an id and be serializable. */
public interface Identifiable extends Serializable {
    String getId();
}
