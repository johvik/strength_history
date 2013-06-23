package strength.history.data.db;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import strength.history.data.db.entry.NameColumn;
import strength.history.data.db.entry.SyncColumns;
import strength.history.data.structure.Exercise;
import strength.history.data.structure.Exercise.MuscleGroup;

/**
 * DB helper for Exercise
 */
public class ExerciseDBHelper extends DBHelperBase<Exercise> {
	private interface Entry extends BaseColumns, NameColumn, SyncColumns {
		static final String TABLE_NAME = "exercise";
		static final String MUSCLE_GROUP = "muscle_group";
		static final String[] ALL_COLUMNS = new String[] { _ID, SYNC, NAME,
				MUSCLE_GROUP };
	}

	private static final String DATABASE_NAME = "exercise.db";
	private static final int DATABASE_VERSION = 1;

	/**
	 * Singleton instance
	 */
	private static ExerciseDBHelper instance = null;

	private ExerciseDBHelper(Context context) {
		super(context, DATABASE_NAME, DATABASE_VERSION);
	}

	/**
	 * Static access to the instance
	 * 
	 * @param context
	 * @return The instance
	 */
	public static ExerciseDBHelper getInstance(Context context) {
		// Singleton pattern
		if (instance == null) {
			synchronized (ExerciseDBHelper.class) {
				if (instance == null) {
					instance = new ExerciseDBHelper(context);
				}
			}
		}
		return instance;
	}

	@Override
	protected ContentValues toContentValues(Exercise e) {
		ContentValues values = new ContentValues();
		values.put(Entry.SYNC, e.getSync());
		values.put(Entry.NAME, e.getName());
		values.put(Entry.MUSCLE_GROUP, e.getMuscleGroup().ordinal());

		return values;
	}

	@Override
	public boolean delete(Exercise e) {
		SQLiteDatabase db = instance.getWritableDatabase();
		int rows = db.delete(Entry.TABLE_NAME, Entry._ID + "=?",
				new String[] { Long.toString(e.getId()) });
		db.close();
		return rows != 0;
	}

	private static boolean insert(SQLiteDatabase db, Exercise e) {
		long id = db
				.insert(Entry.TABLE_NAME, null, instance.toContentValues(e));
		e.setId(id);
		return id != -1;
	}

	@Override
	public boolean insert(Exercise e) {
		SQLiteDatabase db = instance.getWritableDatabase();
		boolean inserted = insert(db, e);
		db.close();
		return inserted;
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
	public ArrayList<Exercise> query(int offset, int limit) {
		ArrayList<Exercise> res = new ArrayList<Exercise>();
		SQLiteDatabase db = instance.getReadableDatabase();
		Cursor cursor = db.query(Entry.TABLE_NAME, Entry.ALL_COLUMNS, null,
				null, null, null, null, offset + ", " + limit);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			long id = cursor.getLong(0);
			long sync = cursor.getLong(1);
			String name = cursor.getString(2);
			MuscleGroup muscleGroup = MuscleGroup.parse(cursor.getInt(3));
			res.add(new Exercise(id, sync, name, muscleGroup));
			cursor.moveToNext();
		}
		cursor.close();
		db.close();
		return res;
	}

	@Override
	public boolean update(Exercise e) {
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
				+ " INTEGER NOT NULL, " + Entry.NAME + " TEXT NOT NULL, "
				+ Entry.MUSCLE_GROUP + " INTEGER NOT NULL);");
	}

	public ArrayList<Exercise> createDefaults() {
		// Unsure of the muscle groups :)
		ArrayList<Exercise> res = new ArrayList<Exercise>();
		res.add(new Exercise("Barbell Curl", MuscleGroup.ARMS));
		res.add(new Exercise("Dumbbell Curl", MuscleGroup.ARMS));
		res.add(new Exercise("Barbell Tricep Press", MuscleGroup.ARMS));
		res.add(new Exercise("Dumbbell Tricep Press", MuscleGroup.ARMS));
		res.add(new Exercise("Overhead Press", MuscleGroup.SHOULDERS));
		res.add(new Exercise("Wrist Curl", MuscleGroup.ARMS));
		res.add(new Exercise("Tricep Kickback", MuscleGroup.ARMS));
		res.add(new Exercise("Bench Press", MuscleGroup.CHEST));
		res.add(new Exercise("Cable Crossover", MuscleGroup.CHEST));
		res.add(new Exercise("Dumbbell Fly", MuscleGroup.CHEST));
		res.add(new Exercise("Incline Bench", MuscleGroup.CHEST));
		res.add(new Exercise("Dips", MuscleGroup.ARMS));
		res.add(new Exercise("Pushup", MuscleGroup.CHEST));
		res.add(new Exercise("Pullup", MuscleGroup.BACK));
		res.add(new Exercise("Back Raise", MuscleGroup.BACK));
		res.add(new Exercise("Bent-Over Row", MuscleGroup.BACK));
		res.add(new Exercise("Seated Row", MuscleGroup.BACK));
		res.add(new Exercise("Chinup", MuscleGroup.BACK));
		res.add(new Exercise("Lat Pulldown", MuscleGroup.BACK));
		res.add(new Exercise("Seated Reverse Fly", MuscleGroup.BACK));
		res.add(new Exercise("Military Press", MuscleGroup.SHOULDERS));
		res.add(new Exercise("Upright Row", MuscleGroup.BACK));
		res.add(new Exercise("Front Raise", MuscleGroup.BACK));
		res.add(new Exercise("Side Lateral Raise", MuscleGroup.ABS));
		res.add(new Exercise("Snatch", MuscleGroup.ARMS));
		res.add(new Exercise("Push Press", MuscleGroup.ARMS));
		res.add(new Exercise("Shrug", MuscleGroup.SHOULDERS));
		res.add(new Exercise("Crunch Machine", MuscleGroup.ABS));
		res.add(new Exercise("Crunch", MuscleGroup.ABS));
		res.add(new Exercise("Ab Twist", MuscleGroup.ABS));
		res.add(new Exercise("Bicycle Kick", MuscleGroup.ABS));
		res.add(new Exercise("Hanging Leg Raise", MuscleGroup.ABS));
		res.add(new Exercise("Hanging Knee Raise", MuscleGroup.ABS));
		res.add(new Exercise("Reverse Crunch", MuscleGroup.ABS));
		res.add(new Exercise("V Up", MuscleGroup.ABS));
		res.add(new Exercise("Situp", MuscleGroup.ABS));
		res.add(new Exercise("Squat", MuscleGroup.LEGS));
		res.add(new Exercise("Lunge", MuscleGroup.LEGS));
		res.add(new Exercise("Dead Lift", MuscleGroup.LEGS));
		res.add(new Exercise("Hamstring Curl", MuscleGroup.LEGS));
		res.add(new Exercise("Good Morning", MuscleGroup.BACK));
		res.add(new Exercise("Clean", MuscleGroup.LEGS));
		res.add(new Exercise("Leg Press", MuscleGroup.LEGS));
		res.add(new Exercise("Leg Extension", MuscleGroup.LEGS));

		for (Exercise e : res) {
			insert(e);
		}
		return res;
	}
}
