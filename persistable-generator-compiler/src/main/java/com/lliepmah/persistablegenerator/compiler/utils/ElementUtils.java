package com.lliepmah.persistablegenerator.compiler.utils;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
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


  public static String findGetter(String name, TypeMirror type, List<ExecutableElement> methodElements) {
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
}
