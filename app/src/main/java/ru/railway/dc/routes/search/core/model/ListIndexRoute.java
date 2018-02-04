package ru.railway.dc.routes.search.core.model;

import java.util.ArrayList;

public class ListIndexRoute extends ArrayList<IndexRoute> {
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		for (int i = 0; i < size(); i++) {
			str.append("\n" + get(i));
		}
		return str.toString();
	}
}
