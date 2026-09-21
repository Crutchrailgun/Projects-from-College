
package tables;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.Iterator;
import java.util.List;

import model.FileTable;
import model.Row;
import model.Table;

public class BinaryTable implements FileTable {
	private static final Path base = Paths.get("db", "tables");

	private final Path rootDir;

	public BinaryTable(String name, List<String> columns) {
		try {
			rootDir = base.resolve(name);
			Files.createDirectories(rootDir);

			Path metadata = rootDir.resolve("metadata");
			Files.createDirectories(metadata);

			Path data = rootDir.resolve("data");
			Files.createDirectories(data);

			// db/tables/tablename/metadata/columns
			Path columnsName = rootDir.resolve("metadata").resolve("columns.bin");
			if (Files.notExists(columnsName)) {
				Files.createFile(columnsName);
			}

			DataOutputStream column = new DataOutputStream(Files.newOutputStream(columnsName));
			for (String field : columns) {
				// System.out.println("FIELDS: " + field);
				column.writeUTF(field);
			}
//			
			column.close();
			clear();

		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	public BinaryTable(String name) {
		rootDir = base.resolve(name);

		if (Files.notExists(rootDir)) {
			throw new IllegalArgumentException("Missing table: " + name);
		}
	}

	@Override
	public void clear() {
		try {
			var data = rootDir.resolve("data");
			Files.walk(data).skip(1).sorted(Comparator.reverseOrder()).forEach(path -> path.toFile().delete());

			Path size = rootDir.resolve("metadata").resolve("size.bin");
			if (Files.notExists(size)) {
				Files.createFile(size);
			}

			var size1 = new DataOutputStream(new FileOutputStream(size.toFile()));
			size1.writeInt(0);
			size1.close();

			// metadata/fingerprint/0
			Path fingerprint = rootDir.resolve("metadata").resolve("fingerprint.bin");
			if (Files.notExists(fingerprint)) {
				Files.createFile(fingerprint);
			}
			var printer = new DataOutputStream(new FileOutputStream(fingerprint.toFile()));
			printer.writeInt(0);
			printer.close();

		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		// size = 0;
		// fingerprint = 0;
	}

	public void changeSize(String symbol) throws IOException {
		var size = rootDir.resolve("metadata").resolve("size.bin");
		var size_in = new DataInputStream(new FileInputStream(size.toFile()));
		var size_number = size_in.readInt();

		size_in.close();

		if (symbol.equals("-")) {
			size_number--;
		} else if (symbol.equals("+")) {
			size_number++;
			// System.out.println("IN METHOD: " + size_number);
		} else {
			throw new RuntimeException();
		}

		var size_out = new DataOutputStream(new FileOutputStream(size.toFile()));
		size_out.writeInt(size_number);
		size_out.close();

	}

	public void changeHC(String symbol, Row row) throws IOException {
		try {
			var fingerprint = rootDir.resolve("metadata").resolve("fingerprint.bin");
			var hash_in = new DataInputStream(new FileInputStream(fingerprint.toFile()));
			var hashcode = hash_in.readInt();

//		hash_in.close();

			if (symbol.equals("+")) {
				hashcode += row.hashCode();

			} else if (symbol.equals("-")) {
				hashcode -= row.hashCode();

			}
			hash_in.close();

			// System.out.println("IN METHOD: " + hashcode);
			var hash_out = new DataOutputStream(new FileOutputStream(fingerprint.toFile()));
			hash_out.writeInt(hashcode);
			hash_out.close();
		}

		catch (IOException e) {
			throw new IllegalArgumentException(e);
		}

	}

	public void rowWriter(String key, List<Object> fields) throws IOException {

		var hexfile = hexToFile(key);
		// System.out.println("HEXFILE: " + hexfile);
		var row_out = new DataOutputStream(new FileOutputStream(hexfile.toFile()));
		Row new_row = new Row(key, fields);

		row_out.writeUTF(new_row.key());

		// for(int i = 0; i < new_row.toString().length()-1; i++) {
		for (Object obj : new_row.fields()) {

			if (obj instanceof String) {
				row_out.writeByte(-1);
				row_out.writeUTF((String) obj);
			} else if (obj instanceof Boolean) {
				row_out.writeByte(-2);
				row_out.writeBoolean((Boolean) obj);
			} else if (obj instanceof Integer) {
				row_out.writeByte(-3);
				row_out.writeInt((Integer) obj);
			} else if (obj instanceof Double) {
				row_out.writeByte(-4);
				row_out.writeDouble((Double) obj);
			} else if (obj == null) {
				row_out.writeByte(-5);

			}

		}
		row_out.close();

	}
	
	public Row readRow(Path p) {
		try {
			var row_in = new DataInputStream(Files.newInputStream(p));
	
			List<Object> fields = new ArrayList<>();
	
			var KEY = row_in.readUTF();
	
			while (row_in.available() > 0) {
	
				var readByte = row_in.readByte();
				// System.out.println(readByte);
				if (readByte == -1) {
					String str = row_in.readUTF();
					fields.add(str);
	
				} else if (readByte == -2) {
					var bool = row_in.readBoolean();
					fields.add(bool);
				} else if (readByte == -3) {
					int num = row_in.readInt();
					fields.add(num);
	
				} else if (readByte == -4) {
					double doub = row_in.readDouble();
					fields.add(doub);
				} else if (readByte == -5) {
					fields.add(null);
				}
	
				// System.out.println(fields);
			}
			var row = new Row(KEY, fields);
	
			row_in.close();
	
			return row;
		}
		catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}
	
	public Row rowReader(String key) throws IOException {

		var data = rootDir.resolve("data");
		var eatenkey = digestFunction(key);
		var hexfile = hexToFile(key);

		var row_in = new DataInputStream(new FileInputStream(hexfile.toFile()));

		List<Object> fields = new ArrayList<>();

		var KEY = row_in.readUTF();

		while (row_in.available() > 0) {

			var readByte = row_in.readByte();
			// System.out.println(readByte);
			if (readByte == -1) {
				String str = row_in.readUTF();
				fields.add(str);

			} else if (readByte == -2) {
				var bool = row_in.readBoolean();
				fields.add(bool);
			} else if (readByte == -3) {
				int num = row_in.readInt();
				fields.add(num);

			} else if (readByte == -4) {
				double doub = row_in.readDouble();
				fields.add(doub);
			} else if (readByte == -5) {
				fields.add(null);
			}

			// System.out.println(fields);
		}
		var row = new Row(KEY, fields);

		row_in.close();

		return row;
	}

	public Path hexToFile(String key) {

		var data = rootDir.resolve("data");
		var eatenkey = digestFunction(key);
		var subDir = data.resolve(eatenkey.substring(0, 2));

		var hexfile = subDir.resolve(eatenkey.substring(2, eatenkey.length()) + ".bin");

		return hexfile;

	}

	private String digestFunction(String key) {
		try {
			var sha1 = MessageDigest.getInstance("SHA-1");
			sha1.update("Josiah Fout".getBytes());
			sha1.update(key.getBytes());

			var digest = sha1.digest();
			var hex = HexFormat.of();

			return hex.formatHex(digest);

		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}

	}

	@Override
	public List<Object> put(String key, List<Object> fields) {

		if (key == null || fields == null) {
			throw new IllegalArgumentException("Invalid Key.");
		}

		if (fields.size() < 0 || fields.size() != columns().size() - 1) {
			throw new IllegalArgumentException("Fields are out of bounds.");
		}
		try {
			var data = rootDir.resolve("data");
			var eatenkey = digestFunction(key);
			var subDir = data.resolve(eatenkey.substring(0, 2));
			Path hexFile = hexToFile(key);

//			
//			if (Files.notExists(hexFile)) {
//				try {
//					Files.createFile(hexFile);
//				} catch (IOException e) {
//				}
//			}

			if (Files.exists(hexFile)) {

				var old_row = rowReader(key);
				rowWriter(key, fields);
				changeHC("-", old_row);
				changeHC("+", new Row(key, fields));

				return old_row.fields();
			} else {
				Files.createDirectories(subDir);

				rowWriter(key, fields);
				changeSize("+");
				changeHC("+", new Row(key, fields));
				return null;
			}

		} catch (IOException e) {
			// e.printStackTrace();
			throw new IllegalStateException(e);

		}

//		return null;
	}

	@Override
	public List<Object> get(String key) {
		try {
			var hexfile = hexToFile(key);

			if (Files.exists(hexfile)) {

				var grabbedRow = rowReader(key);
				return grabbedRow.fields();
			} else {
				return null;
			}
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}

	}

	@Override
	public List<Object> remove(String key) {

		try {
			var hexfile = hexToFile(key);
			var data = rootDir.resolve("data");
			var eatenkey = digestFunction(key);
			var subDir = data.resolve(eatenkey.substring(0, 2));
//			var hexfile = subDir.resolve(eatenkey.substring(2));

			if (Files.exists(hexfile)) { // HIT

				var temp = rowReader(key);
				changeSize("-");
				changeHC("-", temp);
				Files.delete(hexfile);

				return temp.fields();
			} else {

				return null;
			}

		} catch (IOException e) {
			throw new IllegalStateException(e);

			// e.printStackTrace();
		}
//		return null;

	}

	@Override
	public int degree() {
		return columns().size();

	}

	@Override
	public int size() {
		var size = rootDir.resolve("metadata").resolve("size.bin");

		try {
			var size_in = new DataInputStream(new FileInputStream(size.toFile()));
			var size_return = size_in.readInt();
			// System.out.println("SIZE: " + size_return);
			size_in.close();
			return size_return;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			throw new IllegalStateException(e);

		}
//		return 0;

	}

	@Override
	public int hashCode() {
		var hashcode = rootDir.resolve("metadata").resolve("fingerprint.bin");

		try {
			var printer2 = new DataInputStream(new FileInputStream(hashcode.toFile()));
			var printer2_return = printer2.readInt();
			return printer2_return;

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			throw new IllegalStateException(e);

		}
		return 0;

	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof Table && this.hashCode() == obj.hashCode();
	}

	@Override
	public Iterator<Row> iterator() {
		try {
			var data = rootDir.resolve("data");
			return Files.walk(data)
				.filter(path -> Files.isRegularFile(path))
				.map(path -> readRow(path))
				.iterator();
				
	} catch (IOException e) {

		throw new IllegalStateException(e);
	}
		
	}

	@Override
	public String name() {
		var name = rootDir.getFileName().toString();
		return name;
	}

	@Override
	public List<String> columns() {
		var columns = rootDir.resolve("metadata").resolve("columns.bin");
		try {
			DataInputStream columnread = new DataInputStream(new FileInputStream(columns.toFile()));
			List<String> things = new ArrayList<>();
			while (columnread.available() > 0) {
				String thing = columnread.readUTF();
				things.add(thing);
			}

//		

			columnread.close();
			return things;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}

	@Override
	public String toString() {
		return toPrettyString();
	}
}