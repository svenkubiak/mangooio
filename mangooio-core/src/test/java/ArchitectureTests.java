import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import org.junit.jupiter.api.Test;

public class ArchitectureTests {
    private static final JavaClasses CLASSES = new ClassFileImporter().importPackages("io.mangoo");

    @Test
    void testEnums() {
        //given
        ArchRule archRule = ArchRuleDefinition.classes()
                .that().areEnums()
                .should().resideInAPackage("..enums..");

        //then
        archRule.check(CLASSES);
    }

    @Test
    void testUtils() {
        //given
        ArchRule archRule = ArchRuleDefinition.classes()
                .that().haveSimpleNameContaining("Utils")
                .should().resideInAPackage("..utils..");

        //then
        archRule.check(CLASSES);
    }

    @Test
    void testFilters() {
        //given
        ArchRule archRule = ArchRuleDefinition.classes()
                .that().haveSimpleNameEndingWith("Filter")
                .should().resideInAPackage("..filters..");

        //then
        archRule.check(CLASSES);
    }

    @Test
    void testHandlers() {
        //given
        ArchRule archRule = ArchRuleDefinition.classes()
                .that().haveSimpleNameEndingWith("Handler")
                .should().resideInAPackage("..handlers..");

        //then
        archRule.check(CLASSES);
    }

    @Test
    void testAnnotations() {
        //given
        ArchRule archRule = ArchRuleDefinition.classes()
                .that().areAnnotations()
                .should().resideInAPackage("..annotations..");

        //then
        archRule.check(CLASSES);
    }

    @Test
    void testExceptions() {
        //given
        ArchRule archRule = ArchRuleDefinition.classes()
                .that().haveSimpleNameEndingWith("Exception")
                .should().resideInAPackage("..exceptions..");

        //then
        archRule.check(CLASSES);
    }
}
