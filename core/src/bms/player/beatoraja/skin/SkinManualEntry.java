package bms.player.beatoraja.skin;

import com.github.therapi.runtimejavadoc.CommentFormatter;
import com.github.therapi.runtimejavadoc.FieldJavadoc;
import com.github.therapi.runtimejavadoc.RuntimeJavadoc;

/**
 * Represents one skin property's manual
 *
 * @param <T> Skin property corresponding enums, see StringPropertyFactory.StringProperty for example
 * @implNote getComment & getFullName are lazy functions
 */
public class SkinManualEntry<T extends Enum<T>> {
    private static final CommentFormatter formatter = new CommentFormatter();

    private final Enum<T> ref;
    private final String name;
    private final int id;
    private String comment;
    private String fullName;

    public SkinManualEntry(Enum<T> ref, String name, int id) {
        this.ref = ref;
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public String getFullName() {
        if (this.fullName != null) {
            return this.fullName;
        }
        this.fullName = String.format("%s(%d)", this.name, this.id);
        return this.fullName;
    }

    public String getComment() {
        if (this.comment != null) {
            return this.comment;
        }
        FieldJavadoc javadoc = RuntimeJavadoc.getJavadoc(ref);
        this.comment = javadoc.isEmpty() ? "" : formatter.format(javadoc.getComment());
        return this.comment;
    }
}
