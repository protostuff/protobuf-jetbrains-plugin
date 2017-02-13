package io.protostuff.jetbrains.plugin.resources;

import com.google.common.collect.Maps;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.io.StreamUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightVirtualFile;
import io.protostuff.jetbrains.plugin.reference.OptionReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Kostiantyn Shchepanovskyi
 */
public class BundledResourceProviderImpl implements BundledResourceProvider {

    private static final Logger LOGGER = Logger.getInstance(BundledResourceProviderImpl.class);

    private ConcurrentMap<ResourceId, Optional<VirtualFile>> cache = Maps.newConcurrentMap();

    @Override
    public Optional<VirtualFile> getResource(String resource, String displayName) {
        return cache.computeIfAbsent(new ResourceId(resource, displayName),
                this::getResourceImpl);
    }

    private Optional<VirtualFile> getResourceImpl(ResourceId resourceId) {
        String resource = resourceId.resource;
        String displayName = resourceId.displayName;
        String content = readClasspathResource(resource);
        if (content == null) {
            return Optional.empty();
        }
        LightVirtualFile vf = createVirtualFile(displayName, content);
        vf.markReadOnly();
        return Optional.of(vf);
    }

    @NotNull
    private LightVirtualFile createVirtualFile(String resource, String content) {
        LightVirtualFile vf = new LightVirtualFile(resource, content);
        vf.markReadOnly();
        return vf;
    }

    @Nullable
    private String readClasspathResource(String name) {
        try {
            String classpath = System.getProperty("java.class.path");
            LOGGER.info("Reading " + name + " from classpath=" + classpath);
            ClassLoader classLoader = OptionReference.class.getClassLoader();
            if (classLoader == null) {
                throw new IllegalStateException("Can not obtain classloader instance");
            }
            InputStream resource = classLoader.getResourceAsStream(name);
            if (resource == null) {
                LOGGER.info("Could not find " + name);
                return null;
            }
            return StreamUtil.readText(resource, StandardCharsets.UTF_8);
        } catch (Exception e) {
            LOGGER.error("Could not read " + name, e);
        }
        return null;
    }

    static class ResourceId {
        final String resource;
        final String displayName;

        public ResourceId(String resource, String displayName) {
            this.resource = resource;
            this.displayName = displayName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ResourceId that = (ResourceId) o;
            return Objects.equals(resource, that.resource) &&
                    Objects.equals(displayName, that.displayName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(resource, displayName);
        }
    }

}
