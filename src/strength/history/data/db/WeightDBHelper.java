package strength.history.data.db;

import java.util.ArrayList;

import strength.history.data.db.entry.SyncColumns;
import strength.history.data.db.entry.TimeColumn;
import strength.history.data.db.entry.WeightColumn;
import strength.history.data.structure.Weight;
import strength.history.data.structure.SyncBase.State;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

/**
 * DB helper for Weight
 */
public class WeightDBHelper extends DBHelperBase<Weight> {
	private interface Entry extends BaseColumns, TimeColumn, WeightColumn,
			SyncColumns {
		static final String TABLE_NAME = "weight";
		static final String[] ALL_COLUMNS = new String[] { _ID, SYNC, TIME,
				WEIGHT };
	}

	public static final String DATABASE_NAME = "weight.db";
	private static final int DATABASE_VERSION = 1;

	/**
	 * Singleton instance
	 */
	private static WeightDBHelper instance = null;

	private WeightDBHelper(Context context) {
		super(context, DATABASE_NAME, DATABASE_VERSION);
	}

	/**
	 * Static access to the instance
	 * 
	 * @param context
	 * @return The instance
	 */
	public static WeightDBHelper getInstance(Context context) {
		// Singleton pattern
		if (instance == null) {
			synchronized (WeightDBHelper.class) {
				if (instance == null) {
					instance = new WeightDBHelper(context);
				}
			}
		}
		return instance;
	}

	@Override
	protected ContentValues toContentValues(Weight e) {
		ContentValues values = new ContentValues();
		values.put(Entry.TIME, e.getTime());
		values.put(Entry.SYNC, e.getSync());
		values.put(Entry.WEIGHT, e.getWeight());

		return values;
	}

	@Override
	protected String getDBFileName() {
		return DATABASE_NAME;
	}

	@Override
	public boolean delete(Weight e) {
		SQLiteDatabase db = instance.getWritableDatabase();
		int rows = db.delete(Entry.TABLE_NAME, Entry._ID + "=?",
				new String[] { Long.toString(e.getId()) });
		db.close();
		return rows != 0;
	}

	@Override
	public boolean insert(Weight e) {
		SQLiteDatabase db = instance.getWritableDatabase();
		long id = db
				.insert(Entry.TABLE_NAME, null, instance.toContentValues(e));
		e.setId(id);
		db.close();
		return id != -1;
	}

	/**
	 * 
	 * @return null if nothing was found
	 */
	public Weight latest() {
		Weight res = null;
		SQLiteDatabase db = instance.getReadableDatabase();

		Cursor cursor = db.query(Entry.TABLE_NAME, Entry.ALL_COLUMNS, null,
				null, null, null, Entry.TIME + " desc", "1");

		if (cursor.moveToFirst()) {
			long id = cursor.getLong(0);
			long sync = cursor.getLong(1);
			long time = cursor.getLong(2);
			double weight = cursor.getDouble(3);
			// TODO Add serverId and state to the db
			String serverId = "";
			State state = State.NEW;
			res = new Weight(id, sync, serverId, state, time, weight);
		}
		cursor.close();
		db.close();
		return res;
	}

	@Override
	public void purge() {
		SQLiteDatabase db = instance.getWritableDatabase();
		db.beginTransaction();
		try {
			db.execSQL("DROP TABLE IF EXISTS " + Entry.TABLE_NAME);
			onCreate(db);
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		db.close();
	}

	@Override
	public ArrayList<Weight> query(int offset, int limit) {
		ArrayList<Weight> res = new ArrayList<Weight>();
		SQLiteDatabase db = instance.getReadableDatabase();
		Cursor cursor = db.query(Entry.TABLE_NAME, Entry.ALL_COLUMNS, null,
				null, null, null, null, offset + ", " + limit);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			long id = cursor.getLong(0);
			long sync = cursor.getLong(1);
			long time = cursor.getLong(2);
			double weight = cursor.getDouble(3);
			// TODO Add serverId and state to the db
			String serverId = "";
			State state = State.NEW;
			res.add(new Weight(id, sync, serverId, state, time, weight));
			cursor.moveToNext();
		}
		cursor.close();
		db.close();
		return res;
	}

	@Override
	public boolean update(Weight e) {
		SQLiteDatabase db = instance.getWritableDatabase();
		int rows = db.update(Entry.TABLE_NAME, instance.toContentValues(e),
				Entry._ID + "=?", new String[] { Long.toString(e.getId()) });
		db.close();
		return rows != 0;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + Entry.TABLE_NAME + " (" + Entry._ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + Entry.SYNC
				+ " INTEGER NOT NULL, " + Entry.TIME + " INTEGER NOT NULL, "
				+ Entry.WEIGHT + " REAL NOT NULL);");
	}
}
