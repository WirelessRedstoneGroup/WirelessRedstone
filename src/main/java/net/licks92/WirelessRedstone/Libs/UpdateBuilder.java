package net.licks92.WirelessRedstone.Libs;

import java.util.ArrayList;
import java.util.List;

public class UpdateBuilder {

    private String table = null;
    private List<String> sets = new ArrayList<String>();
    private List<String> wheres = new ArrayList<String>();

    public UpdateBuilder(String table) {
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

    public UpdateBuilder set(String set){
        this.sets.add(set);
        return this;
    }

    public UpdateBuilder where(String where){
        this.wheres.add(where);
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sql = new StringBuilder("UPDATE ");

        sql.append(table);

        appendList(sql, sets, " SET ", ", ", "");
        appendList(sql, wheres, " WHERE ", " AND ", "");

        return sql.toString();
    }

}
