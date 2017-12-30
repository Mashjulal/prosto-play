package com.mashjulal.android.prostoplay.database;


public class PlaylistDBSchema {
    public static final class PlaylistTable {
        public static final String TABLE_NAME = "table_playlist";

        public static final class Columns {
            public static final String
                    TRACK_ID = "track_id",
                    TRACK = "track",
                    ARTIST = "artist",
                    LENGTH = "length",
                    BITRATE = "bitrate",
                    SIZE = "size",
                    PATH = "path";
        }

    }
}
