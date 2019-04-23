package io.protostuff.jetbrains.plugin.spellchecker;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.spellchecker.inspections.PlainTextSplitter;
import com.intellij.spellchecker.tokenizer.SpellcheckingStrategy;
import com.intellij.spellchecker.tokenizer.TokenConsumer;
import com.intellij.spellchecker.tokenizer.Tokenizer;
import io.protostuff.compiler.parser.ProtoLexer;
import io.protostuff.jetbrains.plugin.ProtoParserDefinition;
import org.antlr.jetbrains.adapter.lexer.TokenIElementType;
import org.jetbrains.annotations.NotNull;

/**
 * Spellchecker support.
 *
 * @author Kostiantyn Shchepanovskyi
 */
public class ProtoSpellcheckingStrategy extends SpellcheckingStrategy {

    private static final TokenIElementType STRING_NAME_TOKEN = ProtoParserDefinition.token(ProtoLexer.STRING_NAME);
    private static final TokenIElementType STRING_VALUE_TOKEN = ProtoParserDefinition.token(ProtoLexer.STRING_VALUE);
    private static final Tokenizer<PsiElement> STRING_VALUE_TOKENIZER = new Tokenizer<PsiElement>() {
        @Override
        public void tokenize(@NotNull PsiElement element, TokenConsumer consumer) {
            consumer.consumeToken(element, PlainTextSplitter.getInstance());
        }
    };

    @NotNull
    @Override
    public Tokenizer getTokenizer(PsiElement element) {
        ASTNode node = element.getNode();
        if (node != null && (node.getElementType() == STRING_NAME_TOKEN
                             || node.getElementType() == STRING_VALUE_TOKEN)) {
            return STRING_VALUE_TOKENIZER;
        }
        return super.getTokenizer(element);
    }
}
