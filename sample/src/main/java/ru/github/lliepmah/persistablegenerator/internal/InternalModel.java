package ru.github.lliepmah.persistablegenerator.internal;

import com.lliepmah.persistablegenerator.PersistableGenerator;
import com.lliepmah.persistablegenerator.PersistableModel;

/**
 * @author Arthur Korchagin on 11.07.17.
 */

@PersistableGenerator public class InternalModel implements PersistableModel {

  String internalProperty;

  public InternalModel(String internalProperty) {
    this.internalProperty = internalProperty;
  }
}
