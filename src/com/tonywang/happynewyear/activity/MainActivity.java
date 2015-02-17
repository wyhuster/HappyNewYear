package com.tonywang.happynewyear.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.tonywang.happynewyear.ContactType;
import com.tonywang.happynewyear.R;
import com.tonywang.happynewyear.TemplateSMS;
import com.tonywang.happynewyear.adapter.ContactAdapter;
import com.tonywang.happynewyear.db.DBManager;
import com.tonywang.happynewyear.model.Contact;
import com.tonywang.happynewyear.tools.Tools;
import com.tonywang.happynewyear.tools.UpdateDialog;

import android.support.v7.app.ActionBarActivity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {

	private ListView lv_contacts;
	// private DBManager db;
	private GetContactsTask getContactsTask;
	// private GenerateSMSTask generateSMSTask;
	private SendSMSTask sendSMSTask;
	private ProgressDialog progressDialog;
	private final static int ACTION_REFRESH_CONTACT = 1;
	private final static int ACTION_SEND_SMS = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		lv_contacts = (ListView) findViewById(R.id.lv_contacts);

		progressDialog = new ProgressDialog(this);
		progressDialog.setCancelable(false);
		progressDialog.setMessage("读取联系人...");
		if (getContactsTask != null) {
			getContactsTask.cancel(true);
			getContactsTask = null;
		}
		getContactsTask = new GetContactsTask(this);
		getContactsTask.execute(false);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		switch (id) {
		case R.id.action_check:
			ContactAdapter adapter = (ContactAdapter) lv_contacts.getAdapter();
			if (adapter.getCheckedItem().size() == adapter.getCount()) {
				adapter.clearCheckedItem();
			} else {
				for (int i = 0; i < adapter.getCount(); i++)
					adapter.addAllCheckedItem();
			}
			return true;
		case R.id.action_refresh_sms:
			/*
			 * progressDialog.setMessage("生成默认短信..."); if (generateSMSTask !=
			 * null) { generateSMSTask.cancel(true); generateSMSTask = null; }
			 * generateSMSTask = new GenerateSMSTask(this);
			 * generateSMSTask.execute();
			 */
			return true;
		case R.id.action_send_sms:
			showDialog("是否确定发送短信?", ACTION_SEND_SMS);
			return true;
		case R.id.action_refresh_contacts:
			showDialog("是否刷新联系人?", ACTION_REFRESH_CONTACT);
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	private void showDialog(String message, final int action) {
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		builder.setTitle("确认");
		builder.setMessage(message);
		builder.setIcon(R.drawable.icon);
		builder.setPositiveButton("是", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (action == ACTION_REFRESH_CONTACT) {
					progressDialog.setMessage("刷新联系人...");
					if (getContactsTask != null) {
						getContactsTask.cancel(true);
						getContactsTask = null;
					}
					getContactsTask = new GetContactsTask(MainActivity.this);
					getContactsTask.execute(true);
				} else if (action == ACTION_SEND_SMS) {
					ContactAdapter adapter = (ContactAdapter) lv_contacts
							.getAdapter();
					Set<Integer> checkedItems = adapter.getCheckedItem();
					if (checkedItems.size() == 0) {
						Toast.makeText(getApplicationContext(), "请至少选择一个联系人",
								Toast.LENGTH_SHORT).show();
						return;
					}
					List<Contact> contacts_sms = new ArrayList<Contact>();
					for (int i : checkedItems) {
						contacts_sms.add(adapter.getItem(i));
					}
					progressDialog.setMessage("短信发送中...");
					if (sendSMSTask != null) {
						sendSMSTask.cancel(true);
						sendSMSTask = null;
					}
					sendSMSTask = new SendSMSTask();
					sendSMSTask.execute(contacts_sms);
				}
			}
		});
		builder.setNegativeButton("否", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

			}
		});
		builder.show();
	}

	private class GetContactsTask extends
			AsyncTask<Boolean, Void, List<Contact>> {

		private Context mContext;

		public GetContactsTask(Context mContext) {
			this.mContext = mContext;
		}

		@Override
		protected void onPreExecute() {
			progressDialog.show();
		}

		@Override
		protected List<Contact> doInBackground(Boolean... params) {
			DBManager db = new DBManager(mContext);
			boolean refresh = params[0];
			if (refresh) {
				db.deleteAll();
			}
			List<Contact> contacts = db.query();
			if (contacts == null || contacts.size() == 0) {
				contacts = Tools.getAllContacts(mContext);
				if (contacts == null)
					return null;
				// 生成默认短信
				List<Contact> contacts_insert = new ArrayList<Contact>();
				for (Contact contact : contacts) {
					int type = contact.getType();
					String name = contact.getName();
					String template_sms = TemplateSMS.sms_self_normal;
					if (type == ContactType.TYPE_CLASSMATE) {
						template_sms = TemplateSMS.sms_self_classmate_undergraduate;
					} else if (type == ContactType.TYPE_FRIEND) {
						template_sms = TemplateSMS.sms_self_friend;
					}
					String sms = Tools.generateSMS(type, name, template_sms);
					contact.setSms(sms);
					contacts_insert.add(contact);
				}
				db.add(contacts_insert);
				return contacts_insert;
			} else {
				return contacts;
			}
		}

		@Override
		protected void onPostExecute(final List<Contact> result) {
			ContactAdapter adapter = new ContactAdapter(mContext, result);
			lv_contacts.setAdapter(adapter);
			lv_contacts.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					UpdateDialog dialog = new UpdateDialog(mContext, result
							.get(position));
					dialog.show();
				}

			});
			progressDialog.dismiss();
		}
	}

	private class GenerateSMSTask extends AsyncTask<Void, Void, Void> {

		private Context mContext;

		public GenerateSMSTask(Context mContext) {
			this.mContext = mContext;
		}

		@Override
		protected void onPreExecute() {
			progressDialog.show();
		}

		@Override
		protected Void doInBackground(Void... params) {
			DBManager db = new DBManager(mContext);
			List<Contact> contacts = db.query();
			for (Contact contact : contacts) {
				int type = contact.getType();
				String name = contact.getName();
				String template_sms = TemplateSMS.sms_self_normal;
				if (type == ContactType.TYPE_CLASSMATE) {
					template_sms = TemplateSMS.sms_self_classmate_undergraduate;
				} else if (type == ContactType.TYPE_FRIEND) {
					template_sms = TemplateSMS.sms_self_friend;
				}
				String sms = Tools.generateSMS(type, name, template_sms);
				contact.setSms(sms);
				db.updateSms(contact);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			progressDialog.dismiss();
		}

	}

	private class SendSMSTask extends AsyncTask<List<Contact>, Void, Void> {

		@Override
		protected void onPreExecute() {
			progressDialog.show();
		}

		@Override
		protected Void doInBackground(List<Contact>... params) {
			List<Contact> contacts = params[0];
			for (Contact contact : contacts) {
				String name = contact.getName();
				//progressDialog.setMessage("发送至：" + name + " ...");
				String phone = contact.getPhone();
				String sms = contact.getSms();
				// Tools.sendSMS(phone, sms);
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			progressDialog.dismiss();
		}
	}
}
