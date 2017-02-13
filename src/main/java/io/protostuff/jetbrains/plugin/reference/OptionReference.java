package io.protostuff.jetbrains.plugin.reference;

import com.google.common.collect.ImmutableMap;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import io.protostuff.jetbrains.plugin.psi.*;
import io.protostuff.jetbrains.plugin.resources.BundledResourceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

import static io.protostuff.compiler.model.ProtobufConstants.*;

/**
 * @author Kostiantyn Shchepanovskyi
 */
public class OptionReference extends PsiReferenceBase<PsiElement> {

    private static final Logger LOGGER = Logger.getInstance(OptionReference.class);

    // "default" field option (a special case)
    private static final String DEFAULT = "default";

    // File descriptor.proto from io.protostuff.compiler:protostuff-parser.
    // Originally copied from
    // https://github.com/google/protobuf/blob/master/src/google/protobuf/descriptor.proto
    private static final String DESCRIPTOR_PROTO_RESOURCE = "google/protobuf/__descriptor.proto";
    private static final String DESCRIPTOR_PROTO_NAME = "google/protobuf/descriptor.proto";
    private static final VirtualFile DESCRIPTOR_PROTO = loadInMemoryDescriptorProto();

    private String key;

    public OptionReference(PsiElement element, TextRange textRange) {
        super(element, textRange, true);
        key = element.getText();
    }

    private static final Map<Predicate<PsiElement>, String> TARGET_MAPPING
            = ImmutableMap.<Predicate<PsiElement>, String>builder()
            .put(e -> e instanceof FieldNode, MSG_FIELD_OPTIONS)
            .put(e -> e instanceof MessageNode, MSG_MESSAGE_OPTIONS)
            .put(e -> e instanceof EnumConstantNode, MSG_ENUM_VALUE_OPTIONS)
            .put(e -> e instanceof EnumNode, MSG_ENUM_OPTIONS)
            .put(e -> e instanceof RpcMethodNode, MSG_METHOD_OPTIONS)
            .put(e -> e instanceof ServiceNode, MSG_SERVICE_OPTIONS)
            .put(e -> e instanceof ProtoRootNode, MSG_FILE_OPTIONS)
            .build();


    @Nullable
    private String getTarget() {
        PsiElement element = getElement();
        while (element != null) {
            PsiElement el = element;
            Optional<String> result = TARGET_MAPPING.entrySet().stream()
                    .filter(e -> e.getKey().test(el))
                    .map(Map.Entry::getValue)
                    .findFirst();
            if (result.isPresent()) {
                return result.get();
            }
            element = element.getParent();
        }
        return null;
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        if (key == null || key.isEmpty()) {
            return null;
        }
        if (key.startsWith("(")) {
            return resolveCustomOptionReference();
        } else {
            return resolveStandardOptionReference();
        }
    }

    private PsiElement resolveStandardOptionReference() {
        String targetType = getTarget();
        if (MSG_FIELD_OPTIONS.equals(targetType)
                && DEFAULT.equals(key)) {
            return resolveDefaultOptionReference();
        }
        MessageNode message = resolveType(targetType);
        if (message == null) {
            LOGGER.error("Could not resolve " + targetType);
            return null;
        }
        for (MessageField field : message.getFields()) {
            if (Objects.equals(key, field.getFieldName())) {
                return field;
            }
        }
        return null;
    }

    /**
     * "default" field option is a special case: it is not defined
     * in the {@code google/protobuf/descriptor.proto} and it cannot
     * be treated like other options, as its type depends on a field's
     * type.
     * <p>
     * In order to implement value validation, we have to return the
     * field where this option was applied.
     */
    private PsiElement resolveDefaultOptionReference() {
        PsiElement element = getElement();
        while (element != null) {
            if (element instanceof FieldNode) {
                return element;
            }
            element = element.getParent();
        }
        return null;
    }

    @Nullable
    private PsiElement resolveCustomOptionReference() {
        // custom option
        // TODO: resolve
        return null;
    }

    private MessageNode resolveType(String qualifiedName) {
        MessageNode message = resolveTypeFromCurrentFile(qualifiedName);
        if (message == null) {
            ProtoPsiFileRoot descriptorProto = (ProtoPsiFileRoot) getBundledDescriptorProto();
            return (MessageNode) descriptorProto.findType(qualifiedName.substring(1));
        }
        return message;
    }

    private PsiFile getBundledDescriptorProto() {
        Project project = getElement().getProject();
        return PsiManager.getInstance(project).findFile(DESCRIPTOR_PROTO);
    }

    @NotNull
    private static VirtualFile loadInMemoryDescriptorProto() {
        Application application = ApplicationManager.getApplication();
        BundledResourceProvider resourceProvider = application.getComponent(BundledResourceProvider.class);
        Optional<VirtualFile> descriptor = resourceProvider.getResource(DESCRIPTOR_PROTO_RESOURCE, DESCRIPTOR_PROTO_NAME);
        if (!descriptor.isPresent()) {
            throw new IllegalStateException("Could not load bundled resource: " + DESCRIPTOR_PROTO_RESOURCE);
        }
        return descriptor.get();
    }

    @Nullable
    private MessageNode resolveTypeFromCurrentFile(String qualifiedName) {
        PsiElement protoElement = getElement();
        while (protoElement != null && !(protoElement instanceof ProtoRootNode)) {
            protoElement = protoElement.getParent();
        }
        if (protoElement == null) {
            return null;
        }
        ProtoRootNode proto = (ProtoRootNode) protoElement;
        return (MessageNode) proto.resolve(qualifiedName, new ArrayDeque<>());
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return new Object[0];
    }
}
