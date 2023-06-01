package com.jm.blogitz.database;

/**
 * Blog database schema.
 */
public class BlogitzDbSchema {
    /**
     * Structure of the Blog table.
     */
    public static final class BlogTable {
        /**
         * Name of the blog table.
         */
        public static final String TABLE_NAME = "blogs";

        /**
         * Columns in the blog table.
         */
        public static final class Cols {
            /**
             * Blog id.
             */
            public static final String UUID = "uuid";
            /**
             * Blog title.
             */
            public static final String TITLE = "title";
            /**
             * Blog body.
             */
            public static final String BODY = "body";
        }
    }
}
