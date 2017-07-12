package com.lliepmah.persistablegenerator.compiler.writers;

import com.lliepmah.persistablegenerator.PersistableGenerator;
import com.lliepmah.persistablegenerator.compiler.names.Classes;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import java.util.List;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import static com.lliepmah.persistablegenerator.compiler.utils.TypeUtils.getPrimitiveTypeKind;

/**
 * @author Arthur Korchagin on 11.07.17.
 */

public class ListWriter
    implements com.lliepmah.persistablegenerator.compiler.writers.CodeBlockWriter {

  private final PrimitiveWriter mPrimitiveWriter;
  private final PersistableWriter mPersistableWriter;

  private final Types mTypes;
  private final Elements mElementUtils;

  private final PrimitiveType mIntType;

  public ListWriter(PrimitiveWriter primitiveWriter, PersistableWriter persistableWriter,
      Types types, Elements elementUtils) {
    mPrimitiveWriter = primitiveWriter;
    mTypes = types;
    mElementUtils = elementUtils;
    mPersistableWriter = persistableWriter;
    mIntType = mTypes.getPrimitiveType(TypeKind.INT);
  }

  @Override public boolean write(TypeMirror type, String name, MethodSpec.Builder builder) {
    TypeName typeName = ClassName.get(type);

    if (!ParameterizedTypeName.class.isInstance(typeName)) {
      return false;
    }

    ParameterizedTypeName parameterizedTypeName = (ParameterizedTypeName) typeName;
    ClassName className = parameterizedTypeName.rawType;
    List<TypeName> typeArguments = parameterizedTypeName.typeArguments;

    if (className.equals(Classes.LIST) && typeArguments != null && typeArguments.size() == 1) {
      TypeName typeArgumentName = typeArguments.get(0);
      TypeElement typeElement = mElementUtils.getTypeElement(typeArgumentName.toString());

      CodeBlockWriter writer = null;
      TypeMirror typeArgument = null;

      if (checkPrimitive(typeArgumentName)) {
        writer = mPrimitiveWriter;

        if (typeArgumentName.isBoxedPrimitive()) {
          typeArgumentName = typeArgumentName.unbox();
          typeArgument = getPrimitiveTypeMirror(typeArgumentName);
        } else {
          typeArgument = typeElement.asType();
        }
      } else if (typeElement != null
          && typeElement.getAnnotation(PersistableGenerator.class) != null) {
        typeArgument = typeElement.asType();
        writer = mPersistableWriter;
      }

      if (writer != null && typeArgument != null) {
        mPrimitiveWriter.write(mIntType, name + ".size()", builder);
        builder.addCode("for ($T val : " + name + ") {\n", typeArgumentName);
        writer.write(typeArgument, "val", builder);
        builder.addCode("}\n");
        return true;
      }
    }

    return false;
  }

  private TypeMirror getPrimitiveTypeMirror(TypeName typeName) {
    return mTypes.getPrimitiveType(getPrimitiveTypeKind(typeName));
  }

  private boolean checkPrimitive(TypeName typeName) {
    return typeName.isBoxedPrimitive() || typeName.equals(Classes.STRING);
  }
}
