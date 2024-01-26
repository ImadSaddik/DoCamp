package com.example.ragapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
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
                "FOREIGN KEY(Room_ID) REFERENCES Room(Room_ID) ON DELETE CASCADE , " +
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
                "FOREIGN KEY(Room_ID) REFERENCES Room(Room_ID) ON DELETE CASCADE)";

        String CREATE_FILES_TABLE = "CREATE TABLE IF NOT EXISTS Files (" +
                "File_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "File_Name TEXT, " +
                "File_Type TEXT, " +
                "Room_ID INTEGER, " +
                "FOREIGN KEY(Room_ID) REFERENCES Room(Room_ID) ON DELETE CASCADE)";

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

    public int getMaxRoomID() {
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        String query = "SELECT MAX(Room_ID) FROM Room";
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            return cursor.getInt(0);
        }

        return -1;
    }

    public Cursor getRoom(int roomId) {
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        String query = "SELECT * FROM Room WHERE Room_ID = " + roomId;
        return sqLiteDatabase.rawQuery(query, null);
    }

    public Cursor getChatHistory(int roomId) {
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        String query = "SELECT * FROM Chat_History WHERE Room_ID = " + roomId;
        return sqLiteDatabase.rawQuery(query, null);
    }

    public Cursor getFilesHistory(int roomId) {
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        String query = "SELECT * FROM Files WHERE Room_ID = " + roomId;
        return sqLiteDatabase.rawQuery(query, null);
    }

    public void insertRoom(int roomID, String roomName) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put("Room_ID", roomID);
        contentValues.put("Room_Name", roomName);
        db.insert("Room", null, contentValues);
    }

    public void setRoomName(String roomName, int roomID) {
        SQLiteDatabase db = this.getWritableDatabase();

        String query = "UPDATE Room SET Room_Name = '" + roomName + "' WHERE Room_ID = " + roomID;
        db.execSQL(query);
    }

    public void insertFile(String fileName, String mimeType, Integer roomId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String fileType = FileUtilities.getFileType(mimeType);

        String query = "SELECT File_ID FROM Files WHERE File_Name = ? AND Room_ID = ?";
        Cursor cursor = db.rawQuery(query, new String[]{fileName, roomId.toString()});
        if (cursor.moveToFirst()) {
            cursor.close();
        } else {
            ContentValues contentValues = new ContentValues();
            contentValues.put("File_Name", fileName);
            contentValues.put("File_Type", fileType);
            contentValues.put("Room_ID", roomId);
            db.insert("Files", null, contentValues);
        }
    }

    public int getFileId(String fileName, String fileType) {
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        String escapedFileName = fileName.replace("'", "''");
        String query = "SELECT File_ID FROM Files WHERE File_Name = '" + escapedFileName + "' AND File_Type = '" + fileType + "'";
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            int fileId = cursor.getInt(0);
            return fileId;
        }

        return -1;
    }

    public int getFileCountInRoom(int roomId) {
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM Files WHERE Room_ID = " + roomId;
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            int count = cursor.getInt(0);
            return count;
        }

        return -1;
    }

    public void insertEmbedding(String chunk, String vectorRepresentation, Integer roomId, int fileId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("Chunk", chunk);
        contentValues.put("Vector_Representation", vectorRepresentation);
        contentValues.put("Room_ID", roomId);
        contentValues.put("File_ID", fileId);
        db.insert("Embedding", null, contentValues);
    }

    public Cursor getEmbeddingRows(int roomId) {
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        String query = "SELECT Chunk, Vector_Representation FROM Embedding WHERE Room_ID = " + roomId;
        return sqLiteDatabase.rawQuery(query, null);
    }

    public void insertRowInChatHistory(int roomId, String userQuery, String modelResponse) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("Room_ID", roomId);
        contentValues.put("User_Query", userQuery);
        contentValues.put("Model_Response", modelResponse);
        db.insert("Chat_History", null, contentValues);
    }

    public Cursor getActiveRooms() {
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        String query = "SELECT DISTINCT Room.* FROM Room INNER JOIN Files ON Room.Room_ID = Files.Room_ID ORDER BY Room.Created_At DESC";
        return sqLiteDatabase.rawQuery(query, null);
    }

    public void deleteRoom(int roomId) {
        SQLiteDatabase db = this.getWritableDatabase();

        String query = "DELETE FROM Files WHERE Room_ID = " + roomId;
        db.execSQL(query);

        query = "DELETE FROM Embedding WHERE Room_ID = " + roomId;
        db.execSQL(query);

        query = "DELETE FROM Chat_History WHERE Room_ID = " + roomId;
        db.execSQL(query);

        query = "DELETE FROM Room WHERE Room_ID = " + roomId;
        db.execSQL(query);
    }
}