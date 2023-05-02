package com.jm.blogitz.database;

public class BlogitzDbSchema {
    public static final class BlogTable {
        public static final String TABLE_NAME = "blogs";
        public static final class Cols {
            public static final String UUID = "uuid";
            public static final String TITLE = "title";
            public static final String BODY = "body";
        }
    }
}
