package tables;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import model.FileTable;
import model.Row;
import model.Table;

public class JSONTable implements FileTable {
	private static final Path base = Paths.get("db", "tables");
	private final Path jsonFile;

	private static final ObjectMapper helper = new ObjectMapper();
	private final ObjectNode tree;

	public JSONTable(String name, List<String> columns) {
		try {
			Files.createDirectories(base);

			jsonFile = base.resolve(name + ".json");
			if (Files.notExists(jsonFile)) {
				Files.createFile(jsonFile);
			}

			tree = helper.createObjectNode();
			tree.putObject("metadata").putPOJO("columns", columns);
			var data = tree.putObject("data");

			flush();

		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	public JSONTable(String name) {
		try {
			jsonFile = base.resolve(name + ".json");

			if (Files.notExists(jsonFile)) {
				throw new IllegalArgumentException("Missing table: " + name);
			}

			tree = (ObjectNode) helper.readTree(jsonFile.toFile());
			flush();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public void clear() {
		//var tree = helper.createObjectNode();
		var data = tree.putObject("data");
		
		flush();
	}

	@Override
	public void flush() {
		try {
			helper.writerWithDefaultPrettyPrinter().writeValue(jsonFile.toFile(), tree);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public List<Object> put(String key, List<Object> fields) {
		if (key == null || fields == null) {
			throw new IllegalArgumentException("Invalid Key.");
		}
		if (fields.size() != columns().size() - 1) {
			throw new IllegalArgumentException("Fields are out of bounds.");
		}

		var data = tree.get("data");
		if (tree.get("data").has(key)) { // Checks if data has key

			var temp = data.get(key); // saves key's fields
			((ObjectNode) data).putPOJO(key, fields); // slaps it in data
			flush(); // writes data in.

			List<Object> temp2 = helper.convertValue(temp, List.class);

			return temp2; // return temp as List
		} else { // MISS
			((ObjectNode) data).putPOJO(key, fields);
			flush();

			return null;
		}

	}

	@Override
	public List<Object> get(String key) {
		var data = tree.get("data");

		if (tree.get("data").has(key)) {
			var temp = data.get(key);
			var fields = helper.convertValue(temp, List.class);

			return fields;
		}

		return null;
	}

	@Override
	public List<Object> remove(String key) {
		var data = tree.get("data");
		if (tree.get("data").has(key)) {
			var fields = helper.convertValue(data.get(key), List.class);
			((ObjectNode) data).remove(key);
			flush();
			return fields;
		}

		return null;

	}

	@Override
	public int degree() {
		return columns().size();
	}

	@Override
	public int size() {
		var size = tree.get("data").size();
		return size;

	}

	@Override
	public int hashCode() {
		int fingerprint = 0;
	
		var keys = tree.get("data").fieldNames();
	
		while(keys.hasNext()) {
			var next_key = keys.next();
		Row key = new Row(next_key, get(next_key));
		fingerprint += key.hashCode();
		}
		return fingerprint;
	} 
	
//	public HashTable filter(String column, String value) throws InvalidKeyException {
//		HashTable filtered = new HashTable(name(), columns()); 
//		
//	//	var filter = filtered.iterator();
//		var data = tree.get("data");
//		int index = columns().indexOf(column);
//		var keys = data.fieldNames();
//		
//		
//			for(JsonNode node: data) {
//				var row_key = keys.next();
//				if(node{index}.toString().equals(value)) {
//				
//			
//					filtered.put(row_key, helper.convertValue(node, new TypeReference<List<Object>>() {}));
//				}
//			
//			}
//			return filtered; //RETURN HASHTABLE
//		
//		}
//		
	
		
		
	

	@Override
	public boolean equals(Object obj) {
		return obj instanceof Table && this.hashCode() == obj.hashCode();
	}

	@Override
	public Iterator<Row> iterator() {
	
		List<Row> List = new ArrayList<>();
		var keys = tree.get("data").fieldNames();
			
		for(int i =0; i < size(); i++) {
				
				var row_key = keys.next();
				Row row = new Row(row_key, get(row_key));
				List.add(row);
		}
		
		return List.iterator();
				
	
	
	}

	@Override
	public String name() {
		var name = jsonFile.getFileName().toString();
		return name.substring(0, name.length() - 5);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> columns() {

		List<String> columns = new ArrayList<>();
		columns = helper.convertValue(tree.get("metadata").get("columns"), columns.getClass());

		return columns;
	}

	@Override
	public String toString() {
		return toPrettyString();
	}
}