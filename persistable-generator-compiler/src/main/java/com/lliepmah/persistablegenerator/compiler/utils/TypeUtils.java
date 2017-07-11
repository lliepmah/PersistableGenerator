package com.lliepmah.persistablegenerator.compiler.utils;

import com.squareup.javapoet.TypeName;
import javax.lang.model.type.TypeKind;

/**
 * @author Arthur Korchagin on 11.07.17.
 */

public class TypeUtils {

  private TypeUtils() {
  }

  public static TypeKind getPrimitiveTypeKind(TypeName typeName) {

    if (typeName == TypeName.BOOLEAN) return TypeKind.BOOLEAN;
    if (typeName == TypeName.BYTE) return TypeKind.BYTE;
    if (typeName == TypeName.SHORT) return TypeKind.SHORT;
    if (typeName == TypeName.INT) return TypeKind.INT;
    if (typeName == TypeName.LONG) return TypeKind.LONG;
    if (typeName == TypeName.CHAR) return TypeKind.CHAR;
    if (typeName == TypeName.FLOAT) return TypeKind.FLOAT;
    if (typeName == TypeName.DOUBLE) return TypeKind.DOUBLE;

    return null;
  }
}
