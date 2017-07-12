package com.lliepmah.persistablegenerator.compiler.entity;

import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

/**
 * @author Arthur Korchagin on 12.07.17.
 */

public final class Property {

  private final String mName;
  private final TypeMirror mTypeMirror;
  private final VariableElement mField;

  public Property(VariableElement field, String name, TypeMirror typeMirror) {
    mName = name;
    mTypeMirror = typeMirror;
    mField = field;
  }

  public VariableElement getField() {
    return mField;
  }

  public String getName() {
    return mName;
  }

  public TypeMirror getTypeMirror() {
    return mTypeMirror;
  }
}
