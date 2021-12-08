package jplag;


import java.util.Vector;
import java.util.Comparator;

public class SortedVector<T> extends Vector<T> {
    private static final long serialVersionUID = 1L;
    private final Comparator<T> cmp;
    
    public SortedVector(Comparator<T> comparator) {
        cmp = comparator;
    }

	private void insert(T obj, int index1, int index2) {
		int pos;
		int c;
		while (index1 <= index2) {
			pos = (index1 + index2) / 2;
			c = cmp.compare(obj, elementAt(pos - 1));
			if (c < 0) {
				index2 = pos - 1;
			} else if (c > 0) {
				index1 = pos + 1;
			} else {
				index1 = pos;
				index2 = -1;
			}
		}
		insertElementAt(obj, index1 - 1);
	}

	public synchronized void insert(T obj) {
		insert(obj, 1, elementCount);
	}
}
