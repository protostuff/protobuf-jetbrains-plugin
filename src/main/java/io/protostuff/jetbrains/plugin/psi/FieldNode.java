package io.protostuff.jetbrains.plugin.psi;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import io.protostuff.compiler.parser.ProtoParser;
import io.protostuff.jetbrains.plugin.ProtoParserDefinition;
import org.antlr.jetbrains.adapter.psi.IdentifierDefSubtree;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;

import static io.protostuff.jetbrains.plugin.ProtoParserDefinition.*;
import static io.protostuff.jetbrains.plugin.psi.Util.decodeIntegerFromText;

/**
 * @author Kostiantyn Shchepanovskyi
 */
public class FieldNode extends IdentifierDefSubtree implements KeywordsContainer, MessageField {

    private static final Logger LOGGER = Logger.getInstance(FieldNode.class);

    public FieldNode(@NotNull ASTNode node) {
        super(node, ProtoParserDefinition.rule(ProtoParser.RULE_fieldName));
    }

    @Override
    public Collection<PsiElement> keywords() {
        ASTNode node = getNode();
        ASTNode fieldModifier = node.findChildByType(R_FIELD_MODIFIER);
        if (fieldModifier != null) {
            return Util.findKeywords(fieldModifier);
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public String getFieldName() {
        ASTNode nameNode = getFieldNameNode();
        if (nameNode != null) {
            return nameNode.getText();
        }
        return "";
    }

    @Override
    public ASTNode getFieldNameNode() {
        ASTNode node = getNode();
        return node.findChildByType(R_FIELD_NAME);
    }

    @Override
    public int getTag() {
        ASTNode tagNode = getTagNode();
        return decodeIntegerFromText(tagNode);
    }

    @Override
    public ASTNode getTagNode() {
        ASTNode node = getNode();
        return node.findChildByType(R_TAG);
    }

    @Override
    public String toString() {
        return "FieldNode(" + getFieldName() + "=" + getTag() + ")";
    }
}
