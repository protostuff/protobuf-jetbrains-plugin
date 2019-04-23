package io.protostuff.jetbrains.plugin;

import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import io.protostuff.compiler.parser.ProtoLexer;
import org.antlr.jetbrains.adapter.lexer.AntlrLexerAdapter;
import org.antlr.jetbrains.adapter.lexer.TokenIElementType;
import org.jetbrains.annotations.NotNull;

/**
 * A highlighter is really just a mapping from token type to
 * some text attributes using {@link #getTokenHighlights(IElementType)}.
 * The reason that it returns an array, TextAttributesKey[], is
 * that you might want to mix the attributes of a few known highlighters.
 * A {@link TextAttributesKey} is just a name for that a theme
 * or IDE skin can set. For example, {@link DefaultLanguageHighlighterColors#KEYWORD}
 * is the key that maps to what identifiers look like in the editor.
 * To change it, see dialog: Editor > Colors & Fonts > Language Defaults.
 * <p>
 * From <a href="http://www.jetbrains.org/intellij/sdk/docs/reference_guide/custom_language_support/syntax_highlighting_and_error_highlighting.html">doc</a>:
 * "The mapping of the TextAttributesKey to specific attributes used
 * in an editor is defined by the EditorColorsScheme class, and can
 * be configured by the user if the plugin provides an appropriate
 * configuration interface.
 * ...
 * The syntax highlighter returns the {@link TextAttributesKey}
 * instances for each token type which needs special highlighting.
 * For highlighting lexer errors, the standard TextAttributesKey
 * for bad characters HighlighterColors.BAD_CHARACTER can be used."
 */
public class ProtoSyntaxHighlighter extends SyntaxHighlighterBase {

    static final TextAttributesKey KEYWORD =
            createTextAttributesKey("PROTO_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD);
    static final TextAttributesKey STRING_NAME =
            createTextAttributesKey("PROTO_STRING_NAME", DefaultLanguageHighlighterColors.STRING);
    static final TextAttributesKey STRING_VALUE =
            createTextAttributesKey("PROTO_STRING_VALUE", DefaultLanguageHighlighterColors.STRING);
    static final TextAttributesKey NUMBER =
            createTextAttributesKey("PROTO_NUMBER", DefaultLanguageHighlighterColors.NUMBER);
    static final TextAttributesKey LINE_COMMENT =
            createTextAttributesKey("PROTO_LINE_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT);
    static final TextAttributesKey BLOCK_COMMENT =
            createTextAttributesKey("PROTO_BLOCK_COMMENT", DefaultLanguageHighlighterColors.BLOCK_COMMENT);
    static final TextAttributesKey ENUM_CONSTANT =
            createTextAttributesKey("PROTO_ENUM_CONSTANT", DefaultLanguageHighlighterColors.CONSTANT);
    static final TextAttributesKey METADATA =
            createTextAttributesKey("PROTO_METADATA", DefaultLanguageHighlighterColors.METADATA);

    private static final TextAttributesKey[] EMPTY_KEYS = new TextAttributesKey[0];

    @NotNull
    @Override
    public Lexer getHighlightingLexer() {
        ProtoLexer lexer = new ProtoLexer(null);
        return new AntlrLexerAdapter(ProtoLanguage.INSTANCE, lexer, ProtoParserDefinition.ELEMENT_FACTORY);
    }

    @NotNull
    @Override
    public TextAttributesKey[] getTokenHighlights(IElementType tokenType) {
        if (!(tokenType instanceof TokenIElementType)) {
            return EMPTY_KEYS;
        }
        TokenIElementType myType = (TokenIElementType) tokenType;
        int antlrTokenType = myType.getAntlrTokenType();
        TextAttributesKey attrKey;
        switch (antlrTokenType) {
            case ProtoLexer.INTEGER_VALUE:
            case ProtoLexer.FLOAT_VALUE:
                attrKey = NUMBER;
                break;
            case ProtoLexer.STRING_NAME:
                attrKey = STRING_NAME;
                break;
            case ProtoLexer.STRING_VALUE:
                attrKey = STRING_VALUE;
                break;
            case ProtoLexer.COMMENT:
                attrKey = BLOCK_COMMENT;
                break;
            case ProtoLexer.LINE_COMMENT:
                attrKey = LINE_COMMENT;
                break;
            case ProtoLexer.PLUGIN_DEV_MARKER:
                attrKey = METADATA;
                break;
            default:
                return EMPTY_KEYS;
        }
        return new TextAttributesKey[]{attrKey};
    }
}
