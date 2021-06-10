package com.nortal.test.arch.rule;

import com.tngtech.archunit.core.domain.JavaClasses;
import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.implement;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideOutsideOfPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.simpleName;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

public class LimitAccessToScenarioContextRule {

	public void limitAccessToScenarioContext(JavaClasses classes) {
		noClasses()
				.that(
						resideOutsideOfPackage("com.nortal.test.mediator..")
								.and(resideOutsideOfPackage("com.nortal.test.core.rest.interceptors")
								.and(not(implement("com.nortal.test.core.services.hooks.AfterScenarioHook")))
								.and(not(implement("com.nortal.test.core.services.hooks.BeforeScenarioHook")))
								.and(not(simpleName("ScenarioContext")))
								.and(not(simpleName("ScenarioContainer")))
								.and(not(simpleName("Hooks")))
								.and(not(simpleName("ReportFormatter")))
								.and(not(simpleName("HeaderOverrideService")))
								)
						)
				.should()
				.accessClassesThat(simpleName("ScenarioContext").or(simpleName("ScenarioContainer")))
				.because("ScenarioContainer is the component that holds all the state in the scenario. We want to limit its accessibility " +
						"to avoid unexpected state changes. The architecture that we chose dictates that all changes to state should happen " +
						"at mediator layer and any service call should be stateless (should only depend on the input parameters of the method call " +
						"and not what values exist in the context). " +
						"If you need to store some state between steps, please do this in the mediator layer, e.g. " +
						"step calls mediator, mediator invokes a service call and gets a response, mediator updates the scenario context with the " +
						"data from the response.")
				.check(classes);
	}

}
