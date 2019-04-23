package io.protostuff.jetbrains.plugin.psi;

import static io.protostuff.compiler.parser.Util.trimStringName;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import io.protostuff.compiler.parser.ProtoParser;
import io.protostuff.jetbrains.plugin.ProtoParserDefinition;
import org.antlr.jetbrains.adapter.psi.AntlrPsiNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Syntax node.
 *
 * @author Kostiantyn Shchepanovskyi
 */
public class SyntaxStatement extends AntlrPsiNode implements KeywordsContainer {

    public SyntaxStatement(@NotNull ASTNode node) {
        super(node);
    }

    @Nullable
    String getSyntaxName() {
        PsiElement syntaxNameNode = findChildByType(ProtoParserDefinition.rule(ProtoParser.RULE_syntaxName));
        if (syntaxNameNode != null) {
            String text = syntaxNameNode.getText();
            if (text.length() > 2
                    && ((text.startsWith("\"") && text.endsWith("\""))
                        || (text.startsWith("'") && text.endsWith("'"))
                        || (text.startsWith("`") && text.endsWith("`")))) {
                String value = trimStringName(text);
                return value;
            }
            return text;
        }
        return null;
    }

}
