package ru.github.lliepmah.persistablegenerator.internal;

import com.lliepmah.persistablegenerator.PersistableGenerator;

@PersistableGenerator public class SomeModel {
  int age;
  private String name;

  public SomeModel() {
  }

  public SomeModel(int age, String name) {
    this.age = age;
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public int getAge() {
    return age;
  }
}