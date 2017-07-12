# PersistableGenerator

[ ![Download](https://api.bintray.com/packages/lliepmah/com.github.lliepmah/persistable-generator-compiler/images/download.svg) ](https://bintray.com/lliepmah/com.github.lliepmah/persistable-generator-compiler/_latestVersion)

A tool for generating Android Persistable implementation.

Created specially for [binaryprefs](https://github.com/iamironz/binaryprefs)


PersistableGenerator generates generates the Persistable wrapper for data classes to get rid of boilerplate source code.

All you need is to annotate the data class by the annotation `@PersistableGenerator`

## Sample

```java
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
```

Library generates wrapper class `SomeModelPersistable` implements `Persistable` and you can use it next way:

```java
 Persistable persistableModel = new SomeModelPersistable(model);
```


##Integration
```groovy
dependencies {
  ...
  provided 'com.github.lliepmah:persistable-generator-annotations:0.1.1'
  annotationProcessor 'com.github.lliepmah:persistable-generator-compiler:0.1.1'
}
```


###Kotlin
```groovy
dependencies {
  ...
  provided 'com.github.lliepmah:persistable-generator-annotations:0.1.1'
  kapt 'com.github.lliepmah:persistable-generator-compiler:0.1.1'
}
```

## ProGuard
No special ProGuard rules required.