package com.lliepmah.persistablegenerator.compiler.writers;

import com.lliepmah.persistablegenerator.PersistableGenerator;
import com.lliepmah.persistablegenerator.compiler.names.Classes;
import com.lliepmah.persistablegenerator.compiler.names.Methods;
import com.lliepmah.persistablegenerator.compiler.names.Variables;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;

/**
 * @author Arthur Korchagin on 11.07.17.
 */

public class PersistableWriter
    implements com.lliepmah.persistablegenerator.compiler.writers.CodeBlockWriter {

  private final Elements mElementUtils;

  public PersistableWriter(Elements elementUtils) {
    mElementUtils = elementUtils;
  }

  @Override public boolean write(TypeMirror type, String name, MethodSpec.Builder builder) {
    TypeElement typeElement = mElementUtils.getTypeElement(type.toString());
    if (typeElement != null && typeElement.getAnnotation(PersistableGenerator.class) != null) {
      ClassName className = ClassName.get(mElementUtils.getPackageOf(typeElement).toString(),
          typeElement.getSimpleName() + Classes.POSTFIX_PERSISTABLE);

      builder.addStatement("new $T($L).$L($L)", className, name, Methods.WRITE_EXTERNAL,
          Variables.OUT);
      return true;
    }
    return false;
  }
}
