package com.tonywang.happynewyear.utils;

import java.util.Comparator;

import com.tonywang.happynewyear.model.Contact;

public class ContactComparator implements Comparator<Contact> {

	@Override
	public int compare(Contact lhs, Contact rhs) {

		String py1 = lhs.getPinyin();
		String py2 = rhs.getPinyin();
		return py1.compareTo(py2);
	}

}
