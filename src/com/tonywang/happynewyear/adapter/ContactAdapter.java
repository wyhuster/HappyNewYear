package com.tonywang.happynewyear.adapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.tonywang.happynewyear.R;
import com.tonywang.happynewyear.model.Contact;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

public class ContactAdapter extends BaseAdapter {
	private LayoutInflater mInflater;
	private List<Contact> contacts;
	private Set<Integer> checkedItems;

	// private Context mContext;

	public ContactAdapter(Context mContext, List<Contact> contacts) {
		// this.mContext = mContext;
		mInflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		if (contacts != null) {
			this.contacts = contacts;
		} else {
			this.contacts = new ArrayList<Contact>();
		}
		checkedItems = new HashSet<Integer>();
	}

	@Override
	public int getCount() {
		return contacts.size();
	}

	@Override
	public Contact getItem(int position) {
		return contacts.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public Set<Integer> getCheckedItem() {
		return checkedItems;
	}

	public void addAllCheckedItem() {
		for (int i = 0; i < contacts.size(); i++)
			checkedItems.add(i);
		this.notifyDataSetChanged();
	}

	public void clearCheckedItem() {
		checkedItems.clear();
		this.notifyDataSetChanged();
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View row = convertView;
		ViewHolder holder;

		if (row == null) {
			row = mInflater.inflate(R.layout.item_contact, parent, false);
			holder = new ViewHolder();
			holder.catalog = (TextView) row.findViewById(R.id.tv_catalog);
			holder.select = (CheckBox) row.findViewById(R.id.cb_select);
			holder.name = (TextView) row.findViewById(R.id.tv_name);
			holder.phone = (TextView) row.findViewById(R.id.tv_phone);
			row.setTag(holder);
		} else {
			holder = (ViewHolder) row.getTag();
		}

		String catalog = contacts.get(position).getPinyin().substring(0, 1)
				.toUpperCase();
		if (position == 0) {
			holder.catalog.setVisibility(View.VISIBLE);
			holder.catalog.setText(catalog);
		} else {
			String lastCatalog = contacts.get(position - 1).getPinyin()
					.substring(0, 1).toUpperCase();
			if (catalog.equals(lastCatalog)) {
				holder.catalog.setVisibility(View.GONE);
			} else {
				holder.catalog.setVisibility(View.VISIBLE);
				holder.catalog.setText(catalog);
			}
		}
		holder.select.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked) {
					checkedItems.add(position);
				} else {
					checkedItems.remove(position);
				}
			}
		});
		if (checkedItems.contains(position)) {
			holder.select.setChecked(true);
		} else {
			holder.select.setChecked(false);
		}

		Contact contact = contacts.get(position);
		holder.name.setText(contact.getName());
		holder.phone.setText(contact.getPhone());
		return row;
	}

	class ViewHolder {
		TextView catalog;
		CheckBox select;
		TextView name;
		TextView phone;
	}

}
