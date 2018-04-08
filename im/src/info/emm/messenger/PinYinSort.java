package info.emm.messenger;

import info.emm.LocalData.DataAdapter;

import java.util.Comparator;

public class PinYinSort implements Comparator<DataAdapter>{
	@Override
	public int compare(DataAdapter lhs, DataAdapter rhs) {
		if (lhs.isUser && rhs.isUser) {	
			if (lhs.sortLetters.equals("@") || rhs.sortLetters.equals("#")) {
				return -1;
			} else if (lhs.sortLetters.equals("#") || rhs.sortLetters.equals("@")) {
				return 1;
			} else {
				return lhs.sortLetters.compareTo(rhs.sortLetters);
			}
		} else {
			return 0;
		}
	}
}
