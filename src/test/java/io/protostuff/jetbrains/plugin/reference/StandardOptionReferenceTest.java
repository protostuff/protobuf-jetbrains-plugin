package io.protostuff.jetbrains.plugin.reference;

import com.intellij.psi.PsiElement;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import io.protostuff.jetbrains.plugin.psi.DataType;
import io.protostuff.jetbrains.plugin.psi.FieldNode;
import io.protostuff.jetbrains.plugin.psi.ProtoPsiFileRoot;
import org.junit.Assert;

/**
 * Tests for resolving standard options.
 *
 * @author Kostiantyn Shchepanovskyi
 */
public class StandardOptionReferenceTest extends LightCodeInsightFixtureTestCase {

    @Override
    protected String getTestDataPath() {
        return "src/test/resources/reference/options";
    }

    public void testStandardMessageOptionReference() {
        checkReferenceToField(".google.protobuf.MessageOptions", "deprecated",
                "StandardMessageOptionReferenceTestData.proto");
    }

    public void testStandardFieldOptionReference() {
        checkReferenceToField(".google.protobuf.FieldOptions", "deprecated",
                "StandardFieldOptionReferenceTestData.proto");
    }

    public void testStandardFieldOptionJsonNameReference() {
        checkReferenceToField(".google.protobuf.FieldOptions", "json_name",
                "StandardFieldOptionJsonNameReferenceTestData.proto");
    }

    public void testStandardMapFieldOptionReference() {
        checkReferenceToField(".google.protobuf.FieldOptions", "deprecated",
                "StandardMapFieldOptionReferenceTestData.proto");
    }

    public void testStandardEnumOptionReference() {
        checkReferenceToField(".google.protobuf.EnumOptions", "deprecated",
                "StandardEnumOptionReferenceTestData.proto");
    }

    public void testStandardEnumConstantOptionReference() {
        checkReferenceToField(".google.protobuf.EnumValueOptions", "deprecated",
                "StandardEnumConstantOptionReferenceTestData.proto");
    }

    public void testStandardServiceOptionReference() {
        checkReferenceToField(".google.protobuf.ServiceOptions", "deprecated",
                "StandardServiceOptionReferenceTestData.proto");
    }

    public void testStandardServiceMethodOptionReference() {
        checkReferenceToField(".google.protobuf.MethodOptions", "deprecated",
                "StandardServiceMethodOptionReferenceTestData.proto");
    }

    public void testStandardFileOptionReference() {
        checkReferenceToField(".google.protobuf.FileOptions", "deprecated",
                "StandardFileOptionReferenceTestData.proto");
    }

    private void checkReferenceToField(String typeReference, String fieldName, String... file) {
        myFixture.configureByFiles(file);
        ProtoPsiFileRoot proto = (ProtoPsiFileRoot) myFixture.getFile();
        PsiElement elementAtCaret = proto
                .findElementAt(myFixture.getCaretOffset());
        while (elementAtCaret != null
                && elementAtCaret.getReferences().length == 0) {
            elementAtCaret = elementAtCaret.getParent();
        }
        assertNotNull(elementAtCaret);
        PsiElement target = elementAtCaret.getReferences()[0].resolve();
        Assert.assertTrue(target instanceof FieldNode);
        FieldNode field = (FieldNode) target;
        Assert.assertEquals(fieldName, field.getFieldName());
        Assert.assertEquals(typeReference, ((DataType) field.getParent()).getQualifiedName());
    }
}
