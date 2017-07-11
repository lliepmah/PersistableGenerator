package com.lliepmah.persistablegenerator.compiler.writers;

import com.squareup.javapoet.MethodSpec;
import javax.lang.model.type.TypeMirror;

/**
 * @author Arthur Korchagin on 11.07.17.
 */

public interface CodeBlockWriter {

  boolean write(TypeMirror type, String name, MethodSpec.Builder builder);
}
