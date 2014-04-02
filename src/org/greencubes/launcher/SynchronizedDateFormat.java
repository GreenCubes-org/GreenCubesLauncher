package org.greencubes.launcher;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.Date;

public class SynchronizedDateFormat extends DateFormat {

	private static final long serialVersionUID = 6840866068578980616L;
	private final DateFormat parent;

	public SynchronizedDateFormat(DateFormat parent) {
		this.parent = parent;
	}

	@Override
	public synchronized StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition fieldPosition) {
		return parent.format(date, toAppendTo, fieldPosition);
	}

	@Override
	public synchronized Date parse(String source, ParsePosition pos) {
		return parent.parse(source, pos);
	}

}
