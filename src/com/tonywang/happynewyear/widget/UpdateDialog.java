package com.tonywang.happynewyear.widget;

import java.util.HashMap;
import java.util.Map;

import com.tonywang.happynewyear.ContactType;
import com.tonywang.happynewyear.R;
import com.tonywang.happynewyear.db.DBManager;
import com.tonywang.happynewyear.model.Contact;
import com.tonywang.happynewyear.utils.ContactTools;
import com.tonywang.happynewyear.utils.SmsTools;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

public class UpdateDialog {

	private Context context;
	private Contact contact;
	private EditText et_sms;
	private Button btn_cancel;
	private Button btn_update;
	private Button btn_sendsms;

	private int[] array_radio = { R.id.radio0, R.id.radio1, R.id.radio2,
			R.id.radio3, R.id.radio4, R.id.radio5, R.id.radio6, R.id.radio7,
			R.id.radio8, R.id.radio9 };
	private int[] array_type = { ContactType.TYPE_HEAD,
			ContactType.TYPE_TEACHER, ContactType.TYPE_SENIOR_MALE,
			ContactType.TYPE_SENIOR_FEMALE, ContactType.TYPE_RELATIVE,
			ContactType.TYPE_FAMILY, ContactType.TYPE_FRIEND,
			ContactType.TYPE_CLASSMATE, ContactType.TYPE_OTHER,
			ContactType.TYPE_IGNORE };

	public UpdateDialog(Context context, Contact contact) {
		this.context = context;
		this.contact = contact;
	}

	/**
	 * show dialog
	 */
	public void show() {
		LayoutInflater inflaterDl = LayoutInflater.from(context);
		View layout = inflaterDl.inflate(R.layout.select_type_sms, null);

		Dialog dialog = new Dialog(context);

		dialog.setTitle(contact.getName() + " " + contact.getPhone());
		dialog.setContentView(layout);

		display(layout, contact, dialog);
		dialog.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {

			}

		});
		dialog.show();
	}

	/**
	 * display content on dialog
	 * 
	 * @param layout
	 * @param contact
	 * @param dialog
	 */
	private void display(View layout, final Contact contact, final Dialog dialog) {
		et_sms = (EditText) layout.findViewById(R.id.et_sms);
		et_sms.setText(contact.getSms());
		btn_cancel = (Button) layout.findViewById(R.id.btn_cancel);
		btn_cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});

		// set radiobutton checked
		int radio_id = R.id.radio8;
		for (int i = 0; i < array_type.length; i++) {
			if (contact.getType() == array_type[i]) {
				radio_id = array_radio[i];
				break;
			}
		}
		RadioButton radiobutton = (RadioButton) layout.findViewById(radio_id);
		radiobutton.setChecked(true);

		final MyRadioGroup rg = (MyRadioGroup) layout
				.findViewById(R.id.radioGroup1);
		btn_update = (Button) layout.findViewById(R.id.btn_update);
		btn_update.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				int type = contact.getType();
				for (int i = 0; i < array_radio.length; i++) {
					if (rg.getCheckedRadioButtonId() == array_radio[i]) {
						type = array_type[i];
						break;
					}
				}

				DBManager db = new DBManager(context);
				boolean isupdate = false;
				if (type != contact.getType()) {
					contact.setType(type);
					isupdate = db.updateType(contact);
				}

				String update_sms = et_sms.getText().toString();
				if (!update_sms.equals(contact.getSms())) {
					contact.setSms(update_sms);
					isupdate = db.updateSms(contact);
				}

				if (isupdate)
					Toast.makeText(context, "修改成功", Toast.LENGTH_SHORT).show();
				dialog.dismiss();
			}

		});

		btn_sendsms = (Button) layout.findViewById(R.id.btn_sendsms);
		btn_sendsms.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String phone = contact.getPhone();
				String sms = et_sms.getText().toString();
				SmsTools.sendSMS(context, phone, sms);
			}
		});
	}

}
