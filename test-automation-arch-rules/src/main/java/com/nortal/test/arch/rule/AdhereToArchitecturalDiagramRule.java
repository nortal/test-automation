package com.nortal.test.arch.rule;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.library.plantuml.PlantUmlArchCondition;

import java.net.URL;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.library.plantuml.PlantUmlArchCondition.adhereToPlantUmlDiagram;

public class AdhereToArchitecturalDiagramRule {

  static final URL diagram = AdhereToArchitecturalDiagramRule.class.getResource("archDiagram.puml");

  /**
   * This rule enforces that all class in the provided package adheres to a given architectural diagram.
   * The diagram must reside in archDiagram.puml file in resources.
   */
  public void enforceArchitecture(JavaClasses classes) {
    PlantUmlArchCondition diagramCondition =
        adhereToPlantUmlDiagram(diagram, PlantUmlArchCondition.Configurations.consideringOnlyDependenciesInDiagram());

    classes().should(diagramCondition)
        .because("Our project structure should adhere to the agreed architectural diagram that can be found in file archDiagram.puml")
        .check(classes);
  }
}
