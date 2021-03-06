package strength.history.data.db;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import strength.history.data.structure.SyncBase;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Base class for DB helpers
 * 
 * @param <E>
 *            Structure class they handle
 */
public abstract class DBHelperBase<E extends SyncBase<E>> extends
		SQLiteOpenHelper {

	/**
	 * Constructor
	 * 
	 * @param context
	 * @param name
	 *            Name of the DB file
	 * @param version
	 *            Version of the DB
	 */
	public DBHelperBase(Context context, String name, int version) {
		super(context, name, null, version);
	}

	/**
	 * Converts it to content values
	 * 
	 * @param e
	 *            Object to convert
	 * @return The content values
	 */
	protected abstract ContentValues toContentValues(E e);

	protected abstract String getDBFileName();

	/**
	 * Deletes it from the DB
	 * 
	 * @param e
	 *            Object to delete
	 * @return True if objects was deleted
	 */
	public abstract boolean delete(E e);

	/**
	 * Inserts it into the DB
	 * 
	 * @param e
	 *            Object to insert
	 * @return True if object was inserted
	 */
	public abstract boolean insert(E e);

	public abstract void purge();

	/**
	 * Makes a query to the DB
	 * 
	 * @param offset
	 *            Limit offset
	 * @param limit
	 *            Limit
	 * @return The list of resulting items
	 */
	public abstract ArrayList<E> query(int offset, int limit);

	/**
	 * Updates it in the DB
	 * 
	 * @param e
	 *            Object to update
	 * @return True if objects was updated
	 */
	public abstract boolean update(E e);

	@Override
	public abstract void onCreate(SQLiteDatabase db);

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.e(this.getClass().getSimpleName(), "onUpgrade not supported");
	}

	public final void createBackup(Context context, ZipOutputStream backupFile,
			byte[] buffer) throws IOException {
		String fileName = getDBFileName();
		File file = context.getDatabasePath(fileName);
		if (file.exists()) {
			FileInputStream fis = new FileInputStream(file);
			ZipEntry entry = new ZipEntry(fileName);
			backupFile.putNextEntry(entry);
			int count;
			while ((count = fis.read(buffer)) != -1) {
				backupFile.write(buffer, 0, count);
			}
			backupFile.closeEntry();
			fis.close();
		}
	}

	public final void importBackup(Context context, ZipInputStream importFile,
			byte[] buffer) throws IOException {
		close();
		String fileName = getDBFileName();
		File file = context.getDatabasePath(fileName);
		FileOutputStream fos = new FileOutputStream(file);
		int count;
		while ((count = importFile.read(buffer)) != -1) {
			fos.write(buffer, 0, count);
		}
		fos.close();
	}
}
