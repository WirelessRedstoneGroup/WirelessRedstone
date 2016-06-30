package net.licks92.WirelessRedstone.Libs;

import java.util.ArrayList;
import java.util.List;

public class DeleteBuilder {

    private String table = null;
    private List<String> wheres = new ArrayList<String>();

    public DeleteBuilder(String table) {
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

    public DeleteBuilder where(String where){
        this.wheres.add(where);
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sql = new StringBuilder("DELETE FROM ");

        sql.append("'").append(table).append("'");

        appendList(sql, wheres, " WHERE ", " AND ", "");

        return sql.toString();
    }

}
