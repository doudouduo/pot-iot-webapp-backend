package com.pot.iot.webapp;

import org.hibernate.dialect.MySQL5Dialect;
import org.hibernate.dialect.MySQL5InnoDBDialect;

public class MySQL5DialectUTF8 extends MySQL5InnoDBDialect {
    @Override
    public String getTableTypeString(){
        return " ENGINE=InnoDB DEFAULT CHARSET=utf8";
    }
}
