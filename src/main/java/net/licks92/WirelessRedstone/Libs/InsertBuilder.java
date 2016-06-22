package net.licks92.WirelessRedstone.Libs;

import java.util.ArrayList;
import java.util.List;

public class InsertBuilder {

    private String table = null;
    private List<String> columns = new ArrayList<String>();
    private List<String> values = new ArrayList<String>();

    public InsertBuilder(String table) {
        this.table = table;
    }

    private void appendList(StringBuilder sql, List<String> list, String init, String sep, String end) {
        boolean first = true;
        for (String s : list) {
            if (first) {
                sql.append(init);
            } else {
                sql.append(sep);
            }
            sql.append(s);
            first = false;
        }
        sql.append(end);
    }

    public InsertBuilder addColumn(String column){
        this.columns.add(column);
        return this;
    }

    public InsertBuilder addValue(String value){
        this.values.add(value);
        return this;
    }

    public InsertBuilder addColumnWithValue(String column, Object value){
        this.columns.add(column);
        this.values.add(value.toString());
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sql = new StringBuilder("INSERT INTO ");

        sql.append(table);

        appendList(sql, columns, " (", ", ", ")");
        appendList(sql, values, " VALUES (", ", ", ")");

        return sql.toString();
    }

}
