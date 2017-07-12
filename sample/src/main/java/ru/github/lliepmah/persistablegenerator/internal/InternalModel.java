package ru.github.lliepmah.persistablegenerator.internal;

import com.lliepmah.persistablegenerator.PersistableGenerator;

/**
 * @author Arthur Korchagin on 11.07.17.
 */

@PersistableGenerator public class InternalModel {

  String internalProperty;

  public InternalModel(String internalProperty) {
    this.internalProperty = internalProperty;
  }
}
