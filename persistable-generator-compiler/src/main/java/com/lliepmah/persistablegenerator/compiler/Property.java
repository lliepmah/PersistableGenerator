package com.lliepmah.persistablegenerator.compiler;

import javax.lang.model.type.TypeMirror;

/**
 * @author Arthur Korchagin on 12.07.17.
 */

public final class Property {

  private final String mName;
  private final TypeMirror mTypeMirror;

  public Property(String name, TypeMirror typeMirror) {
    mName = name;
    mTypeMirror = typeMirror;
  }

  public String getName() {
    return mName;
  }

  public TypeMirror getTypeMirror() {
    return mTypeMirror;
  }
}
