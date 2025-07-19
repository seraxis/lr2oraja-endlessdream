package bms.player.beatoraja.modmenu.fm;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/*
 * Represents one folder definition in folder/default.json
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FolderDefinition {
    private String sql;
    private String name;
    @JsonProperty("showall")
    private Boolean showAll;
    @JsonProperty("folder")
    private List<FolderDefinition> children;

    // NOTE: This field can be viewed as key
    @JsonIgnore
    private Integer bits;

    public FolderDefinition(String sql, String name, Boolean showAll, Integer bits) {
        this.sql = sql;
        this.name = name;
        this.showAll = showAll;
        this.bits = bits;
    }

    public FolderDefinition(String sql, String name, Boolean showAll) {
        this(sql, name, showAll, null);
    }

    public FolderDefinition() {
        this(null, null, null, null);
    }

    /**
     * If sql field satisfy 'favorite & x != 0' pattern, set corresponding bits field
     * Otherwise do nothing
     *
     * @implSpec Do not throw error even if sql field is corrupted or cannot take bits from it
     */
    public void tryExtractBitsFromSql() {
        if (sql == null || sql.isEmpty()) {
            return ;
        }
        String left = "favorite & ";
        String right = " != 0";
        if (!sql.startsWith(left) || !sql.endsWith(right)) {
            return ;
        }
        String expectedStr = sql.substring(left.length(), sql.length() - right.length());
        if (expectedStr.isEmpty()) {
            return ;
        }
        try {
            int pw = Integer.parseInt(expectedStr);
            this.bits = 31 - Integer.numberOfLeadingZeros(pw);
        } catch (Exception e) {
            // Do nothing
        }
    }

    public String getSql() {
        return sql;
    }
    
    public String getName() {
        return name;
    }

    public Boolean getShowAll() {
        return showAll;
    }

    public Integer getBits() {
        return bits;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setShowAll(Boolean showAll) {
        this.showAll = showAll;
    }

    public void setBits(Integer bits) {
        this.bits = bits;
    }
}
