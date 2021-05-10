package com.nortal.test.arch.rule;

import com.tngtech.archunit.core.domain.JavaClasses;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.simpleNameEndingWith;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

public class StepsShouldNotCallServicesDirectly {

	public void preventDirectAccessToServices(JavaClasses classes) {
		noClasses()
				.that().resideInAPackage("com.nortal.test.glue..")
				.should()
				.accessClassesThat(resideInAPackage("com.nortal.test.services..").and(simpleNameEndingWith("Service")))
				.because("Step classes are a thin layer that provides business language mapping to mediators." +
						"They should not directly call Service classes, because any state change done by a service will not be persisted " +
						"in the scenario context. Direct calls to services should be replaced by calls to mediators, which then can immediately " +
						"call the service.")
				.check(classes);
	}

}
