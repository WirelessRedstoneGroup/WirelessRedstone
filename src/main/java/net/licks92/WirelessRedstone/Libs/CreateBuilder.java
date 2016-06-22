package net.licks92.WirelessRedstone.Libs;

import java.util.ArrayList;
import java.util.List;

public class CreateBuilder {

    private String table = null;
    private Boolean ifNotExist = true;
    private List<String> columns = new ArrayList<String>();

    public CreateBuilder(String table) {
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

    public CreateBuilder setIfNotExist(Boolean bool){
        this.ifNotExist = bool;
        return this;
    }

    public CreateBuilder addColumn(String column, String type){
        this.columns.add(column + " " + type);
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sql = new StringBuilder("CREATE TABLE ");

        if(ifNotExist)
            sql.append("IF NOT EXISTS ");

        sql.append(table);
        appendList(sql, columns, " (", ", ", ")");

        return sql.toString();
    }
}
