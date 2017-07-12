package ru.github.lliepmah.persistablegenerator;

import com.lliepmah.persistablegenerator.PersistableGenerator;

/**
 * ModelCar
 *
 * @author Arthur Korchagin on 11.07.17.
 */
@PersistableGenerator class ModelCar {

  String carName;
  int elegibleName;

  public ModelCar(String carName, int elegibleName) {
    this.carName = carName;
    this.elegibleName = elegibleName;
  }
}
