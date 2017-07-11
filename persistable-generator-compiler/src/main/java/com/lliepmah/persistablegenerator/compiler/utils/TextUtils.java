package com.lliepmah.persistablegenerator.compiler.utils;

import java.util.Iterator;

/**
 * @author Arthur Korchagin on 12.07.17.
 */

public class TextUtils {
  private TextUtils() {
  }

  public static String join(CharSequence delimiter, Iterable tokens) {
    StringBuilder sb = new StringBuilder();
    Iterator<?> it = tokens.iterator();
    if (it.hasNext()) {
      sb.append(it.next());
      while (it.hasNext()) {
        sb.append(delimiter);
        sb.append(it.next());
      }
    }
    return sb.toString();
  }
}
