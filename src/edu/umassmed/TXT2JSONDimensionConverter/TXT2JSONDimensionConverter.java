package edu.umassmed.TXT2JSONDimensionConverter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TXT2JSONDimensionConverter {
	final Map<String, Map<String, Map<String, List<String[]>>>> positions;
	final Map<String, Map<String, Map<String, List<String[]>>>> dimensions;
	
	public TXT2JSONDimensionConverter() {
		this.positions = new LinkedHashMap<String, Map<String, Map<String, List<String[]>>>>();
		this.dimensions = new LinkedHashMap<String, Map<String, Map<String, List<String[]>>>>();
	}
	
	public void writeDimensionsFile() throws IOException {
		final FileWriter fw = new FileWriter(
				"./dimensionsV2/MicroscopeDimensions.json");
		final BufferedWriter bw = new BufferedWriter(fw);
		bw.write("{\n");
		int counter1 = 0;
		int counter2 = 0;
		int counter3 = 0;
		for (final String type : this.positions.keySet()) {
			final Map<String, Map<String, List<String[]>>> typePositions = this.positions
					.get(type);
			final Map<String, Map<String, List<String[]>>> typeDimensions = this.dimensions
					.get(type);
			final String tabs = "\t";
			bw.write(tabs + "\"" + type + "\":{\n");
			counter2 = 0;
			for (final String key : typePositions.keySet()) {
				final String tabs2 = tabs + "\t";
				bw.write(tabs2 + "\"" + key + "\":{\n");
				final Map<String, List<String[]>> groupCoord = typePositions
						.get(key);
				final Map<String, List<String[]>> groupDims = typeDimensions
						.get(key);
				counter3 = 0;
				for (final String groupKey : groupCoord.keySet()) {
					final String tabs3 = tabs2 + "\t";
					bw.write(tabs3 + "\"" + groupKey + "\":");
					
					final List<String[]> coords = groupCoord.get(groupKey);
					final List<String[]> dims = groupDims.get(groupKey);

					String tabs4 = tabs3;
					if (coords.size() > 1) {
						bw.write("[\n");
						tabs4 = tabs3 + "\t";
					} else {
						bw.write("{\n");
					}
					
					for (int i = 0; i < coords.size(); i++) {
						final String[] singleCoord = coords.get(i);
						final String[] singleDims = dims.get(i);
						if (coords.size() > 1) {
							bw.write(tabs4 + "{\n");
						}
						
						bw.write(tabs4 + "\t\"x\":" + singleCoord[0] + ",\n");
						bw.write(tabs4 + "\t\"y\":" + singleCoord[1] + ",\n");
						bw.write(tabs4 + "\t\"w\":" + singleDims[0] + ",\n");
						bw.write(tabs4 + "\t\"h\":" + singleDims[1] + "\n");
						
						if (coords.size() > 1) {
							if (i < (coords.size() - 1)) {
								bw.write(tabs4 + "},\n");
							} else {
								bw.write(tabs4 + "}\n");
							}
						}
					}
					
					if (coords.size() > 1) {
						bw.write(tabs3 + "]");
					} else {
						bw.write(tabs3 + "}");
					}
					if (counter3 < (groupCoord.keySet().size() - 1)) {
						bw.write(",\n");
					} else {
						bw.write("\n");
					}
					counter3++;
				}
				bw.write(tabs2 + "}");
				if (counter2 < (typePositions.keySet().size() - 1)) {
					bw.write(",\n");
				} else {
					bw.write("\n");
				}
				counter2++;
			}
			bw.write(tabs + "}");
			if (counter1 < (this.positions.keySet().size() - 1)) {
				bw.write(",\n");
			} else {
				bw.write("\n");
			}
			counter1++;
		}
		bw.write("}");
		bw.close();
		fw.close();
	}

	public void parseDimensionsFile(final String type) throws IOException {
		final String f = "./dimensionsV2/" + type + ".txt";
		final FileReader fr = new FileReader(f);
		final BufferedReader br = new BufferedReader(fr);
		
		Map<String, Map<String, List<String[]>>> typePositions;
		Map<String, Map<String, List<String[]>>> typeDimensions;
		if (this.positions.containsKey(type)) {
			typePositions = this.positions.get(type);
		} else {
			typePositions = new LinkedHashMap<String, Map<String, List<String[]>>>();
		}
		if (this.dimensions.containsKey(type)) {
			typeDimensions = this.dimensions.get(type);
		} else {
			typeDimensions = new LinkedHashMap<String, Map<String, List<String[]>>>();
		}
		
		String line = br.readLine();
		while (line != null) {
			if (line.startsWith("//") || line.equals("")) {
				line = br.readLine();
				continue;
			}
			
			final String[] values = line.split(":");
			
			final String[] coord = values[1].split(",");
			final String[] dims = values[2].split(",");
			Map<String, List<String[]>> map;
			List<String[]> list;
			
			final String key = values[0];

			String currentKey = values[0];
			String currentSubKey = "General";
			if (key.contains("#")) {
				final String[] keys = key.split("#");
				currentKey = keys[0];
				currentSubKey = keys[1];
			}
			if (typePositions.keySet().contains(currentKey)) {
				map = typePositions.get(currentKey);
				if (map.keySet().contains(currentSubKey)) {
					list = map.get(currentSubKey);
				} else {
					list = new ArrayList<String[]>();
				}
			} else {
				map = new LinkedHashMap<String, List<String[]>>();
				list = new ArrayList<String[]>();
			}
			list.add(coord);
			map.put(currentSubKey, list);
			typePositions.put(currentKey, map);

			if (typeDimensions.keySet().contains(currentKey)) {
				map = typeDimensions.get(currentKey);
				if (map.keySet().contains(currentSubKey)) {
					list = map.get(currentSubKey);
				} else {
					list = new ArrayList<String[]>();
				}
			} else {
				map = new LinkedHashMap<String, List<String[]>>();
				list = new ArrayList<String[]>();
			}
			list.add(dims);
			map.put(currentSubKey, list);
			typeDimensions.put(currentKey, map);
			line = br.readLine();
		}
		br.close();
		fr.close();
		this.positions.put(type, typePositions);
		this.dimensions.put(type, typeDimensions);
	}

	public static void main(final String[] args) {
		final TXT2JSONDimensionConverter conv = new TXT2JSONDimensionConverter();
		try {
			conv.parseDimensionsFile("InvertedMicroscopeStand");
			conv.writeDimensionsFile();
		} catch (final IOException ex) {
			ex.printStackTrace();
		}
	}
}
