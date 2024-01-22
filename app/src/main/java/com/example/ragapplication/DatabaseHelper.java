package com.example.ragapplication;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "RAG_Database";
    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_EMBEDDING_TABLE = "CREATE TABLE IF NOT EXISTS Embedding (" +
                "Chunk_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "Chunk TEXT, " +
                "Vector_Representation TEXT, " +
                "Room_ID INTEGER, " +
                "File_ID INTEGER, " +
                "Created_At DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY(Room_ID) REFERENCES Room(Room_ID), " +
                "FOREIGN KEY(File_ID) REFERENCES Files(File_ID))";

        String CREATE_ROOM_TABLE = "CREATE TABLE IF NOT EXISTS Room (" +
                "Room_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "Room_Name TEXT, " +
                "Created_At DATETIME DEFAULT CURRENT_TIMESTAMP)";

        String CREATE_CHAT_HISTORY_TABLE = "CREATE TABLE IF NOT EXISTS Chat_History (" +
                "Room_ID INTEGER, " +
                "User_Query TEXT, " +
                "Model_Response TEXT, " +
                "Created_At DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY(Room_ID) REFERENCES Room(Room_ID))";

        String CREATE_FILES_TABLE = "CREATE TABLE IF NOT EXISTS Files (" +
                "File_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "File_Name TEXT, " +
                "File_Type TEXT, " +
                "Room_ID INTEGER, " +
                "FOREIGN KEY(Room_ID) REFERENCES Room(Room_ID))";

        db.execSQL(CREATE_EMBEDDING_TABLE);
        db.execSQL(CREATE_ROOM_TABLE);
        db.execSQL(CREATE_CHAT_HISTORY_TABLE);
        db.execSQL(CREATE_FILES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String DROP_EMBEDDING_TABLE = "DROP TABLE IF EXISTS Embedding";
        String DROP_ROOM_TABLE = "DROP TABLE IF EXISTS Room";
        String DROP_CHAT_HISTORY_TABLE = "DROP TABLE IF EXISTS Chat_History";
        String DROP_FILES_TABLE = "DROP TABLE IF EXISTS Files";

        db.execSQL(DROP_EMBEDDING_TABLE);
        db.execSQL(DROP_ROOM_TABLE);
        db.execSQL(DROP_CHAT_HISTORY_TABLE);
        db.execSQL(DROP_FILES_TABLE);

        onCreate(db);
    }

    public void numberOfEntriesInEachTable(Activity activity) {
        int numberOfEntriesInEmbeddingTable = 0;
        int numberOfEntriesInRoomTable = 0;
        int numberOfEntriesInChatHistoryTable = 0;
        int numberOfEntriesInFilesTable = 0;

        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();

        String query = "SELECT COUNT(*) FROM Embedding";
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            numberOfEntriesInEmbeddingTable = cursor.getInt(0);
        }

        query = "SELECT COUNT(*) FROM Room";
        cursor = sqLiteDatabase.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            numberOfEntriesInRoomTable = cursor.getInt(0);
        }

        query = "SELECT COUNT(*) FROM Chat_History";
        cursor = sqLiteDatabase.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            numberOfEntriesInChatHistoryTable = cursor.getInt(0);
        }

        query = "SELECT COUNT(*) FROM Files";
        cursor = sqLiteDatabase.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            numberOfEntriesInFilesTable = cursor.getInt(0);
        }

        Toast.makeText(activity, "Embedding Table: " + numberOfEntriesInEmbeddingTable, Toast.LENGTH_SHORT).show();
        Toast.makeText(activity, "Room Table: " + numberOfEntriesInRoomTable, Toast.LENGTH_SHORT).show();
        Toast.makeText(activity, "Chat History Table: " + numberOfEntriesInChatHistoryTable, Toast.LENGTH_SHORT).show();
        Toast.makeText(activity, "Files Table: " + numberOfEntriesInFilesTable, Toast.LENGTH_SHORT).show();
    }
}