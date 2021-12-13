package jplag;


import java.util.Vector;
import java.util.Comparator;

public class SortedVector<T> extends Vector<T> {
    private static final long serialVersionUID = 1L;
    private final Comparator<T> cmp;
    
    public SortedVector(Comparator<T> comparator) {
        cmp = comparator;
    }

	private void insert(T obj, int index2) {
		int pos;
		int c;
		int index1 = 1;
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

	public final synchronized void insert(T obj) {
		insert(obj, elementCount);
	}

	private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
		throw new java.io.NotSerializableException("jplag.SortedVector");
	}

	private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
		throw new java.io.NotSerializableException("jplag.SortedVector");
	}

	@Override
	public final synchronized SortedVector<T> clone() throws AssertionError {
		throw new AssertionError();
	}
}
