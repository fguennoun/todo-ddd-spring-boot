package com.example.todo.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

/**
 * Tests architecturaux avec ArchUnit
 *
 * Vérifie le respect des règles d'architecture DDD :
 * - Séparation des couches
 * - Direction des dépendances
 * - Encapsulation des packages
 *
 * @author Todo Team
 */
@AnalyzeClasses(
    packages = "com.example.todo",
    importOptions = ImportOption.DoNotIncludeTests.class
)
class ArchitectureTest {

    // =================================================================
    // Tests de la structure en couches DDD
    // =================================================================

    @ArchTest
    static final ArchRule layered_architecture_should_be_respected = layeredArchitecture()
        .consideringAllDependencies()

        .layer("Presentation").definedBy("..infrastructure.rest..")
        .layer("Application").definedBy("..application..")
        .layer("Domain").definedBy("..domain..")
        .layer("Infrastructure").definedBy("..infrastructure..")

        .whereLayer("Presentation").mayNotBeAccessedByAnyLayer()
        .whereLayer("Application").mayOnlyBeAccessedByLayers("Presentation", "Infrastructure")
        .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Infrastructure")
        .whereLayer("Infrastructure").mayNotAccessAnyLayer();

    // =================================================================
    // Tests des règles du domaine
    // =================================================================

    @ArchTest
    static final ArchRule domain_should_not_depend_on_infrastructure =
        noClasses().that().resideInAPackage("..domain..")
        .should().dependOnClassesThat().resideInAPackage("..infrastructure..");

    @ArchTest
    static final ArchRule domain_should_not_depend_on_application =
        noClasses().that().resideInAPackage("..domain..")
        .should().dependOnClassesThat().resideInAPackage("..application..");

    @ArchTest
    static final ArchRule domain_should_not_depend_on_spring_framework =
        noClasses().that().resideInAPackage("..domain..")
        .should().dependOnClassesThat().resideInAPackage("org.springframework..");

    @ArchTest
    static final ArchRule domain_entities_should_be_in_model_package =
        classes().that().areAnnotatedWith("jakarta.persistence.Entity")
        .should().resideInAPackage("..infrastructure.persistence..")
        .because("JPA entities belong to infrastructure layer");

    // =================================================================
    // Tests des patterns DDD
    // =================================================================

    @ArchTest
    static final ArchRule aggregates_should_have_private_constructor_for_jpa =
        classes().that().resideInAPackage("..domain.model..")
        .and().areNotEnums()
        .and().areNotRecords()
        .and().areNotInterfaces()
        .should().notBePublic()
        .because("Domain aggregates should control their instantiation");

    @ArchTest
    static final ArchRule repositories_should_be_interfaces =
        classes().that().resideInAPackage("..domain.repository..")
        .should().beInterfaces()
        .because("Repository contracts should be defined as interfaces in domain");

    @ArchTest
    static final ArchRule value_objects_should_be_records =
        classes().that().resideInAPackage("..domain.model..")
        .and().haveSimpleNameEndingWith("Id")
        .should().beRecords()
        .because("Value Objects should be immutable records");

    // =================================================================
    // Tests des use cases
    // =================================================================

    @ArchTest
    static final ArchRule use_cases_should_be_named_consistently =
        classes().that().resideInAPackage("..application.usecase..")
        .and().haveSimpleNameEndingWith("UseCase")
        .should().bePublic()
        .because("Use cases should follow naming convention");

    @ArchTest
    static final ArchRule use_cases_should_be_annotated_with_service =
        classes().that().resideInAPackage("..application.usecase..")
        .and().haveSimpleNameEndingWith("UseCase")
        .should().beAnnotatedWith("org.springframework.stereotype.Service")
        .because("Use cases should be Spring services");

    // =================================================================
    // Tests de la couche infrastructure
    // =================================================================

    @ArchTest
    static final ArchRule controllers_should_be_in_rest_package =
        classes().that().areAnnotatedWith("org.springframework.web.bind.annotation.RestController")
        .should().resideInAPackage("..infrastructure.rest..")
        .because("REST controllers belong to infrastructure layer");

    @ArchTest
    static final ArchRule repository_implementations_should_be_in_infrastructure =
        classes().that().implement("com.example.todo.domain.repository.TodoRepository")
        .should().resideInAPackage("..infrastructure.persistence..")
        .because("Repository implementations belong to infrastructure layer");

    // =================================================================
    // Tests des annotations
    // =================================================================

    @ArchTest
    static final ArchRule services_should_be_transactional =
        classes().that().resideInAPackage("..application.usecase..")
        .and().haveSimpleNameEndingWith("UseCase")
        .should().beAnnotatedWith("org.springframework.transaction.annotation.Transactional")
        .because("Use cases should define transaction boundaries");

    @ArchTest
    static final ArchRule controllers_should_not_be_transactional =
        classes().that().areAnnotatedWith("org.springframework.web.bind.annotation.RestController")
        .should().notBeAnnotatedWith("org.springframework.transaction.annotation.Transactional")
        .because("Controllers should not manage transactions directly");

    // =================================================================
    // Tests des dépendances externes
    // =================================================================

    @ArchTest
    static final ArchRule no_junit_in_production_code =
        noClasses().that().resideOutsideOfPackage("..test..")
        .should().dependOnClassesThat().resideInAPackage("org.junit..")
        .because("JUnit should only be used in test code");

    @ArchTest
    static final ArchRule no_mockito_in_production_code =
        noClasses().that().resideOutsideOfPackage("..test..")
        .should().dependOnClassesThat().resideInAPackage("org.mockito..")
        .because("Mockito should only be used in test code");
}
