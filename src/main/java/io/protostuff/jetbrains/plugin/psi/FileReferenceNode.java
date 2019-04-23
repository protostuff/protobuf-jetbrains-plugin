package io.protostuff.jetbrains.plugin.psi;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileSystemItem;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiReference;
import io.protostuff.compiler.parser.Util;
import io.protostuff.jetbrains.plugin.reference.ImportProtoReference;
import io.protostuff.jetbrains.plugin.reference.file.FilePathReferenceProvider;
import java.util.Collection;
import org.antlr.jetbrains.adapter.psi.AntlrPsiNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * File reference node.
 *
 * @author Kostiantyn Shchepanovskyi
 */
public class FileReferenceNode extends AntlrPsiNode {

    private final FilePathReferenceProvider referenceProvider;

    public FileReferenceNode(@NotNull ASTNode node) {
        super(node);
        referenceProvider = new FilePathReferenceProvider(true);
    }

    @NotNull
    @Override
    public PsiReference[] getReferences() {
        String filename = getFilename();
        if (filename == null) {
            return new PsiReference[0];
        }
        int filenameStart = getText().lastIndexOf('/');
        TextRange textRange;
        if (filenameStart >= 0) {
            textRange = TextRange.create(filenameStart + 1, getTextLength() - 1);
        } else {
            textRange = TextRange.EMPTY_RANGE;
        }
        ProtoPsiFileRoot target = getTarget();
        ImportProtoReference reference = new ImportProtoReference(this, textRange, target);
        return new PsiReference[]{reference};
    }

    /**
     * Returns target proto PSI file root node.
     */
    @Nullable
    public ProtoPsiFileRoot getTarget() {
        String filename = getFilename();
        if (filename == null) {
            return null;
        }
        Module module = ModuleUtilCore.findModuleForPsiElement(this);
        if (module != null) {
            return getTarget(filename, module);
        }
        // fallback: if we are inside of a dependency, current module is null
        // in this case we try to resolve reference in all dependencies of all modules
        // (might be not fully correct, but better than nothing)
        ModuleManager moduleManager = ModuleManager.getInstance(getProject());
        Module[] modules = moduleManager.getModules();
        for (Module m : modules) {
            ProtoPsiFileRoot target = getTarget(filename, m);
            if (target != null) {
                return target;
            }
        }
        return null;
    }

    private ProtoPsiFileRoot getTarget(@NotNull String filename, @NotNull Module module) {
        Collection<PsiFileSystemItem> roots = new FilePathReferenceProvider().getRoots(module, getRoot());
        for (PsiFileSystemItem root : roots) {
            VirtualFile file = root.getVirtualFile().findFileByRelativePath(filename);
            if (file != null) {
                PsiManager psiManager = PsiManager.getInstance(getProject());
                PsiFile psiFile = psiManager.findFile(file);
                if (psiFile instanceof ProtoPsiFileRoot) {
                    return (ProtoPsiFileRoot) psiFile;
                }
            }
        }
        return null;
    }

    @Nullable
    private ProtoPsiFileRoot getRoot() {
        PsiElement node = this;
        while (!(node instanceof ProtoPsiFileRoot || node == null)) {
            node = node.getParent();
        }
        return (ProtoPsiFileRoot) node;
    }

    @Nullable
    private String getFilename() {
        String text = getText();
        if (text == null
                || text.length() < 2
                || !text.startsWith("\"")
                || !text.endsWith("\"")) {
            return null;
        }
        return Util.trimStringName(text);
    }

}
