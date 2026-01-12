package org.apache.lucene;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaMember;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import java.util.Set;
import org.apache.lucene.index.FilterLeafReader;
import org.apache.lucene.tests.util.LuceneTestCase;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public class TestDesignConstraints extends LuceneTestCase {
  private static JavaClasses classes;

  @BeforeClass
  public static void readClasses() {
    classes = new ClassFileImporter().importPackages("org.apache.lucene");
  }

  @AfterClass
  public static void cleanup() {
    classes = null;
  }

  public void testFilterClasses() {
    ArchRuleDefinition.classes()
        .that()
        .areAssignableTo(FilterLeafReader.FilterPostingsEnum.class)
        .and()
        .areNotInterfaces()
        .should(overrideMethodsConsistently("intoBitSet", "nextDoc"))
        .check(classes);
  }

  private ArchCondition<? super JavaClass> overrideMethodsConsistently(String... methodNames) {
    Set<String> methodNameSet = Set.of(methodNames);
    return new ArchCondition<>("override methods consistently: " + methodNameSet) {
      @Override
      public void check(JavaClass javaClass, ConditionEvents events) {
        var overrides =
            javaClass.getMethods().stream()
                .filter(method -> methodNameSet.contains(method.getName()))
                .toList();

        if (overrides.size() != 2 && !overrides.isEmpty()) {
          String message =
              String.format(
                  "Class %s %s must override the following methods consistently: %s but only overrides: %s",
                  javaClass.getName(),
                  javaClass.getSourceCodeLocation(),
                  methodNameSet,
                  overrides.stream().map(JavaMember::getName).toList());
          events.add(SimpleConditionEvent.violated(javaClass, message));
        }
      }
    };
  }
}
