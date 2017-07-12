package com.lliepmah.persistablegenerator.compiler.utils;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;

import static com.lliepmah.persistablegenerator.compiler.utils.ElementUtils.isPrivate;
import static com.lliepmah.persistablegenerator.compiler.utils.ElementUtils.isStatic;

/**
 * @author Arthur Korchagin on 11.07.17.
 */

public class TypeUtils {

  public static final String METHOD_NAME_COPY = "copy";

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

  public static boolean hasCopyMethod(VariableElement variableElement, Elements elements) {

    TypeMirror typeMirror = variableElement.asType();
    if (!DeclaredType.class.isInstance(typeMirror)) {
      return false;
    }

    Element typeElement = ((DeclaredType) typeMirror).asElement();

    if (!TypeElement.class.isInstance(typeElement)) {
      return false;
    }

    TypeElement fieldTypeElement = (TypeElement) typeElement;

    for (Element member : elements.getAllMembers(fieldTypeElement)) {

      if (member instanceof Symbol.MethodSymbol) {
        Symbol.MethodSymbol method = (Symbol.MethodSymbol) member;
        if (isMethodNamed(method, METHOD_NAME_COPY)
            && !isStatic(method)
            && !isPrivate(method)
            && noParameters(method)
            && isSameType(method.getReturnType(), typeElement.asType())) {
          return true;
        }
      }

    }

    return false;
  }

  private static boolean isMethodNamed(Symbol.MethodSymbol method, CharSequence name) {
    return method.getSimpleName().contentEquals(name);
  }

  private static boolean isSameType(Type type, TypeMirror typeMirror) {
    return ClassName.get(type).equals(ClassName.get(typeMirror));
  }

  private static boolean noParameters(Symbol.MethodSymbol method) {
    return method.getParameters() == null || method.getParameters().size() == 0;
  }
}



