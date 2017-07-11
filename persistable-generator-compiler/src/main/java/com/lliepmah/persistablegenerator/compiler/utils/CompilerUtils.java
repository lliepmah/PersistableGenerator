package com.lliepmah.persistablegenerator.compiler.utils;

import com.squareup.javapoet.ClassName;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

/**
 * @author Arthur Korchagin on 11.07.17.
 */

public class CompilerUtils {

  private static final String NULLABLE_ANNOTATION_NAME = "Nullable";

  private CompilerUtils() {
  }

  private static boolean isTypeEqual(TypeMirror typeMirror, String otherType) {
    return otherType.equals(typeMirror.toString());
  }

  static boolean isSubtypeOfType(TypeMirror typeMirror, String otherType) {
    if (isTypeEqual(typeMirror, otherType)) {
      return true;
    }
    if (typeMirror.getKind() != TypeKind.DECLARED) {
      return false;
    }
    DeclaredType declaredType = (DeclaredType) typeMirror;
    List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
    if (typeArguments.size() > 0) {
      StringBuilder typeString = new StringBuilder(declaredType.asElement().toString());
      typeString.append('<');
      for (int i = 0; i < typeArguments.size(); i++) {
        if (i > 0) {
          typeString.append(',');
        }
        typeString.append('?');
      }
      typeString.append('>');
      if (typeString.toString().equals(otherType)) {
        return true;
      }
    }
    Element element = declaredType.asElement();
    if (!(element instanceof TypeElement)) {
      return false;
    }
    TypeElement typeElement = (TypeElement) element;
    TypeMirror superType = typeElement.getSuperclass();
    if (isSubtypeOfType(superType, otherType)) {
      return true;
    }
    for (TypeMirror interfaceType : typeElement.getInterfaces()) {
      if (isSubtypeOfType(interfaceType, otherType)) {
        return true;
      }
    }
    return false;
  }

  static boolean hasAnnotationWithName(Element element, String simpleName) {
    for (AnnotationMirror mirror : element.getAnnotationMirrors()) {
      String annotationName = mirror.getAnnotationType().asElement().getSimpleName().toString();
      if (simpleName.equals(annotationName)) {
        return true;
      }
    }
    return false;
  }

  static boolean isFieldRequired(Element element) {
    return !hasAnnotationWithName(element, NULLABLE_ANNOTATION_NAME);
  }

  static AnnotationMirror getMirror(Element element, Class<? extends Annotation> annotation) {
    for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
      if (annotationMirror.getAnnotationType().toString().equals(annotation.getCanonicalName())) {
        return annotationMirror;
      }
    }
    return null;
  }

  /** Finds the parent binder type in the supplied set, if any. */
  static boolean hasParentType(TypeElement typeElement, final ClassName parent) {

    TypeMirror type;
    while (true) {
      type = typeElement.getSuperclass();
      if (type.getKind() == TypeKind.NONE) {
        return false;
      }

      typeElement = (TypeElement) ((DeclaredType) type).asElement();

      if (parent.equals(ClassName.get(type))) {
        return true;
      }
    }
  }

  static TypeElement findParentType(TypeElement typeElement, Set<TypeElement> parents) {
    TypeMirror type;
    while (true) {
      type = typeElement.getSuperclass();
      if (type.getKind() == TypeKind.NONE) {
        return null;
      }
      typeElement = (TypeElement) ((DeclaredType) type).asElement();
      if (parents.contains(typeElement)) {
        return typeElement;
      }
    }
  }

  static boolean implementsInterface(TypeElement typeElement, final ClassName parent) {


    for (TypeMirror type : typeElement.getInterfaces()) {
      typeElement = (TypeElement) ((DeclaredType) type).asElement();

      if (parent.equals(ClassName.get(typeElement))) {
        return true;
      }
    }
    return false;

  }

  static TypeElement findParentInterface(TypeElement typeElement, Set<TypeElement> parents) {
    for (TypeMirror type : typeElement.getInterfaces()) {
      typeElement = (TypeElement) ((DeclaredType) type).asElement();
      if (parents.contains(typeElement)) {
        return typeElement;
      }
    }
    return null;
  }

  static TypeElement getGeneric(DeclaredType declaredType) {
    List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
    TypeMirror firstElement;
    if (typeArguments != null && typeArguments.size() == 1 && ((firstElement =
        typeArguments.get(0)) instanceof DeclaredType)) {
      return (TypeElement) ((DeclaredType) firstElement).asElement();
    }
    return null;
  }
}
