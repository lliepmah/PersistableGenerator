package com.lliepmah.persistablegenerator.compiler.utils;

import com.lliepmah.persistablegenerator.compiler.entity.Property;
import com.lliepmah.persistablegenerator.compiler.exceptions.ElementException;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

/**
 * @author Arthur Korchagin on 11.07.17.
 */

public class ElementUtils {

  private ElementUtils() {
  }

  public static boolean isStatic(Element element) {
    return element.getModifiers().contains(Modifier.STATIC);
  }

  public static boolean isPrivate(Element element) {
    return element.getModifiers().contains(Modifier.PRIVATE);
  }

  public static String findGetter(String name, TypeMirror type,
      List<ExecutableElement> methodElements) {
    String getterName = "get" + name.substring(0, 1).toUpperCase() + name.substring(1);

    for (ExecutableElement method : methodElements) {

      TypeName fieldMirror = ClassName.get(type);
      TypeName returnType = ClassName.get(method.getReturnType());

      String methodName = method.getSimpleName().toString();

      if (method.getParameters().size() == 0 && fieldMirror.equals(returnType) && methodName.equals(
          getterName)) {
        return getterName + "()";
      }
    }
    return null;
  }

  public static void splitMembers(List<? extends Element> source, List<VariableElement> destFields,
      List<ExecutableElement> descMethods) {
    for (Element member : source) {
      if (member instanceof VariableElement) {
        destFields.add((VariableElement) member);
      } else if (member.getKind() == ElementKind.METHOD) {
        descMethods.add((ExecutableElement) member);
      }
    }
  }

  public static LinkedList<Property> extractProperties(final List<? extends Element> members)
      throws ElementException {
    return new LinkedList<Property>() {{

      final List<VariableElement> fieldElements = new ArrayList<>();
      final List<ExecutableElement> methodElements = new ArrayList<>();

      ElementUtils.splitMembers(members, fieldElements, methodElements);

      for (VariableElement field : fieldElements) {
        if (!ElementUtils.isStatic(field)) {
          String propertyName = field.getSimpleName().toString();
          TypeMirror type = field.asType();

          if (ElementUtils.isPrivate(field)) {
            propertyName = ElementUtils.findGetter(propertyName, type, methodElements);
          }

          if (propertyName != null) {
            add(new Property(field, propertyName, type));
          } else {
            throw new ElementException(String.format("Field %1$s is private and haven't getter",
                field.getSimpleName().toString()), field);
          }
        }
      }
    }};
  }
}
