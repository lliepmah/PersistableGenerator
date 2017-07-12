package com.lliepmah.persistablegenerator.compiler.exceptions;

import javax.lang.model.element.Element;

/**
 * @author Arthur Korchagin on 12.07.17.
 */

public class ElementException extends Exception {

  private final Element mElement;

  public ElementException(String message, Element element) {
    super(message);
    mElement = element;
  }

  public Element getElement() {
    return mElement;
  }
}
