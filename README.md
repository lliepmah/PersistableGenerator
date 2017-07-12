# PersistableGenerator

[ ![Download](https://api.bintray.com/packages/lliepmah/com.github.lliepmah/persistable-generator-compiler/images/download.svg) ](https://bintray.com/lliepmah/com.github.lliepmah/persistable-generator-compiler/_latestVersion)

A tool for generating [Persistable](https://github.com/iamironz/binaryprefs/blob/master/library/src/main/java/com/ironz/binaryprefs/serialization/serializer/persistable/Persistable.java) implementation.

Created specially for [Binaryprefs](https://github.com/iamironz/binaryprefs)


PersistableGenerator generates the Persistable wrapper for data classes to get rid of boilerplate source code.

All you need is to annotate the data class by the annotation `@PersistableGenerator`

## Sample


#### Java

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

#### Kotlin

```kotlin
@PersistableGenerator
data class SomeModel(val name: String, val age: Int, val someList: List<OtherModel>, val someInts: List<Int>, val someStrings: List<String>)
```

Library generates wrapper class `SomeModelPersistable` implements `Persistable` and you can use it next way:

```java
 Persistable persistableModel = new SomeModelPersistable(model);
```


## Integration

```groovy
def persistableGeneratorVersion = 'x.x.x'
...
dependencies {
  provided "com.github.lliepmah:persistable-generator-annotations:$persistableGeneratorVersion"
  annotationProcessor "com.github.lliepmah:persistable-generator-compiler:$persistableGeneratorVersion"
  ...
}
```


### Kotlin

```groovy
def persistableGeneratorVersion = 'x.x.x'

dependencies {
  provided "com.github.lliepmah:persistable-generator-annotations:$persistableGeneratorVersion"
  kapt "com.github.lliepmah:persistable-generator-compiler:$persistableGeneratorVersion"
  ...
}
```

## ProGuard
No special ProGuard rules required.