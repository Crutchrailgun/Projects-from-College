package tables;

import java.util.Iterator;
import java.util.List;

import model.DataTable;
import model.Row;
import model.Table;

public class SymbolTable implements DataTable {
//Code done by Josiah Fout, 9.12.24
	private Row[] rows;
	private List<String> columns;
	private String name;
	private int size;
	private int degree;
	private int fingerprint = 0;
	int cap = 52;

	public SymbolTable(String name, List<String> columns) {

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

	@Override
	public List<Object> put(String key, List<Object> fields) {
		// Direct address and accounting for uppercases

		// guard CONDITIONS
		if (key == null || key.isEmpty()) {
			throw new IllegalArgumentException("key is invalid or empty");
		}
		if (fields.size() != columns().size() - 1) {
			throw new IllegalArgumentException("Fields are out of bounds.");
		}

		// if hit, update old fields, return old

		if (rows[indexOf(key)] != null) { // HIT
			// UPDATE
			List<Object> temp = rows[indexOf(key)].fields();
			fingerprint-= rows[indexOf(key)].hashCode();
			rows[indexOf(key)] = new Row(key, fields);

			// For Testing
			
			fingerprint+= rows[indexOf(key)].hashCode();

			return temp;

		} else {
			// if MISS, create new row for adding key and fields

			rows[indexOf(key)] = new Row(key, fields);
			size++;
			// hashcode

			fingerprint +=rows[indexOf(key)].hashCode();
			return null;
		}

	}

	// create row, increase fingerprint by new row hash
	// hit, decrease by old, increase by new.

	@Override
	public List<Object> get(String key) {
		// Direct address and accounting for uppercases

		// if hit, return old fields
		if (rows[indexOf(key)] != null) {

			return rows[indexOf(key)].fields();
		} else {
			// if miss

			return null;
		}

	}

	@Override
	public List<Object> remove(String key) {
		// Direct address and accounting for uppercases
		if (rows[indexOf(key)] != null) {

			List<Object> temp = rows[indexOf(key)].fields();
			fingerprint -= rows[indexOf(key)].hashCode();
			rows[indexOf(key)] = null;
			size--;
			
			// on hit, decrease hash by rows hash

			return temp;
		} else {
			// MISS

			return null;

		}

	}

	// HELPER METHOD INDEX// Direct Address
	public int indexOf(String key) {
		int index = key.charAt(0) - 'A';
		if (index > 25) {
			index = index - 6;
		}

		return index;
	}

	@Override
	public int degree() {
		// DEGREE IS TOTAL NUMBER OF COLUMNS
		degree = columns.size();
		return degree;

	}

	@Override
	public int size() {
		// SIZE IS CURRENT NUMBER OF ROWS
		return size;

	}

	@Override
	public int capacity() {
		// Max number of rows, alphabetically lower and upper 52 max.
		return cap;

	}

	@Override
	public int hashCode() {
		
//	
		return fingerprint;

	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Table) {
			if(obj.hashCode() == fingerprint) {
				return true;
			}
		}
		return false;
		
			

	}

	@Override
	public Iterator<Row> iterator() {
		return new Iterator<>() {
			// initialization (private)
			private int currentIndex = 0;

			@Override
			public boolean hasNext() {
				while (currentIndex < rows.length && rows[currentIndex] == null) {
					currentIndex++;
				}
				return currentIndex < rows.length;

				// count++ until CONDITION

				// differs based on 2 designs

				// return NEXT

			}

			@Override
			public Row next() {
				if (!hasNext()) {
					//throw new java.util.NoSuchElementException();
				}
				// uses the element by returning it
				// progress step
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

	}
}
