package io.protostuff.jetbrains.plugin.psi;

import static io.protostuff.compiler.parser.ProtoParser.RULE_reservedFieldName;
import static io.protostuff.compiler.parser.Util.trimStringName;
import static io.protostuff.jetbrains.plugin.ProtoParserDefinition.rule;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import java.util.ArrayList;
import java.util.List;
import org.antlr.jetbrains.adapter.psi.AntlrPsiNode;
import org.jetbrains.annotations.NotNull;

/**
 * Reserved field names node.
 *
 * @author Kostiantyn Shchepanovskyi
 */
public class ReservedFieldNamesNode extends AntlrPsiNode
        implements AntlrParserRuleNode, KeywordsContainer {

    private Boolean syntaxErrors;

    public ReservedFieldNamesNode(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public boolean hasSyntaxErrors() {
        if (syntaxErrors == null) {
            syntaxErrors = Util.checkForSyntaxErrors(this);
        }
        return syntaxErrors;
    }

    /**
     * Get reserved field names.
     */
    public List<String> getNames() {
        List<PsiElement> nodes = findChildrenByType(rule(RULE_reservedFieldName));
        List<String> result = new ArrayList<>();
        for (PsiElement node : nodes) {
            String s = trimStringName(node.getText());
            result.add(s);
        }
        return result;
    }

}
