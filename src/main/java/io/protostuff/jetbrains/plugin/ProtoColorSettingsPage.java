package io.protostuff.jetbrains.plugin;

import com.google.common.base.Joiner;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Color settings page.
 */
public class ProtoColorSettingsPage implements ColorSettingsPage {
    private static final AttributesDescriptor[] DESCRIPTORS = new AttributesDescriptor[]{
            new AttributesDescriptor("Keyword", ProtoSyntaxHighlighter.KEYWORD),
            new AttributesDescriptor("String name", ProtoSyntaxHighlighter.STRING_NAME),
            new AttributesDescriptor("String value", ProtoSyntaxHighlighter.STRING_VALUE),
            new AttributesDescriptor("Number", ProtoSyntaxHighlighter.NUMBER),
            new AttributesDescriptor("Line comment", ProtoSyntaxHighlighter.LINE_COMMENT),
            new AttributesDescriptor("Block comment", ProtoSyntaxHighlighter.BLOCK_COMMENT),
            new AttributesDescriptor("Enum constant", ProtoSyntaxHighlighter.ENUM_CONSTANT)
    };

    private Map<String, TextAttributesKey> additionalTags;

    public ProtoColorSettingsPage() {
        initAdditionalTags();
    }

    private void initAdditionalTags() {
        additionalTags = new HashMap<>();
        additionalTags.put("keyword", ProtoSyntaxHighlighter.KEYWORD);
        additionalTags.put("constant", ProtoSyntaxHighlighter.ENUM_CONSTANT);
    }

    @Nullable
    @Override
    public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
        return additionalTags;
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return Icons.PROTO;
    }

    @NotNull
    @Override
    public SyntaxHighlighter getHighlighter() {
        return new ProtoSyntaxHighlighter();
    }

    @NotNull
    @Override
    public String getDemoText() {
        return Joiner.on('\n').join(
                "syntax = \"proto3\";\n",
                "",
                "/* block comment */",
                "<keyword>message</keyword> Foo {",
                "   // line comment",
                "   <keyword>optional</keyword> <keyword>int32</keyword> x = 1;",
                "",
                "   <keyword>enum</keyword> Bar {",
                "       <constant>CONSTANT</constant> = 0 [baz = \"daf\"];",
                "   }",
                "}",
                ""
        );
    }

    @NotNull
    @Override
    public AttributesDescriptor[] getAttributeDescriptors() {
        return DESCRIPTORS;
    }

    @NotNull
    @Override
    public ColorDescriptor[] getColorDescriptors() {
        return ColorDescriptor.EMPTY_ARRAY;
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Protobuf";
    }
}
