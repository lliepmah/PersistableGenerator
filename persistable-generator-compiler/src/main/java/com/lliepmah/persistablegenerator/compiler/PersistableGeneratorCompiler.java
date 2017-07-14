package com.lliepmah.persistablegenerator.compiler;

import com.google.auto.service.AutoService;
import com.lliepmah.persistablegenerator.PersistableGenerator;
import com.lliepmah.persistablegenerator.compiler.entity.Property;
import com.lliepmah.persistablegenerator.compiler.exceptions.ElementException;
import com.lliepmah.persistablegenerator.compiler.names.Classes;
import com.lliepmah.persistablegenerator.compiler.names.Fields;
import com.lliepmah.persistablegenerator.compiler.names.Methods;
import com.lliepmah.persistablegenerator.compiler.names.Variables;
import com.lliepmah.persistablegenerator.compiler.utils.TextUtils;
import com.lliepmah.persistablegenerator.compiler.utils.TypeUtils;
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
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import static com.lliepmah.persistablegenerator.compiler.utils.ElementUtils.extractProperties;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

/**
 * @author Arthur Korchagin on 11.07.17.
 */

@SupportedAnnotationTypes("com.lliepmah.persistablegenerator.PersistableGenerator")
@AutoService(Processor.class) public class PersistableGeneratorCompiler extends AbstractProcessor {

  private Elements mElements;
  private Types mTypes;
  private Filer mFiler;
  private Trees mTrees;

  private List<? extends CodeBlockWriter> mWriteExternalWritersChain;
  private List<? extends CodeBlockWriter> mReadExternalWritersChain;

  @Override public synchronized void init(ProcessingEnvironment env) {
    super.init(env);
    mElements = env.getElementUtils();
    mTypes = env.getTypeUtils();
    mFiler = env.getFiler();
    try {
      mTrees = Trees.instance(processingEnv);
    } catch (IllegalArgumentException ignored) {
    }

    PrimitiveWriter primitiveWriter = new PrimitiveWriter();
    PersistableWriter persistableWriter = new PersistableWriter(mElements);
    mWriteExternalWritersChain = Arrays.asList(primitiveWriter, persistableWriter,
        new ListWriter(primitiveWriter, persistableWriter, mTypes, mElements));

    ReadPrimitiveWriter readPrimitiveWriter = new ReadPrimitiveWriter();
    ReadPersistableWriter readPersistableWriter = new ReadPersistableWriter(mElements);
    mReadExternalWritersChain = Arrays.asList(readPrimitiveWriter, readPersistableWriter,
        new ReadListWriter(readPrimitiveWriter, readPersistableWriter, mTypes, mElements,
            env.getMessager()));
  }

  @Override public boolean process(Set<? extends TypeElement> set, RoundEnvironment env) {
    for (Element element : env.getElementsAnnotatedWith(PersistableGenerator.class)) {
      try {
        brewJava((TypeElement) element).writeTo(mFiler);
      } catch (ElementException elementException) {
        error(elementException.getElement(), elementException.getMessage());
      } catch (Exception e) {
        logParsingError(element, PersistableGenerator.class, e);
      }
    }
    return true;
  }

  private JavaFile brewJava(TypeElement typeElement) throws ElementException {
    ClassName name = ClassName.get(typeElement);
    return JavaFile.builder(name.packageName(),
        createType(name, typeElement, extractProperties(mElements.getAllMembers(typeElement))))
        .addFileComment("Generated code from PersistableGenerator. Do not modify!")
        .build();
  }

  private TypeSpec createType(ClassName bindingClassName, TypeElement typeElement,
      List<Property> properties) throws ElementException {
    return TypeSpec.classBuilder(bindingClassName.simpleName() + Classes.POSTFIX_PERSISTABLE)
        .addModifiers(PUBLIC)
        .addSuperinterface(Classes.PERSISTABLE)
        .addField(ClassName.get(typeElement), Fields.MODEL, PRIVATE)
        .addMethod(createEmptyConstructor())
        .addMethod(createModelGetter(bindingClassName))
        .addMethod(createConstructor(typeElement))
        .addMethod(createWriteExternalMethod(properties))
        .addMethod(createReadExternalMethod())
        .addMethod(createBuildMethod(bindingClassName, properties))
        .addMethod(createDeepCloneMethod(bindingClassName, properties))
        .build();
  }

  private MethodSpec createEmptyConstructor() {
    MethodSpec.Builder builder = MethodSpec.constructorBuilder().addModifiers(PUBLIC);
    return builder.build();
  }

  private MethodSpec createConstructor(TypeElement typeElement) {
    return MethodSpec.constructorBuilder()
        .addModifiers(PUBLIC)
        .addParameter(ClassName.get(typeElement), Variables.MODEL)
        .addStatement("$L = $L", Fields.MODEL, Variables.MODEL)
        .build();
  }

  private MethodSpec createModelGetter(ClassName className) {
    return MethodSpec.methodBuilder(Methods.GET_MODEL)
        .returns(className)
        .addModifiers(PUBLIC)
        .addStatement("return $L", Fields.MODEL)
        .build();
  }

  private MethodSpec createWriteExternalMethod(List<Property> properties) throws ElementException {

    MethodSpec.Builder builder = MethodSpec.methodBuilder(Methods.WRITE_EXTERNAL)
        .addAnnotation(Override.class)
        .addModifiers(PUBLIC)
        .addParameter(Classes.DATA_OUTPUT, Variables.OUT);

    for (Property property : properties) {
      if (!appendWriteExternalMethod(builder, property)) {
        throw new ElementException(String.format("Can't generate code for class %1$s",
            property.getTypeMirror().toString()), property.getField());
      }
    }
    return builder.build();
  }

  /**
   * @return true if can create method
   */
  private boolean appendWriteExternalMethod(MethodSpec.Builder builder, Property property) {
    for (CodeBlockWriter codeBlockWriter : mWriteExternalWritersChain) {
      if (codeBlockWriter.write(property.getTypeMirror(), Fields.MODEL + "." + property.getName(),
          builder)) {
        return true;
      }
    }
    return false;
  }

  private MethodSpec createBuildMethod(TypeName typeName, List<Property> properties) {

    MethodSpec.Builder builder = MethodSpec.methodBuilder(Methods.BUILD)
        .addModifiers(PUBLIC, STATIC)
        .returns(typeName)
        .addParameter(Classes.DATA_INPUT, Variables.INPUT);

    List<String> variables = new LinkedList<>();
    for (Property property : properties) {
      for (CodeBlockWriter codeBlockWriter : mReadExternalWritersChain) {
        String variableName = Variables.PREFIX_VAL + properties.indexOf(property);
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
    return MethodSpec.methodBuilder(Methods.READ_EXTERNAL)
        .addAnnotation(Override.class)
        .addModifiers(PUBLIC)
        .addParameter(Classes.DATA_INPUT, Variables.INPUT)
        .addStatement("$L = $L($L)", Fields.MODEL, Methods.BUILD, Variables.INPUT)
        .build();
  }

  private MethodSpec createDeepCloneMethod(TypeName typeName, List<Property> properties) {

    MethodSpec.Builder builder = MethodSpec.methodBuilder(Methods.DEEP_CLONE)
        .addAnnotation(Override.class)
        .addModifiers(PUBLIC)
        .returns(Classes.PERSISTABLE)
        .addStatement("if($L==null) return null", Fields.MODEL);

    List<String> variables = new LinkedList<>();
    for (Property property : properties) {
      String variableName = Variables.PREFIX_VAL + properties.indexOf(property);
      variables.add(variableName);

      TypeMirror mirror = property.getTypeMirror();
      String propertyName = property.getName();

      if (TypeUtils.hasCopyMethod(property.getField(), mElements)) {
        propertyName = propertyName + "." + Methods.COPY + "()";
      }

      // TODO: 12.07.17 copy Lists and Lists with copyable

      builder.addStatement("$T $L = $L.$L", mirror, variableName, Fields.MODEL, propertyName);
    }

    TypeElement typeElement = mElements.getTypeElement(typeName.toString());
    ClassName persistableModelClassName =
        ClassName.get(mElements.getPackageOf(typeElement).toString(),
            typeElement.getSimpleName() + Classes.POSTFIX_PERSISTABLE);

    builder.addStatement("return new $T(new $T($L))", persistableModelClassName, typeName,
        TextUtils.join(",", variables));

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
