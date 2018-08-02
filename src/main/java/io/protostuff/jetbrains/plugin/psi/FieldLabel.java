package io.protostuff.jetbrains.plugin.psi;

import java.util.Optional;

/**
 * Field label.
 *
 * @author Kostiantyn Shchepanovskyi
 */
public enum FieldLabel {
    OPTIONAL("optional"),
    REQUIRED("required"),
    REPEATED("repeated"),
    JSON_NAME("json_name");

    private final String name;

    FieldLabel(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    /**
     * Get field label by name.
     */
    public static Optional<FieldLabel> forString(String name) {
        for (FieldLabel fieldLabel : FieldLabel.values()) {
            if (fieldLabel.getName().equals(name)) {
                return Optional.of(fieldLabel);
            }
        }
        return Optional.empty();
    }
}
