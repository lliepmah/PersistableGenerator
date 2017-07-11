package ru.github.lliepmah.persistablegenerator;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;

/**
 * @author Arthur Korchagin on 11.07.17.
 */

public class MainActivity extends Activity {

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.ac_main);

    TestClass testClass = new TestClass();
    TestClassPersistable d = new TestClassPersistable(testClass);

  }
}
