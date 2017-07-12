package com.lliepmah.persistablegenerator.compiler.names;

import com.squareup.javapoet.ClassName;
import java.util.List;

/**
 * @author Arthur Korchagin on 12.07.17.
 */

public interface Classes {

  ClassName LIST = ClassName.get(List.class);
  ClassName STRING = ClassName.get(String.class);

  ClassName DATA_OUTPUT = ClassName.get(Packages.BINARYPREFS_PERSISTABLE_IO, "DataOutput");
  ClassName DATA_INPUT = ClassName.get(Packages.BINARYPREFS_PERSISTABLE_IO, "DataInput");
  ClassName PERSISTABLE = ClassName.get(Packages.BINARYPREFS_PERSISTABLE, "Persistable");

  String POSTFIX_PERSISTABLE = "Persistable";
}
