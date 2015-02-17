package com.tonywang.happynewyear.tools;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.Contacts.People;
import android.telephony.SmsManager;
import android.util.Log;

import com.tonywang.happynewyear.ContactType;
import com.tonywang.happynewyear.TemplateSMS;
import com.tonywang.happynewyear.model.Contact;

public class Tools {
	/**
	 * 获取手机通讯录和sim卡上的联系人信息
	 * 
	 * @param context
	 * @return List<Contact>
	 */
	public static List<Contact> getAllContacts(Context context) {
		Set<String> phone_set = new HashSet<String>();
		List<Contact> arrayList = new ArrayList<Contact>();
		// 获取本机联系人
		Cursor cur = context.getContentResolver().query(
				ContactsContract.Contacts.CONTENT_URI,
				null,
				null,
				null,
				ContactsContract.Contacts.DISPLAY_NAME
						+ " COLLATE LOCALIZED ASC");
		if (cur != null && cur.moveToFirst()) {
			do {
				// 查看该联系人有多少个电话号码。如果没有这返回值为0
				int phoneCount = cur
						.getInt(cur
								.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
				if (phoneCount < 1) {
					continue;
				}

				Contact samContact = new Contact();
				int idColumn = cur
						.getColumnIndex(ContactsContract.Contacts._ID);
				int displayNameColumn = cur
						.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
				// 获得联系人的ID号
				String contactId = cur.getString(idColumn);
				// 获得联系人姓名
				String disPlayName = cur.getString(displayNameColumn);
				// System.out.println(disPlayName);
				samContact.setName(disPlayName);

				Cursor phones = context.getContentResolver().query(
						ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
						null,
						ContactsContract.CommonDataKinds.Phone.CONTACT_ID
								+ " = " + contactId, null, null);
				if (phones != null && phones.moveToFirst()) {
					do {
						// 遍历所有的电话号码
						String phoneNumber = phones
								.getString(phones
										.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
						int phoneType = phones
								.getInt(phones
										.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
						if (phoneType == ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE) {
							phoneNumber = phoneNumber.replace(" ", "");
							if (isPhone(phoneNumber)) {
								samContact.setPhone(phoneNumber);
								if (!phone_set.contains(phoneNumber)) {
									arrayList.add(samContact);
									phone_set.add(phoneNumber);
								}
							}
							break;
						}
					} while (phones.moveToNext());
					phones.close();
				}

			} while (cur.moveToNext());
			cur.close();
		}
		// 获取sim卡联系人
		Uri uri = Uri.parse("content://icc/adn");
		Cursor cur2 = context.getContentResolver().query(
				uri,
				null,
				null,
				null,
				ContactsContract.Contacts.DISPLAY_NAME
						+ " COLLATE LOCALIZED ASC");
		// System.out.println("contact num from sim card = " + cur2.getCount());
		// System.out.println("---------------");
		if (cur2 != null && cur2.moveToFirst()) {
			do {
				try {

					int displayNameColumn = cur2.getColumnIndex(People.NAME);
					int phoneColumn = cur2.getColumnIndex(People.NUMBER);
					String simName = cur2.getString(displayNameColumn);
					String simPhone = cur2.getString(phoneColumn);
					if (simName == null || simPhone == null) {
						continue;
					}
					simPhone = simPhone.replace(" ", "");
					if (isPhone(simPhone)) {
						if (!phone_set.contains(simPhone)) {
							Contact samContact = new Contact();
							samContact.setName(simName);
							samContact.setPhone(simPhone);
							arrayList.add(samContact);

							phone_set.add(simPhone);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			} while (cur2.moveToNext());
			cur2.close();
		}

		// Log.i("test", "ttttttt:" + arrayList.size());
		return arrayList;
	}

	/**
	 * 判断联系人的号码是否为手机
	 * 
	 * @param phone
	 * @return
	 */
	private static boolean isPhone(String phone) {
		if (phone.startsWith("+86"))
			return true;
		else if (phone.length() == 11 && phone.charAt(0) == '1') {
			return true;
		}
		return false;
	}

	/**
	 * 生成默认短信
	 * 
	 * @param type
	 * @param name
	 * @param template
	 * @return
	 */
	public static String generateSMS(int type, String name, String template) {
		StringBuffer sms = new StringBuffer();
		String xing = name.subSequence(0, 1).toString();
		switch (type) {
		case ContactType.TYPE_HEAD:
			sms.append(xing).append("总：").append(template).append("  王焱");
			break;
		case ContactType.TYPE_TEACHER:
			sms.append(xing).append("老师：").append(template).append("  王焱");
			break;
		case ContactType.TYPE_SENIOR_MALE:
			sms.append("师兄：").append(template).append("  王焱");
			break;
		case ContactType.TYPE_SENIOR_FEMALE:
			sms.append("师姐：").append(template).append("  王焱");
			break;
		case ContactType.TYPE_RELATIVE:
		case ContactType.TYPE_FAMILY:
			sms.append(template).append("  王焱");
			break;
		case ContactType.TYPE_FRIEND:
		case ContactType.TYPE_CLASSMATE:
		case ContactType.TYPE_OTHER:
			sms.append(name).append("：").append(template).append("  王焱");
			break;
		case ContactType.TYPE_IGNORE:
			break;
		default:
			break;
		}

		return sms.toString();
	}

	/**
	 * 直接发送短信
	 * 
	 * @param phone
	 * @param sms
	 */
	public static void sendSMS(String phone, String sms) {
		SmsManager smsManager = SmsManager.getDefault();
		// 切分短信，每七十个汉字切一个，不足七十就只有一个：返回的是字符串的List集合
		List<String> texts = smsManager.divideMessage(sms);
		if (texts == null) {
			return;
		}
		// 发送之前检查短信内容是否为空
		for (int i = 0; i < texts.size(); i++) {
			String text = texts.get(i);
			// smsManager.sendTextMessage(phone, null, text, null, null);
		}
	}

	/**
	 * 调用系统发送短信的界面
	 * 
	 * @param context
	 * @param phone
	 * @param sms
	 */
	public static void sendSMS(Context context, String phone, String sms) {
		Uri uri = Uri.parse("smsto://" + phone);
		Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
		intent.putExtra("sms_body", sms);
		context.startActivity(intent);
	}
}
