package com.lliepmah.persistablegenerator.compiler;

import com.google.auto.service.AutoService;
import com.lliepmah.persistablegenerator.PersistableGenerator;
import com.lliepmah.persistablegenerator.compiler.utils.ElementUtils;
import com.lliepmah.persistablegenerator.compiler.utils.TextUtils;
import com.lliepmah.persistablegenerator.compiler.writers.CodeBlockWriter;
import com.lliepmah.persistablegenerator.compiler.writers.ListWriter;
import com.lliepmah.persistablegenerator.compiler.writers.PersistableWriter;
import com.lliepmah.persistablegenerator.compiler.writers.PrimitiveWriter;
import com.lliepmah.persistablegenerator.compiler.writers.ReadListWriter;
import com.lliepmah.persistablegenerator.compiler.writers.ReadPersistableWriter;
import com.lliepmah.persistablegenerator.compiler.writers.ReadPrimitiveWriter;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.sun.source.util.Trees;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

/**
 * @author Arthur Korchagin on 11.07.17.
 */

@SupportedAnnotationTypes("com.lliepmah.persistablegenerator.PersistableGenerator")
@AutoService(Processor.class) public class PersistableGeneratorCompiler extends AbstractProcessor {

  public static final ClassName LIST_CLASSNAME = ClassName.get(List.class);
  public static final ClassName STRING_CLASSNAME = ClassName.get("java.lang", "String");
  public static final ClassName DATA_OUTPUT_CLASSNAME =
      ClassName.get("com.ironz.binaryprefs.serialization.serializer.persistable.io", "DataOutput");
  public static final ClassName DATA_INPUT_CLASSNAME =
      ClassName.get("com.ironz.binaryprefs.serialization.serializer.persistable.io", "DataInput");
  public static final ClassName PERSISTABLE_CLASSNAME =
      ClassName.get("com.ironz.binaryprefs.serialization.serializer.persistable", "Persistable");

  //public static final ClassName PERSISTABLE_MODEL_CLASSNAME =
  //    ClassName.get("com.lliepmah.persistablegenerator", "PersistableModel");

  public static final String POSTFIX_PERSISTABLE = "Persistable";
  public static final String FIELD_NAME_MODEL = "mModel";

  private Elements elementUtils;
  private Types typeUtils;
  private Filer filer;
  private Trees trees;

  private List<? extends CodeBlockWriter> mWriteExternalWritersChain;
  private List<? extends CodeBlockWriter> mReadExternalWritersChain;

  @Override public synchronized void init(ProcessingEnvironment env) {
    super.init(env);
    elementUtils = env.getElementUtils();
    typeUtils = env.getTypeUtils();
    filer = env.getFiler();
    try {
      trees = Trees.instance(processingEnv);
    } catch (IllegalArgumentException ignored) {
    }

    PrimitiveWriter primitiveWriter = new PrimitiveWriter();
    PersistableWriter persistableWriter = new PersistableWriter(elementUtils);
    mWriteExternalWritersChain = Arrays.asList(primitiveWriter, persistableWriter,
        new ListWriter(primitiveWriter, persistableWriter, typeUtils, elementUtils));

    ReadPrimitiveWriter readPrimitiveWriter = new ReadPrimitiveWriter();
    ReadPersistableWriter readPersistableWriter = new ReadPersistableWriter(elementUtils);
    mReadExternalWritersChain = Arrays.asList(readPrimitiveWriter, readPersistableWriter,
        new ReadListWriter(readPrimitiveWriter, readPersistableWriter, typeUtils, elementUtils, env.getMessager()));
  }

  @Override public boolean process(Set<? extends TypeElement> set, RoundEnvironment env) {
    for (Element element : env.getElementsAnnotatedWith(PersistableGenerator.class)) {

      try {
        JavaFile javaFile = brewJava((TypeElement) element);
        javaFile.writeTo(filer);
      } catch (Exception e) {
        logParsingError(element, PersistableGenerator.class, e);
      }
    }
    return true;
  }

  private JavaFile brewJava(TypeElement typeElement) {

    ClassName name = ClassName.get(typeElement);

    return JavaFile.builder(name.packageName(), createType(name, typeElement))
        .addFileComment("Generated code from PersistableGenerator. Do not modify!")
        .build();
  }

  private TypeSpec createType(ClassName bindingClassName, TypeElement typeElement) {

    TypeSpec.Builder result =
        TypeSpec.classBuilder(bindingClassName.simpleName() + POSTFIX_PERSISTABLE)
            .addModifiers(PUBLIC);

    result.addSuperinterface(PERSISTABLE_CLASSNAME);
    result.addField(ClassName.get(typeElement), FIELD_NAME_MODEL, PRIVATE);

    List<? extends Element> members = elementUtils.getAllMembers(typeElement);

    final List<VariableElement> fieldElements = new ArrayList<>();
    final List<ExecutableElement> methodElements = new ArrayList<>();

    for (Element member : members) {
      if (member instanceof VariableElement) {
        fieldElements.add((VariableElement) member);
      } else if (member.getKind() == ElementKind.METHOD) {
        methodElements.add((ExecutableElement) member);
      }
    }

    List<Property> properties = new LinkedList<Property>() {{

      for (VariableElement field : fieldElements) {
        if (!ElementUtils.isStatic(field)) {
          String propertyName = field.getSimpleName().toString();
          TypeMirror type = field.asType();

          if (ElementUtils.isPrivate(field)) {
            propertyName = ElementUtils.findGetter(propertyName, type, methodElements);
          }

          if (propertyName != null) {
            add(new Property(propertyName, type));
          }
        }
      }
    }};

    result.addMethod(createEmptyConstructor());
    result.addMethod(createConstructor(typeElement, fieldElements));
    result.addMethod(createWriteExternalMethod(properties));
    result.addMethod(createReadExternalMethod());
    result.addMethod(createBuildMethod(bindingClassName, properties));
    result.addMethod(createDeepCloneMethod(fieldElements));

    return result.build();
  }

  private MethodSpec createEmptyConstructor() {
    MethodSpec.Builder builder = MethodSpec.constructorBuilder().addModifiers(PUBLIC);
    return builder.build();
  }

  private MethodSpec createConstructor(TypeElement typeElement, List<VariableElement> fields) {

    MethodSpec.Builder builder = MethodSpec.constructorBuilder()
        .addModifiers(PUBLIC)
        .addParameter(ClassName.get(typeElement), "model");

    builder.addStatement("mModel = model");

    return builder.build();
  }

  private MethodSpec createWriteExternalMethod(List<Property> properties) {

    MethodSpec.Builder builder = MethodSpec.methodBuilder("writeExternal")
        .addAnnotation(Override.class)
        .addModifiers(PUBLIC)
        .addParameter(DATA_OUTPUT_CLASSNAME, "out");

    for (Property property : properties) {
      for (CodeBlockWriter codeBlockWriter : mWriteExternalWritersChain) {
        if (codeBlockWriter.write(property.getTypeMirror(),
            FIELD_NAME_MODEL + "." + property.getName(), builder)) {
          break;
        }
      }
    }

    return builder.build();
  }

  private MethodSpec createBuildMethod(TypeName typeName, List<Property> properties) {

    MethodSpec.Builder builder = MethodSpec.methodBuilder("build")
        .addModifiers(PUBLIC, STATIC)
        .returns(typeName)
        .addParameter(DATA_INPUT_CLASSNAME, "input");

    List<String> variables = new LinkedList<>();
    for (Property property : properties) {
      for (CodeBlockWriter codeBlockWriter : mReadExternalWritersChain) {
        String variableName = "val_" + properties.indexOf(property);

        if (codeBlockWriter.write(property.getTypeMirror(), variableName, builder)) {
          variables.add(variableName);
          break;
        }
      }
    }

    builder.addStatement("return new $T(" + TextUtils.join(",", variables) + ")", typeName);
    return builder.build();
  }

  private MethodSpec createReadExternalMethod() {

    return MethodSpec.methodBuilder("readExternal")
        .addAnnotation(Override.class)
        .addModifiers(PUBLIC)
        .addParameter(DATA_INPUT_CLASSNAME, "input")
        .addStatement("mModel = build(input)")
        .build();
  }

  private MethodSpec createDeepCloneMethod(List<VariableElement> fields) {

    MethodSpec.Builder builder = MethodSpec.methodBuilder("deepClone")
        .addAnnotation(Override.class)
        .addModifiers(PUBLIC)
        .returns(PERSISTABLE_CLASSNAME);

    for (VariableElement var : fields) {
      builder.addStatement("String name"
          + var.getSimpleName().toString()
          + " = \""
          + var.getSimpleName().toString()
          + "\"");
    }
    builder.addStatement("return null");

    return builder.build();
  }

  @Override public Set<String> getSupportedAnnotationTypes() {
    Set<String> types = new LinkedHashSet<>();
    for (Class<? extends Annotation> annotation : getSupportedAnnotations()) {
      types.add(annotation.getCanonicalName());
    }
    return types;
  }

  private Set<Class<? extends Annotation>> getSupportedAnnotations() {
    Set<Class<? extends Annotation>> annotations = new LinkedHashSet<>();
    annotations.add(PersistableGenerator.class);
    return annotations;
  }

  private void logParsingError(Element element, Class<? extends Annotation> annotation,
      Exception e) {
    StringWriter stackTrace = new StringWriter();
    e.printStackTrace(new PrintWriter(stackTrace));
    error(element, "Unable to parse @%s binding.\n\n%s", annotation.getSimpleName(), stackTrace);
  }

  private void error(Element element, String message, Object... args) {
    printMessage(Diagnostic.Kind.ERROR, element, message, args);
  }

  private void note(Element element, String message, Object... args) {
    printMessage(Diagnostic.Kind.NOTE, element, message, args);
  }

  private void printMessage(Diagnostic.Kind kind, Element element, String message, Object[] args) {
    if (args.length > 0) {
      message = String.format(message, args);
    }
    processingEnv.getMessager().printMessage(kind, message, element);
  }
}
