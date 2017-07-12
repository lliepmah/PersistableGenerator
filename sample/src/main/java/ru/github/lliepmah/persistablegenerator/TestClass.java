package ru.github.lliepmah.persistablegenerator;

import android.app.Activity;
import com.lliepmah.persistablegenerator.PersistableGenerator;
import java.util.List;
import ru.github.lliepmah.persistablegenerator.internal.InternalModel;

/**
 * @author Arthur Korchagin on 11.07.17.
 */
@PersistableGenerator class TestClass {

  private String field1;
  private int field2;

  String field3;
  int field4;

  List<String> field5;

  ModelCar field6;

  List<ModelCar> field7;
  InternalModel field8;

  List<Integer> field9;

  Object myObject;
  Activity myActivity;

  private List<Character> field10;

  public List<Character> getField10() {
    return field10;
  }

  public int getField2() {
    return field2;
  }

  public TestClass() {
  }

  public TestClass(int field2, String field3, int field4, List<String> field5, ModelCar field6,
      List<ModelCar> field7, InternalModel field8, List<Integer> field9, List<Character> field10) {
    this.field2 = field2;
    this.field3 = field3;
    this.field4 = field4;
    this.field5 = field5;
    this.field6 = field6;
    this.field7 = field7;
    this.field8 = field8;
    this.field9 = field9;
    this.field10 = field10;
  }
}
