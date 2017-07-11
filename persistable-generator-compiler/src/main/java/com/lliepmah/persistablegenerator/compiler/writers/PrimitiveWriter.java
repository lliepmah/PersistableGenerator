package com.lliepmah.persistablegenerator.compiler.writers;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import static com.lliepmah.persistablegenerator.compiler.PersistableGeneratorCompiler.STRING_CLASSNAME;

/**
 * @author Arthur Korchagin on 11.07.17.
 */

public class PrimitiveWriter
    implements com.lliepmah.persistablegenerator.compiler.writers.CodeBlockWriter {

  public PrimitiveWriter() {
  }

  private String getPrimitiveName(TypeMirror type) {
    TypeKind kind = type.getKind();
    switch (kind) {
      case INT:
        return "Int";
      case LONG:
        return "Long";
      case BOOLEAN:
        return "Boolean";
      case BYTE:
        return "Byte";
      case SHORT:
        return "Short";
      case CHAR:
        return "Char";
      case FLOAT:
        return "Float";
      case DOUBLE:
        return "Double";
    }
    if (STRING_CLASSNAME.equals(ClassName.get(type))) {
      return "String";
    }
    return null;
  }

  @Override public boolean write(TypeMirror type, String name, MethodSpec.Builder builder) {
    String primitiveName = getPrimitiveName(type);
    boolean isPrimitive = primitiveName != null;
    if (isPrimitive) {
      builder.addStatement("out.write" + primitiveName + "(" + name + ")");
    }
    return isPrimitive;
  }
}
