package com.jfocht.AudioRecorderApp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.text.SimpleDateFormat;
import java.text.ParseException;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;


public class AudioClipDatabase extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "clipsManager";

    // Audio Clip table name
    private static final String TABLE_CLIPS = "clips";

    // Audio Clip Table Columns names
    public static final String KEY_ID = "id";
    public static final String KEY_NAME = "name";
    public static final String KEY_FILENAME = "filename";
    public static final String KEY_DURATION = "duration";
    public static final String KEY_CREATED = "created";

    private static final SimpleDateFormat SQLITE_DATE_FORMAT =
        new SimpleDateFormat("yyyy-MM-dd k:m:s");

    public AudioClipDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        android.util.Log.d("Record Audio", "Finished opening database");
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CLIPS_TABLE = "CREATE TABLE " + TABLE_CLIPS + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT,"
                + KEY_FILENAME + " TEXT," + KEY_DURATION + " INT,"
                + KEY_CREATED + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
        db.execSQL(CREATE_CLIPS_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Nothing for now
    }


    // Audio clip handlers
    public void addAudioClip(AudioClip audioClip) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, audioClip.getName());
        values.put(KEY_FILENAME, audioClip.getFilename());
        values.put(KEY_DURATION, audioClip.getDuration());

        db.insert(TABLE_CLIPS, null, values);
        db.close();
    }

    public void deleteAudioClipById(Integer id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CLIPS, KEY_ID + " = ?",
                  new String[] { Integer.toString(id) });
        db.close();
    }

    public String getAudioClipFileName(int audioClipId) {
        String selectQuery = "SELECT " + KEY_FILENAME + " FROM " + TABLE_CLIPS
                             + " WHERE id=?";
        SQLiteDatabase db = this.getReadableDatabase();
        String[] params = new String[] { String.valueOf(audioClipId) };
        Cursor c = db.rawQuery(selectQuery, params);
        if (c.moveToFirst()) {
            return c.getString(0);
        }
        return null;
    }

    public Cursor getAudioClipCursor() {
        String selectQuery = "SELECT id, name, filename, duration, created, "
               + "id as _id FROM " + TABLE_CLIPS;
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(selectQuery, null);
    }

    public List<AudioClip> getAllAudioClips() {
        List<AudioClip> clipList = new ArrayList<AudioClip>();
        Cursor cursor = getAudioClipCursor();
        if (cursor.moveToFirst()) {
            do {
                clipList.add(extractAudioClip(cursor));
            } while (cursor.moveToNext());
        }
        return clipList;
    }

    private AudioClip extractAudioClip(Cursor cursor) {
        Date date;
        try {
            date = SQLITE_DATE_FORMAT.parse(cursor.getString(4));
        } catch (ParseException e) {
            // Log error
            date = null;
        }
        return new AudioClip(
                Integer.parseInt(cursor.getString(0)),
                cursor.getString(1),
                cursor.getString(2),
                Integer.parseInt(cursor.getString(3)),
                date);
    }
}
