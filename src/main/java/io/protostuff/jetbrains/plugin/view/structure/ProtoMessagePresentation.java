package io.protostuff.jetbrains.plugin.view.structure;

import com.intellij.navigation.ItemPresentation;
import io.protostuff.jetbrains.plugin.Icons;
import io.protostuff.jetbrains.plugin.psi.MessageNode;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class ProtoMessagePresentation implements ItemPresentation {
    protected final MessageNode element;

    protected ProtoMessagePresentation(MessageNode element) {
        this.element = element;
    }

    @Nullable
    @Override
    public Icon getIcon(boolean unused) {
        return Icons.MESSAGE;
    }

    @Nullable
    @Override
    public String getPresentableText() {
        return element.getName();
    }

    @Nullable
    @Override
    public String getLocationString() {
        return null;
    }
}