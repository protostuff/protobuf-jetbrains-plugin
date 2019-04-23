package io.protostuff.jetbrains.plugin;

import static io.protostuff.compiler.parser.ProtoLexer.COMMENT;
import static io.protostuff.compiler.parser.ProtoLexer.LINE_COMMENT;
import static io.protostuff.compiler.parser.ProtoLexer.NL;
import static io.protostuff.compiler.parser.ProtoLexer.PLUGIN_DEV_MARKER;
import static io.protostuff.compiler.parser.ProtoLexer.STRING_VALUE;
import static io.protostuff.compiler.parser.ProtoLexer.WS;
import static io.protostuff.compiler.parser.ProtoParser.RULE_customFieldReference;
import static io.protostuff.compiler.parser.ProtoParser.RULE_enumBlock;
import static io.protostuff.compiler.parser.ProtoParser.RULE_enumField;
import static io.protostuff.compiler.parser.ProtoParser.RULE_enumFieldName;
import static io.protostuff.compiler.parser.ProtoParser.RULE_enumName;
import static io.protostuff.compiler.parser.ProtoParser.RULE_extendBlock;
import static io.protostuff.compiler.parser.ProtoParser.RULE_extendBlockEntry;
import static io.protostuff.compiler.parser.ProtoParser.RULE_extensions;
import static io.protostuff.compiler.parser.ProtoParser.RULE_field;
import static io.protostuff.compiler.parser.ProtoParser.RULE_fieldModifier;
import static io.protostuff.compiler.parser.ProtoParser.RULE_fieldName;
import static io.protostuff.compiler.parser.ProtoParser.RULE_fieldRerefence;
import static io.protostuff.compiler.parser.ProtoParser.RULE_fileReference;
import static io.protostuff.compiler.parser.ProtoParser.RULE_groupBlock;
import static io.protostuff.compiler.parser.ProtoParser.RULE_groupName;
import static io.protostuff.compiler.parser.ProtoParser.RULE_ident;
import static io.protostuff.compiler.parser.ProtoParser.RULE_importStatement;
import static io.protostuff.compiler.parser.ProtoParser.RULE_map;
import static io.protostuff.compiler.parser.ProtoParser.RULE_mapKey;
import static io.protostuff.compiler.parser.ProtoParser.RULE_messageBlock;
import static io.protostuff.compiler.parser.ProtoParser.RULE_messageName;
import static io.protostuff.compiler.parser.ProtoParser.RULE_oneof;
import static io.protostuff.compiler.parser.ProtoParser.RULE_oneofName;
import static io.protostuff.compiler.parser.ProtoParser.RULE_option;
import static io.protostuff.compiler.parser.ProtoParser.RULE_optionEntry;
import static io.protostuff.compiler.parser.ProtoParser.RULE_optionValue;
import static io.protostuff.compiler.parser.ProtoParser.RULE_packageStatement;
import static io.protostuff.compiler.parser.ProtoParser.RULE_proto;
import static io.protostuff.compiler.parser.ProtoParser.RULE_range;
import static io.protostuff.compiler.parser.ProtoParser.RULE_reservedFieldNames;
import static io.protostuff.compiler.parser.ProtoParser.RULE_reservedFieldRanges;
import static io.protostuff.compiler.parser.ProtoParser.RULE_rpcMethod;
import static io.protostuff.compiler.parser.ProtoParser.RULE_rpcName;
import static io.protostuff.compiler.parser.ProtoParser.RULE_rpcType;
import static io.protostuff.compiler.parser.ProtoParser.RULE_serviceBlock;
import static io.protostuff.compiler.parser.ProtoParser.RULE_serviceName;
import static io.protostuff.compiler.parser.ProtoParser.RULE_standardFieldRerefence;
import static io.protostuff.compiler.parser.ProtoParser.RULE_syntaxStatement;
import static io.protostuff.compiler.parser.ProtoParser.RULE_tag;
import static io.protostuff.compiler.parser.ProtoParser.RULE_typeReference;
import static io.protostuff.compiler.parser.ProtoParser.STRING_NAME;
import static io.protostuff.compiler.parser.ProtoParser.ruleNames;

import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.IStubFileElementType;
import com.intellij.psi.tree.TokenSet;
import io.protostuff.compiler.parser.ProtoLexer;
import io.protostuff.compiler.parser.ProtoParser;
import io.protostuff.jetbrains.plugin.psi.CustomFieldReferenceNode;
import io.protostuff.jetbrains.plugin.psi.EnumConstantNode;
import io.protostuff.jetbrains.plugin.psi.EnumNode;
import io.protostuff.jetbrains.plugin.psi.ExtendEntryNode;
import io.protostuff.jetbrains.plugin.psi.ExtendNode;
import io.protostuff.jetbrains.plugin.psi.ExtensionsNode;
import io.protostuff.jetbrains.plugin.psi.FieldNode;
import io.protostuff.jetbrains.plugin.psi.FieldReferenceNode;
import io.protostuff.jetbrains.plugin.psi.FileReferenceNode;
import io.protostuff.jetbrains.plugin.psi.GenericNameNode;
import io.protostuff.jetbrains.plugin.psi.GroupNode;
import io.protostuff.jetbrains.plugin.psi.ImportNode;
import io.protostuff.jetbrains.plugin.psi.MapKeyNode;
import io.protostuff.jetbrains.plugin.psi.MapNode;
import io.protostuff.jetbrains.plugin.psi.MessageNode;
import io.protostuff.jetbrains.plugin.psi.OneOfNode;
import io.protostuff.jetbrains.plugin.psi.OptionEntryNode;
import io.protostuff.jetbrains.plugin.psi.OptionNode;
import io.protostuff.jetbrains.plugin.psi.OptionValueNode;
import io.protostuff.jetbrains.plugin.psi.PackageStatement;
import io.protostuff.jetbrains.plugin.psi.ProtoPsiFileRoot;
import io.protostuff.jetbrains.plugin.psi.ProtoRootNode;
import io.protostuff.jetbrains.plugin.psi.RangeNode;
import io.protostuff.jetbrains.plugin.psi.ReservedFieldNamesNode;
import io.protostuff.jetbrains.plugin.psi.ReservedFieldRangesNode;
import io.protostuff.jetbrains.plugin.psi.RpcMethodNode;
import io.protostuff.jetbrains.plugin.psi.RpcMethodTypeNode;
import io.protostuff.jetbrains.plugin.psi.ServiceNode;
import io.protostuff.jetbrains.plugin.psi.StandardFieldReferenceNode;
import io.protostuff.jetbrains.plugin.psi.SyntaxStatement;
import io.protostuff.jetbrains.plugin.psi.TypeReferenceNode;
import io.protostuff.jetbrains.plugin.psi.stubs.EnumStub;
import io.protostuff.jetbrains.plugin.psi.stubs.FileStub;
import io.protostuff.jetbrains.plugin.psi.stubs.GroupStub;
import io.protostuff.jetbrains.plugin.psi.stubs.MessageStub;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.antlr.jetbrains.adapter.lexer.AntlrLexerAdapter;
import org.antlr.jetbrains.adapter.lexer.PsiElementTypeFactory;
import org.antlr.jetbrains.adapter.lexer.RuleIElementType;
import org.antlr.jetbrains.adapter.lexer.TokenIElementType;
import org.antlr.jetbrains.adapter.parser.AntlrParserAdapter;
import org.antlr.jetbrains.adapter.parser.DefaultSyntaxErrorFormatter;
import org.antlr.jetbrains.adapter.parser.SyntaxError;
import org.antlr.jetbrains.adapter.parser.SyntaxErrorFormatter;
import org.antlr.jetbrains.adapter.psi.AntlrPsiNode;
import org.antlr.v4.runtime.InputMismatchException;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.jetbrains.annotations.NotNull;

/**
 * Parser definition for Protobuf.
 *
 * @author Kostiantyn Shchepanovskyi
 */
public class ProtoParserDefinition implements ParserDefinition {

    public static final PsiElementTypeFactory ELEMENT_FACTORY = PsiElementTypeFactory
            .builder()
            .language(ProtoLanguage.INSTANCE)
            .parser(new ProtoParser(null))
            .addRuleElementType(EnumStub.TYPE)
            .addRuleElementType(GroupStub.TYPE)
            .addRuleElementType(MessageStub.TYPE)
            .build();
    public static final TokenSet KEYWORDS = ELEMENT_FACTORY.createTokenSet(
            ProtoLexer.PACKAGE,
            ProtoLexer.SYNTAX,
            ProtoLexer.IMPORT,
            ProtoLexer.PUBLIC,
            ProtoLexer.OPTION,
            ProtoLexer.MESSAGE,
            ProtoLexer.GROUP,
            ProtoLexer.OPTIONAL,
            ProtoLexer.REQUIRED,
            ProtoLexer.REPEATED,
            ProtoLexer.ONEOF,
            ProtoLexer.EXTEND,
            ProtoLexer.EXTENSIONS,
            ProtoLexer.RESERVED,
            ProtoLexer.TO,
            ProtoLexer.MAX,
            ProtoLexer.ENUM,
            ProtoLexer.SERVICE,
            ProtoLexer.RPC,
            ProtoLexer.STREAM,
            ProtoLexer.RETURNS,
            ProtoLexer.MAP,
            ProtoLexer.BOOLEAN_VALUE,
            ProtoLexer.DOUBLE,
            ProtoLexer.FLOAT,
            ProtoLexer.INT32,
            ProtoLexer.INT64,
            ProtoLexer.UINT32,
            ProtoLexer.UINT64,
            ProtoLexer.SINT32,
            ProtoLexer.SINT64,
            ProtoLexer.FIXED32,
            ProtoLexer.FIXED64,
            ProtoLexer.SFIXED32,
            ProtoLexer.SFIXED64,
            ProtoLexer.BOOL,
            ProtoLexer.STRING,
            ProtoLexer.BYTES
    );
    // keywords also can be identifiers
    public static final TokenSet IDENTIFIER_TOKEN_SET = ELEMENT_FACTORY.createTokenSet(
            ProtoLexer.IDENT,
            ProtoLexer.PACKAGE,
            ProtoLexer.SYNTAX,
            ProtoLexer.IMPORT,
            ProtoLexer.PUBLIC,
            ProtoLexer.OPTION,
            ProtoLexer.MESSAGE,
            ProtoLexer.GROUP,
            ProtoLexer.OPTIONAL,
            ProtoLexer.REQUIRED,
            ProtoLexer.REPEATED,
            ProtoLexer.ONEOF,
            ProtoLexer.EXTEND,
            ProtoLexer.EXTENSIONS,
            ProtoLexer.RESERVED,
            ProtoLexer.TO,
            ProtoLexer.MAX,
            ProtoLexer.ENUM,
            ProtoLexer.SERVICE,
            ProtoLexer.RPC,
            ProtoLexer.STREAM,
            ProtoLexer.RETURNS,
            ProtoLexer.MAP,
            ProtoLexer.BOOLEAN_VALUE,
            ProtoLexer.DOUBLE,
            ProtoLexer.FLOAT,
            ProtoLexer.INT32,
            ProtoLexer.INT64,
            ProtoLexer.UINT32,
            ProtoLexer.UINT64,
            ProtoLexer.SINT32,
            ProtoLexer.SINT64,
            ProtoLexer.FIXED32,
            ProtoLexer.FIXED64,
            ProtoLexer.SFIXED32,
            ProtoLexer.SFIXED64,
            ProtoLexer.BOOL,
            ProtoLexer.STRING,
            ProtoLexer.BYTES
    );
    public static final TokenSet COMMENT_TOKEN_SET = ELEMENT_FACTORY.createTokenSet(
            ProtoLexer.COMMENT,
            ProtoLexer.LINE_COMMENT,
            ProtoLexer.PLUGIN_DEV_MARKER
    );
    public static final TokenSet LITERAL_TOKEN_SET = ELEMENT_FACTORY.createTokenSet(
            ProtoLexer.STRING_NAME,
            ProtoLexer.STRING_VALUE,
            ProtoLexer.FLOAT_VALUE,
            ProtoLexer.INTEGER_VALUE,
            ProtoLexer.IDENT,
            ProtoLexer.PACKAGE,
            ProtoLexer.SYNTAX,
            ProtoLexer.IMPORT,
            ProtoLexer.PUBLIC,
            ProtoLexer.OPTION,
            ProtoLexer.MESSAGE,
            ProtoLexer.GROUP,
            ProtoLexer.OPTIONAL,
            ProtoLexer.REQUIRED,
            ProtoLexer.REPEATED,
            ProtoLexer.ONEOF,
            ProtoLexer.EXTEND,
            ProtoLexer.EXTENSIONS,
            ProtoLexer.RESERVED,
            ProtoLexer.TO,
            ProtoLexer.MAX,
            ProtoLexer.ENUM,
            ProtoLexer.SERVICE,
            ProtoLexer.RPC,
            ProtoLexer.STREAM,
            ProtoLexer.RETURNS,
            ProtoLexer.MAP,
            ProtoLexer.BOOLEAN_VALUE,
            ProtoLexer.DOUBLE,
            ProtoLexer.FLOAT,
            ProtoLexer.INT32,
            ProtoLexer.INT64,
            ProtoLexer.UINT32,
            ProtoLexer.UINT64,
            ProtoLexer.SINT32,
            ProtoLexer.SINT64,
            ProtoLexer.FIXED32,
            ProtoLexer.FIXED64,
            ProtoLexer.SFIXED32,
            ProtoLexer.SFIXED64,
            ProtoLexer.BOOL,
            ProtoLexer.STRING,
            ProtoLexer.BYTES
    );
    public static final TokenSet WHITESPACE = ELEMENT_FACTORY.createTokenSet(WS, NL);
    private static final Logger LOGGER = Logger.getInstance(ProtoParserDefinition.class);
    private static final SyntaxErrorFormatter ERROR_FORMATTER = new DefaultSyntaxErrorFormatter() {
        @Override
        public String formatMessage(SyntaxError error) {
            RecognitionException exception = error.getException();
            if (exception instanceof InputMismatchException) {
                InputMismatchException mismatchException = (InputMismatchException) exception;
                RuleContext ctx = mismatchException.getCtx();
                int ruleIndex = ctx.getRuleIndex();
                switch (ruleIndex) {
                    case ProtoParser.RULE_ident:
                        return ProtostuffBundle.message("error.expected.identifier");
                    default:
                        break;
                }
            }
            return super.formatMessage(error);
        }
    };
    private static final List<TokenIElementType> TOKEN_TYPES = ELEMENT_FACTORY
            .getTokenIElementTypes();
    public static final TokenIElementType ID = TOKEN_TYPES.get(ProtoLexer.IDENT);

    // tokens
    public static final TokenIElementType LCURLY = TOKEN_TYPES.get(ProtoLexer.LCURLY);
    public static final TokenIElementType RCURLY = TOKEN_TYPES.get(ProtoLexer.RCURLY);
    public static final TokenIElementType LPAREN = TOKEN_TYPES.get(ProtoLexer.LPAREN);
    public static final TokenIElementType RPAREN = TOKEN_TYPES.get(ProtoLexer.RPAREN);
    public static final TokenIElementType LSQUARE = TOKEN_TYPES.get(ProtoLexer.LSQUARE);
    public static final TokenIElementType RSQUARE = TOKEN_TYPES.get(ProtoLexer.RSQUARE);
    public static final TokenIElementType LT = TOKEN_TYPES.get(ProtoLexer.LT);
    public static final TokenIElementType GT = TOKEN_TYPES.get(ProtoLexer.GT);
    public static final TokenIElementType ASSIGN = TOKEN_TYPES.get(ProtoLexer.ASSIGN);
    private static final List<RuleIElementType> RULE_TYPES = ELEMENT_FACTORY.getRuleIElementTypes();
    // Rules
    public static final IElementType R_TYPE_REFERENCE = rule(RULE_typeReference);
    public static final IElementType R_NAME = rule(RULE_ident);
    public static final IElementType R_FIELD_MODIFIER = rule(RULE_fieldModifier);
    public static final IElementType R_FIELD_NAME = rule(RULE_fieldName);
    public static final IElementType R_GROUP_NAME = rule(RULE_groupName);
    public static final IElementType R_TAG = rule(RULE_tag);
    private static final IFileElementType FILE = new IFileElementType(ProtoLanguage.INSTANCE);
    private static final TokenSet COMMENTS = ELEMENT_FACTORY
            .createTokenSet(COMMENT, LINE_COMMENT, PLUGIN_DEV_MARKER);
    private static final TokenSet STRING = ELEMENT_FACTORY.createTokenSet(STRING_NAME, STRING_VALUE);
    private final Map<Integer, Function<ASTNode, PsiElement>> elementFactories = new HashMap<>();

    private final Map<String, Method> parserRuleMethods = createParserRuleMethods();

    /**
     * Create new parser definition.
     */
    public ProtoParserDefinition() {
        register(RULE_customFieldReference, CustomFieldReferenceNode::new);
        register(RULE_enumBlock, EnumNode::new);
        register(RULE_enumField, EnumConstantNode::new);
        register(RULE_enumFieldName, GenericNameNode::new);
        register(RULE_enumName, GenericNameNode::new);
        register(RULE_extendBlock, ExtendNode::new);
        register(RULE_extendBlockEntry, ExtendEntryNode::new);
        register(RULE_extensions, ExtensionsNode::new);
        register(RULE_field, FieldNode::new);
        register(RULE_fieldName, GenericNameNode::new);
        register(RULE_fieldRerefence, FieldReferenceNode::new);
        register(RULE_fileReference, FileReferenceNode::new);
        register(RULE_groupBlock, GroupNode::new);
        register(RULE_groupName, GenericNameNode::new);
        register(RULE_importStatement, ImportNode::new);
        register(RULE_map, MapNode::new);
        register(RULE_mapKey, MapKeyNode::new);
        register(RULE_messageBlock, MessageNode::new);
        register(RULE_messageName, GenericNameNode::new);
        register(RULE_oneof, OneOfNode::new);
        register(RULE_oneofName, GenericNameNode::new);
        register(RULE_option, OptionNode::new);
        register(RULE_optionEntry, OptionEntryNode::new);
        register(RULE_optionValue, OptionValueNode::new);
        register(RULE_packageStatement, PackageStatement::new);
        register(RULE_proto, ProtoRootNode::new);
        register(RULE_range, RangeNode::new);
        register(RULE_reservedFieldNames, ReservedFieldNamesNode::new);
        register(RULE_reservedFieldRanges, ReservedFieldRangesNode::new);
        register(RULE_rpcMethod, RpcMethodNode::new);
        register(RULE_rpcType, RpcMethodTypeNode::new);
        register(RULE_rpcName, GenericNameNode::new);
        register(RULE_serviceBlock, ServiceNode::new);
        register(RULE_serviceName, GenericNameNode::new);
        register(RULE_standardFieldRerefence, StandardFieldReferenceNode::new);
        register(RULE_typeReference, TypeReferenceNode::new);
        register(RULE_syntaxStatement, SyntaxStatement::new);
    }

    public static TokenIElementType token(int token) {
        return TOKEN_TYPES.get(token);
    }

    public static IElementType rule(int rule) {
        return (IElementType) RULE_TYPES.get(rule);
    }

    private Map<String, Method> createParserRuleMethods() {
        Map<String, Method> result = new HashMap<>();
        for (String ruleName : ruleNames) {
            try {
                Method method = ProtoParser.class.getMethod(ruleName);
                result.put(ruleName, method);
            } catch (NoSuchMethodException e) {
                LOGGER.error("Could not get parser method for rule " + ruleName);
            }
        }
        return Collections.unmodifiableMap(result);
    }

    private void register(int rule, Function<ASTNode, PsiElement> factory) {
        if (elementFactories.containsKey(rule)) {
            throw new IllegalStateException("Duplicate rule");
        }
        elementFactories.put(rule, factory);
    }

    @NotNull
    @Override
    public Lexer createLexer(Project project) {
        ProtoLexer lexer = new ProtoLexer(null);
        return new AntlrLexerAdapter(ProtoLanguage.INSTANCE, lexer, ELEMENT_FACTORY);
    }

    @Override
    public PsiParser createParser(Project project) {
        final ProtoParser parser = new ProtoParser(null);
        return new AntlrParserAdapter(ProtoLanguage.INSTANCE, parser, ELEMENT_FACTORY,
                ERROR_FORMATTER) {
            @Override
            protected ParseTree parse(Parser parser, IElementType root) {
                // start rule depends on root passed in; sometimes we want to create an ID node etc...
                ProtoParser protoParser = (ProtoParser) parser;
                if (root instanceof IFileElementType) {
                    return protoParser.proto();
                }
                if (root instanceof RuleIElementType) {
                    RuleIElementType type = (RuleIElementType) root;
                    String ruleName = ruleNames[type.getRuleIndex()];
                    return parserRule(protoParser, ruleName);
                }
                throw new UnsupportedOperationException(String.valueOf(root.getIndex()));
            }
        };
    }

    @NotNull
    private ParseTree parserRule(ProtoParser protoParser, String ruleName) {
        try {
            Method method = parserRuleMethods.get(ruleName);
            if (method == null) {
                throw new IllegalStateException("Not a parser rule: " + ruleName);
            }
            return (ParseTree) method.invoke(protoParser);
        } catch (Exception e) {
            throw new IllegalStateException("Exception in parser rule: " + ruleName, e);
        }
    }

    @Override
    public IStubFileElementType<FileStub> getFileNodeType() {
        return FileStub.TYPE;
    }

    @NotNull
    @Override
    public TokenSet getWhitespaceTokens() {
        return WHITESPACE;
    }

    @NotNull
    @Override
    public TokenSet getCommentTokens() {
        return COMMENTS;
    }

    @NotNull
    @Override
    public TokenSet getStringLiteralElements() {
        return STRING;
    }

    @NotNull
    @Override
    public PsiElement createElement(ASTNode node) {
        IElementType elType = node.getElementType();
        if (elType instanceof TokenIElementType) {
            return new AntlrPsiNode(node);
        }
        if (!(elType instanceof RuleIElementType)) {
            return new AntlrPsiNode(node);
        }
        RuleIElementType ruleElType = (RuleIElementType) elType;
        int ruleIndex = ruleElType.getRuleIndex();
        if (elementFactories.containsKey(ruleIndex)) {
            Function<ASTNode, PsiElement> factory = elementFactories.get(ruleIndex);
            return factory.apply(node);
        }
        return new AntlrPsiNode(node);
    }

    @Override
    public PsiFile createFile(FileViewProvider viewProvider) {
        return new ProtoPsiFileRoot(viewProvider);
    }

    @Override
    public SpaceRequirements spaceExistenceTypeBetweenTokens(ASTNode left, ASTNode right) {
        return SpaceRequirements.MAY;
    }
}
