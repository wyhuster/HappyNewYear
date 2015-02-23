package com.tonywang.happynewyear.utils;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.telephony.SmsManager;

import com.tonywang.happynewyear.ContactType;

public class SmsTools {

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
