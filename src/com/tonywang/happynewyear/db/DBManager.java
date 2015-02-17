package com.tonywang.happynewyear.db;

import java.util.ArrayList;
import java.util.List;

import com.tonywang.happynewyear.model.Contact;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DBManager {
	private DBHelper helper;
	private SQLiteDatabase db;

	public DBManager(Context context) {
		helper = new DBHelper(context);
		// 因为getWritableDatabase内部调用了mContext.openOrCreateDatabase(mName, 0,
		// mFactory);
		// 所以要确保context已初始化,我们可以把实例化DBManager的步骤放在Activity的onCreate里
		db = helper.getWritableDatabase();
	}

	/**
	 * add persons
	 * 
	 * @param persons
	 */
	public void add(List<Contact> persons) {
		db.beginTransaction(); // 开始事务
		try {
			for (Contact person : persons) {
				db.execSQL("INSERT INTO contact VALUES(null, ?, ?, ?, ?)",
						new Object[] { person.getName(), person.getPhone(),
								person.getType(), person.getSms() });
			}
			db.setTransactionSuccessful(); // 设置事务成功完成
		} finally {
			db.endTransaction(); // 结束事务
		}
	}

	/**
	 * update person's type
	 * 
	 * @param person
	 */
	public boolean updateType(Contact person) {
		ContentValues cv = new ContentValues();
		cv.put("type", person.getType());
		return db.update("contact", cv, "phone = ?",
				new String[] { person.getPhone() }) > 0 ? true : false;
	}

	/**
	 * update person's sms
	 * 
	 * @param person
	 */
	public boolean updateSms(Contact person) {
		ContentValues cv = new ContentValues();
		cv.put("sms", person.getSms());
		return db.update("contact", cv, "phone = ?",
				new String[] { person.getPhone() }) > 0 ? true : false;
	}

	/**
	 * delete one person
	 * 
	 * @param person
	 */
	public void deletePerson(Contact person) {
		db.delete("contact", "phone = ?", new String[] { person.getPhone() });
	}

	/**
	 * delete all persons
	 * 
	 * @param person
	 */
	public void deleteAll() {
		db.execSQL("DELETE FROM contact");
		// db.delete("contact", "", new String[] {});
	}

	/**
	 * query all persons, return list
	 * 
	 * @return List<Person>
	 */
	public List<Contact> query() {
		ArrayList<Contact> persons = new ArrayList<Contact>();
		Cursor c = queryTheCursor();
		while (c.moveToNext()) {
			Contact person = new Contact();
			person.setName(c.getString(c.getColumnIndex("name")));
			person.setPhone(c.getString(c.getColumnIndex("phone")));
			person.setType(c.getInt(c.getColumnIndex("type")));
			person.setSms(c.getString(c.getColumnIndex("sms")));
			persons.add(person);
		}
		c.close();
		return persons;
	}

	/**
	 * query all persons, return cursor
	 * 
	 * @return Cursor
	 */
	private Cursor queryTheCursor() {
		Cursor c = db.rawQuery("SELECT * FROM contact", null);
		return c;
	}

	/**
	 * close database
	 */
	public void closeDB() {
		db.close();
	}
}
