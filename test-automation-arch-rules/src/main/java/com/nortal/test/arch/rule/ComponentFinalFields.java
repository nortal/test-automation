package com.nortal.test.arch.rule;

import com.tngtech.archunit.core.domain.JavaClasses;

import static com.tngtech.archunit.core.domain.properties.CanBeAnnotated.Predicates.annotatedWith;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

public class ComponentFinalFields {

	public void preventNonFinalClassFields(JavaClasses classes) {
		classes()
				.that().resideInAnyPackage("com.nortal.test.mediator..", "com.nortal.test.services..")
				.and(
						annotatedWith("org.springframework.stereotype.Component")
								.or(annotatedWith("org.springframework.stereotype.Service"))
				)
				.should()
				.haveOnlyFinalFields()
				.because("Non-final class fields mean that someone will try to change their value at some points which we want to avoid " +
						"as that means we are keeping changing state in our mediators/services, " +
						"also test run in parallel which means one test changing things will corrupt it for others. " +
						"If you need to have some mutable state please put it in ScenarionContext class " +
						"which is thread local and properly separated.")
				.check(classes);
	}

}
