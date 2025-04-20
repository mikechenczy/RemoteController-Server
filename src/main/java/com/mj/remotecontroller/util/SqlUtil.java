package com.mj.remotecontroller.util;

public class SqlUtil {
    public static String getInsertSql(String table, String ... fieldsAndValues) {
        String s = "insert into "+table+" (";
        for(int i=0;i<fieldsAndValues.length;i++) {
            s = s+fieldsAndValues[i].split(";")[0];
            if(i+1!=fieldsAndValues.length)
                s = s+", ";
            else
                s = s+") values (";
        }
        for(int i=0;i<fieldsAndValues.length;i++) {
            s = s+fieldsAndValues[i].split(";")[1];
            if(i+1!=fieldsAndValues.length)
                s = s+", ";
            else
                s = s+")";
        }
        return s;
    }
}
