package com.drobnapomoc.drobnapomoc;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.GeneralCodingRules;

import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

@AnalyzeClasses(
        packages = "com.drobnapomoc.drobnapomoc",
        importOptions = ImportOption.DoNotIncludeTests.class
)
class ArchitectureTest {

    @ArchTest
    static final ArchRule domainShouldBeFrameworkFreeAndSelfContained =
            classes().that().resideInAPackage("..domain..")
                    .should().onlyDependOnClassesThat().resideInAnyPackage(
                            "..domain..",
                            "java..",
                            "org.jspecify.."
                    )
                    .because("Domain layer must stay pure Java without framework or outer-layer dependencies");

    @ArchTest
    static final ArchRule applicationShouldNotDependOnAdaptersOrInfrastructureFrameworks =
            noClasses().that().resideInAPackage("..application..")
                    .should().dependOnClassesThat(resideInAnyPackage(
                            "..adapter..",
                            "..shared..",
                            "..api..",
                            "org.springframework.web..",
                            "org.springframework.security..",
                            "org.springframework.jdbc..",
                            "jakarta.servlet.."
                    ).and(not(resideInAPackage("..shared.domain.."))))
                    .because("Application layer must remain isolated from adapters, OpenAPI models, and web/security/JDBC frameworks; "
                            + "only the shared domain kernel (e.g. AccountRole) is allowed");

    @ArchTest
    static final ArchRule inboundAdaptersShouldNotDependOnOutboundAdapters =
            noClasses().that().resideInAPackage("..adapter.in..")
                    .should().dependOnClassesThat().resideInAPackage("..adapter.out..")
                    .because("Driving adapters must communicate through application ports, never outbound adapters");

    @ArchTest
    static final ArchRule outboundAdaptersShouldNotDependOnInboundAdapters =
            noClasses().that().resideInAPackage("..adapter.out..")
                    .should().dependOnClassesThat().resideInAPackage("..adapter.in..")
                    .because("Driven adapters must not depend on driving adapters");

    @ArchTest
    static final ArchRule adaptersShouldNotDependOnApplicationServices =
            noClasses().that().resideInAPackage("..adapter..")
                    .should().dependOnClassesThat().resideInAPackage("..application.service..")
                    .because("Adapters must depend on inbound/outbound port interfaces rather than service implementations");

    @ArchTest
    static final ArchRule sharedShouldNotDependOnBusinessModules =
            classes().that().resideInAPackage("..shared..")
                    .should().onlyDependOnClassesThat().resideInAnyPackage(
                            "..shared..",
                            "..api..",
                            "java..",
                            "jakarta..",
                            "org.springframework..",
                            "org.jspecify..",
                            "org.slf4j..",
                            "lombok.."
                    )
                    .because("Shared kernel must never depend on business modules");

    @ArchTest
    static final ArchRule topLevelModulesShouldBeFreeOfCycles =
            slices().matching("com.drobnapomoc.drobnapomoc.(*)..")
                    .should().beFreeOfCycles();

    @ArchTest
    static final ArchRule productionCodeShouldNotUseFieldInjection =
            noFields().that().areDeclaredInClassesThat().resideOutsideOfPackage("..api..")
                    .should(GeneralCodingRules.BE_ANNOTATED_WITH_AN_INJECTION_ANNOTATION)
                    .because("Constructor injection should be used instead of field injection");

    @ArchTest
    static final ArchRule productionCodeShouldNotUseStandardStreams =
            noClasses().that().resideOutsideOfPackage("..api..")
                    .should(GeneralCodingRules.ACCESS_STANDARD_STREAMS);
}
