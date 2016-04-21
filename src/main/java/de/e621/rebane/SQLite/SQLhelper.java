package de.e621.rebane.SQLite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/* Diese Klasse dient dazu, die SQL tabelle vorzugereiten.
 * Fals kein KEY VALUE paar in einer "settings" tabelle gespeichert werden soll, sondern z.B.
 * Kontakte kann der String z.B. TABLE_NAME in "Kontakte" geändert werden und der Wert
 * QUERY_TABLE_CREATE so aussehen: CREATE TABLE (Vorname TEXT, Nachname TEXT, Telephon INT16, Kommentar TEXT);
 *
 * Verwendet werden sollte diese Klasse ausschließlich von der SQLiteDB Klasse
 */

public class SQLhelper extends SQLiteOpenHelper {
	private static final int DATABASE_VERSION = 2;
    static final String TABLE_NAME = "settings";
    private static final String QUERY_TABLE_CREATE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                "Key TEXT, " +
                "Value TEXT);";

    SQLhelper(Context context) {
        super(context, "ApplicationDatabase", null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(QUERY_TABLE_CREATE);
    }

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}
}
