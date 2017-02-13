package io.protostuff.jetbrains.plugin.resources;

import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * @author Kostiantyn Shchepanovskyi
 */
public interface BundledResourceProvider {

    /**
     * Returns a read-only {@code VirtualFile} for a given resource, bundled with plugin.
     */
    Optional<VirtualFile> getResource(String resource, String displayName);
}
