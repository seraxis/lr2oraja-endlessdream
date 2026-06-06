package bms.player.beatoraja;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * General definition of an SQLite database accessor. Manages creation of tables and inserting data.
 */
public abstract class SQLiteDatabaseAccessor {

    private final ColumnHandler columnHandler = new ColumnHandler();

    private final Map<String, Table> tables;

    public SQLiteDatabaseAccessor(Table... tables) {
        this.tables = Arrays.stream(tables)
                .collect(Collectors.toMap(
                        Table::name,
                        table -> table
                ));
    }

    /**
     * Creates all tables managed by a DatabaseAccessor and adds missing columns to existing tables.
     *
     * @param qr QueryRunner
     * @throws SQLException
     */
    public void validate(QueryRunner qr) throws SQLException {
        for (var table : tables.values()) {
            if (!tableExists(qr, table)) {
                qr.update(table.generateTableDdl());
            } else {
                var existingColumns = qr.query("PRAGMA table_info('" + table.name() + "');", columnHandler).stream()
                        .map(Column::name)
                        .toList();
                var newColumns = Arrays.stream(table.columns())
                        .filter(column -> !existingColumns.contains(column.name()))
                        .toList();
                for (var newColumn : newColumns) {
                    qr.update("ALTER TABLE " + table.name() + " ADD COLUMN [" + newColumn.name() + "] " + newColumn.type()
                            + (newColumn.notNull() ? " NOT NULL" : "") + (newColumn.hasDefaultValue() ? " DEFAULT " + newColumn.defaultValue() : ""));
                }
            }
        }
    }

    protected void insert(QueryRunner qr, String tableName, Object entity) throws SQLException {
        insert(qr, null, tableName, entity);
    }

    protected void insert(QueryRunner qr, Connection con, String tableName, Object entity) throws SQLException {
        var columns = Optional.ofNullable(tables.get(tableName))
                .map(Table::columns)
                .orElse(null);
        if (columns == null) {
            return;
        }

        var columnNames = new StringJoiner(",");
        var placeholders = new StringJoiner(",");
        for (var column : columns) {
            columnNames.add(column.name());
            placeholders.add("?");
        }
        var sql = "INSERT OR REPLACE INTO " + tableName + " (" + columnNames + ") VALUES (" + placeholders + ");";

        Object[] params = new Object[columns.length];
        for (int i = 0; i < columns.length; i++) {
            try {
                var propertyDescriptor = new PropertyDescriptor(columns[i].name(), entity.getClass());
                var getterMethod = propertyDescriptor.getReadMethod();
                params[i] = getterMethod.invoke(entity);
            } catch (IntrospectionException | ReflectiveOperationException | IllegalArgumentException e) {
                e.printStackTrace();
            }
        }

        if (con != null) {
            qr.update(con, sql, params);
        } else {
            qr.update(sql, params);
        }
    }

    private boolean tableExists(QueryRunner qr, Table table) throws SQLException {
        return !qr.query(
                "SELECT * FROM sqlite_master WHERE name = ? and type='table';",
                new MapListHandler(),
                table.name()
        ).isEmpty();
    }

    public record Table(String name, Column... columns) {

        private String generateTableDdl() {
            var tableDdl = new StringBuilder("CREATE TABLE [" + name + "] (");
            var columnDdls = Arrays.stream(columns)
                    .map(Column::generateColumnDdl)
                    .collect(Collectors.joining(","));
            tableDdl.append(columnDdls);

            var primaryKeys = Arrays.stream(columns)
                    .filter(Column::primaryKey)
                    .map(Column::name)
                    .toList();
            if (!primaryKeys.isEmpty()) {
                tableDdl.append(",PRIMARY KEY(");
                var pkColumnNames = String.join(",", primaryKeys);
                tableDdl.append(pkColumnNames);
                tableDdl.append(")");
            }
            tableDdl.append(");");
            return tableDdl.toString();
        }
    }

    public record Column(String name, String type, boolean notNull, boolean primaryKey, String defaultValue) {

        public Column(String name, String type) {
            this(name, type, false, false, null);
        }

        public Column(String name, String type, boolean notNull, boolean primaryKey) {
            this(name, type, notNull, primaryKey, null);
        }

        private boolean hasDefaultValue() {
            return defaultValue != null && !defaultValue.isEmpty();
        }

        private String generateColumnDdl() {
            return "[" +
                    name +
                    "] " +
                    type +
                    (notNull ? " NOT NULL" : "") +
                    (hasDefaultValue() ? " DEFAULT " + defaultValue : "");
        }
    }

    public static class ColumnHandler implements ResultSetHandler<List<Column>> {

        @Override
        public List<Column> handle(ResultSet rs) throws SQLException {
            List<Column> columns = new ArrayList<>();
            while (rs.next()) {
                columns.add(
                        new Column(
                                rs.getString("name"),
                                rs.getString("type"),
                                rs.getInt("notnull") != 0,
                                rs.getInt("pk") != 0,
                                rs.getString("dflt_value")
                        )
                );
            }
            return columns;
        }
    }
}
