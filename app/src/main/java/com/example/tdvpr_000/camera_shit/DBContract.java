package com.example.tdvpr_000.camera_shit;

import android.provider.BaseColumns;

/**
 * Created by tdvpr_000 on 2/14/2017.
 */

public final class DBContract {
    private DBContract() {}

    public static class FeedEntry implements BaseColumns {
        public static final String DATABASE_NAME = "Tracking.db";
        public static final String TABLE_NAME = "tags";

        public static final String COLUMN_FILE = "file";
        public static final String COLUMN_TAGS = "tag";

        public static final String COLUMN_VALUE = "value";

        public static final String DEFAULT_NUMERICAL_TAG = "Numerical";


        public static final String COLUMN_DATES = "dates";
    }

}
