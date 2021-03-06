package ru.github.lliepmah.persistablegenerator;

import android.app.Activity;
import android.os.Bundle;
import com.ironz.binaryprefs.serialization.serializer.persistable.Persistable;
import ru.github.lliepmah.persistablegenerator.internal.SomeModel;
import ru.github.lliepmah.persistablegenerator.internal.SomeModelPersistable;

/**
 * @author Arthur Korchagin on 11.07.17.
 */

public class MainActivity extends Activity {

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.ac_main);

    TestClass testClass = new TestClass();
    TestClassPersistable d = new TestClassPersistable(testClass);

    SomeModel model = new SomeModel();

    Persistable persistableModel = new SomeModelPersistable(model);
  }
}
