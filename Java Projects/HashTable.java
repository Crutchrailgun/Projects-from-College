package tables;

import java.security.InvalidKeyException;
import java.util.Iterator;
import java.util.List;

import model.DataTable;
import model.Row;
import model.Table;

public class HashTable implements DataTable {
	private Row[] rows;
	private List<String> columns;
	private String name;
	private int size;
	private int degree;
	private int fingerprint = 0;
	private static final long FNV_64_PRIME = 0x100000001b3L;
	private static final long FNV_64_INIT = 0xcbf29ce484222325L;

	private static final Row SENTINEL = new Row(null, null);

	int cap = 32;

	public HashTable(String name, List<String> columns) {
		this.name = name;
		this.columns = columns;
		clear();
	
	}

	@Override
	public void clear() {
		rows = new Row[cap];
		size = 0;
		fingerprint = 0;
	}

	public void rehash() throws InvalidKeyException {
		// let backup = old array ref
		// new empty array * 2
		cap = cap * 2;
		Row[] oldrows = rows;
		rows = new Row[cap];
		fingerprint = 0;
		size = 0;
		for (int i = 0; i < oldrows.length; i++) {
			if (oldrows[i] != null && oldrows[i] != SENTINEL) {
				put(oldrows[i].key(), oldrows[i].fields());

			}
		}

	}

	private long doubleHash(String key) {
		// hashA + hashB
		{
			key += "Josiah";
			byte[] data = key.getBytes();
			long hash = FNV_64_INIT;
			for (byte b : data) {
				hash ^= b;
				hash *= FNV_64_PRIME;
				
			}
			hash = hash *2 +1;
			return Math.abs((int) hash) % cap;
//			int hash = key.hashCode();
//	hash = (hash * 2) + 1;
////
//	return Math.floorMod(hash, cap - 1) + 1;

		}
	}

	private int hashFunction(String key) {
//		salt KEY
		key = key + "Josiah";
		final int m = 10000007;
		int hashSoFar = 0;
		final char[] s = key.toCharArray();
		long p_pow = 1;
		for (int i = 0; i < key.length(); i++) {
			hashSoFar = (int) ((hashSoFar + (s[i] - 'a' + 1) * p_pow) % m);
			p_pow = (p_pow * 31 % m);
		}
		int hash = hashSoFar;
		// int hash = key.hashCode();

		return hash;

		// PLACEHOLDER

	}

	@Override
	public List<Object> put(String key, List<Object> fields) {
		int memoryIndex = -1;
		int h = hashFunction(key);
		int c = (int) doubleHash(key);
		int i = 0;
		Row New_Value = new Row(key, fields);
		if (key == null) {
			throw new IllegalArgumentException("Invalid Key.");
		}
		if (fields.size() != columns.size() - 1) {
			throw new IllegalArgumentException();
		}

		for (int j = 0; j < cap; j++) {
			int Q = h + j * c;

			i = Math.floorMod(Q, cap);

			// CHECK FOR MISS AND HIT
			// MISS -> MISS
			// HIT -> HIT
			// KEEP LOOPING,
			// throw an IllegalStateExpection
			// c = result hash2 of key, the adjustment

			if (rows[i] == null) { // MISS
				if (memoryIndex > 0) {
					rows[memoryIndex] = New_Value;
					fingerprint += rows[memoryIndex].hashCode();
				} else {
					rows[i] = New_Value;
					fingerprint += rows[i].hashCode();

				}
				if (rows[i] == SENTINEL) {
					memoryIndex = i;
				}

				size++;
				if (this.loadFactor() > 0.75) {
					try {
						rehash();
					} catch (InvalidKeyException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				return null;

			}
			if (rows[i] != null && rows[i].key() == key) {

				// if (rows[i] == SENTINEL) { //HIT

				Row temp = rows[i];
				fingerprint -= rows[i].hashCode();
				rows[i] = New_Value;
				fingerprint += rows[i].hashCode();

				return temp.fields();

			}
		}
		// }
		return null;
	}

	@Override
	public List<Object> get(String key) {
		int h = hashFunction(key);
		int c = (int) doubleHash(key);
		int i = 0;

		for (int j = 0; j < cap; j++) {
			int Q = h + j * c;

			i = Math.floorMod(Q, cap);

			if (rows[i] == null) {
				return null;
			}
			if (rows[i] != null && rows[i].key() == key) {
				return rows[i].fields();
			}

		}
		return null;
	}

	// throw new UnsupportedOperationException("Implement get for Module 2");

	@Override
	public List<Object> remove(String key) {
		// SENTINEL ISNT VALID OR NULL
		// new Row(null, ...)
		// or new Object()
		int h = hashFunction(key);
		int c = (int) doubleHash(key);
		int i = 0;
		for (int j = 0; j < cap; j++) {
			int Q = h + j * c;

			i = Math.floorMod(Q, cap);

			if (rows[i] == null) {

				return null;
			}
			if (rows[i].key() == key && rows[i] != null) {
				List<Object> temp = rows[i].fields();
				fingerprint -= rows[i].hashCode();
				rows[i] = SENTINEL;
				size--;

				return temp;
			}

		}
		return null;
		// throw new UnsupportedOperationException("Implement remove for Module 2");
	}

	@Override
	public int degree() {
		degree = columns.size();
		return degree;
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public int capacity() {
		return cap;
	}

	@Override
	public int hashCode() {
		return fingerprint;

	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Table) {
			if (obj.hashCode() == fingerprint) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Iterator<Row> iterator() {
		return new Iterator<>() {
			private int currentIndex = 0;

			@Override
			public boolean hasNext() {
				while (currentIndex < rows.length && (rows[currentIndex] == null || rows[currentIndex] == SENTINEL)) {
					currentIndex++;
				}
				return currentIndex < rows.length;
			}

			@Override
			public Row next() {
				return rows[currentIndex++];
			}
		};
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public List<String> columns() {
		return columns;
	}

	@Override
	public String toString() {
		return toPrettyString();
		// throw new UnsupportedOperationException("Implement toString for Module 2");
	}
}
