package de.e621.rebane.SQLite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.sql.SQLXML;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

/* Diese Klasse sollte in der Hauptactivity instanziert werden: SQLiteDB sql = new SQLiteDB(this);
 * Danach muss die instanz mit .open() geöffnet und spätestens bei beenden der App mit .close()
 * wieder geschlossen werden.
 * Wenn die Tabelle NICHT mehr nur Key Value Paare enthält müssen die Funktionen setValue und
 * getValue angepasst werden, sodass z.B. statt nach einem Key mit einer SQL anweisung gesucht wird
 * und die Daten entsprechend in einem String[] oder Object[] zurückgegeben werden.
 *
 * Das Erstellen der Tabelle erfolg automatisch. Für genaure informationen über SQL queries gibt es
 * zahlreiche dokumentationen im Intener. z.B. von SQL selbst oder auch von php.net
 */

public class SQLiteDB {
	private SQLiteDatabase database;
	private SQLhelper dbHelper;

    /** Erzeugt eine Neue instanz. Es sollte immer nur eine Instanz gleichzeitig verwendet werden<br>
     * Jedoch ist das hohlen des Context über einen Konstruktor wesentlich angenehmer ;)<br>
     * <br>
     * Anschließen muss die Datenbank mit instanc.open() geöffnet und abschließend mit instance.close()<br>
     * wieder geschlossen werden.
     * */
	public SQLiteDB(Context context) {
		dbHelper = new SQLhelper(context);
	}

    /** Öffnet die Datenbank - nach der bearbeitung .close() nicht vergessen */
	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

    /** Schließt die datenbank.<br>Ich bin mir nicht sicher, ob überhaupt ein Fehler auftritt, wenn man
     * versucht, daten zu lesen wären die Datenbank geschlossen ist - einfach aufpassen :)
      */
	public void close() {
		dbHelper.close();
	}

    /** Speicher ein Key Value Paar.<br>Der Wert für Key wird überschrieben, fals der Key bereits existiert */
	public void setValue(String Key, String Value) {
		//See if key already exists
		Cursor cursor = database.query(SQLhelper.TABLE_NAME, new String[]{ "Key" } , "Key='" + Key + "'", null, null, null, null);
		Boolean got_result = cursor.moveToFirst();
		cursor.close();
		
		if (!got_result) { //Create Key
			ContentValues values = new ContentValues();
			values.put("VALUE", Value);
			values.put("KEY", Key);
			database.insert(SQLhelper.TABLE_NAME, null, values);
		} else { //Update Key
			ContentValues values = new ContentValues();
			values.put("VALUE", Value);
			database.update(SQLhelper.TABLE_NAME, values, "Key='" + Key + "'", null);
		}
	}

    /** Holt einen Wert aus der Datenbank.<br>Fals kein Key Value Paar existiert wird null zurück gegeben*/
	public String getValue(String Key) {
		Cursor cursor = database.query(SQLhelper.TABLE_NAME, new String[]{ "Value" } , "Key='" + Key + "'", null, null, null, null);
		if (!cursor.moveToFirst() || cursor.isNull(0)) { cursor.close(); return null; }
		String result = cursor.getString(0);
		
		cursor.close(); return result;
	}

    public String[] getStringArray(String key) {
        String raw = getValue(key);
        if (raw == null || raw.isEmpty()) return new String[]{};
        Logger.getLogger("SQLite").info("Getting " + key + " : " + raw);
        return raw.substring(2, raw.length()-2).split("\", \"");
    }
    public void setStringArray(String key, String... array) {
        String raw = "[\"";
        int l1 = array.length-1;
        for (int i = 0; i <= l1; i++) {
            raw += array[i];
            if (i != l1) raw += "\", \"";
        }
        raw += "\"]";
        Logger.getLogger("SQLite").info("Setting " + key + " : " + raw);
        setValue(key, raw);
    }
}
