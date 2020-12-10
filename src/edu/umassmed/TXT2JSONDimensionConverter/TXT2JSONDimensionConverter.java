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
	final Map<String, Map<String, List<String[]>>> positions;
	final Map<String, Map<String, List<String[]>>> dimensions;
	
	public TXT2JSONDimensionConverter() {
		this.positions = new LinkedHashMap<String, Map<String, List<String[]>>>();
		this.dimensions = new LinkedHashMap<String, Map<String, List<String[]>>>();
	}
	
	public void writeDimensionsFile() throws IOException {
		final FileWriter fw = new FileWriter(
				"./dimensionsV2/MicroscopeDimensions.json");
		final BufferedWriter bw = new BufferedWriter(fw);
		bw.write("{\n");
		int counter1 = 0;
		int counter2 = 0;
		for (final String type : this.positions.keySet()) {
			final Map<String, List<String[]>> typePositions = this.positions
					.get(type);
			final Map<String, List<String[]>> typeDimensions = this.dimensions
					.get(type);
			final String tabs = "\t";
			bw.write(tabs + "\"" + type + "\":{\n");
			for (final String key : typePositions.keySet()) {

				final List<String[]> coords = typePositions.get(key);
				final List<String[]> dims = typeDimensions.get(key);
				final String tabs2 = tabs + "\t";
				bw.write(tabs2 + "\"" + key + "\":");
				String tabs3 = tabs2;
				if (coords.size() > 1) {
					bw.write("[\n");
					tabs3 = tabs2 + "\t";
				} else {
					bw.write("{\n");
				}

				for (int i = 0; i < coords.size(); i++) {
					final String[] singleCoord = coords.get(i);
					final String[] singleDims = dims.get(i);
					if (coords.size() > 1) {
						bw.write(tabs3 + "{\n");
					}

					bw.write(tabs3 + "\t\"x\":" + singleCoord[0] + ",\n");
					bw.write(tabs3 + "\t\"y\":" + singleCoord[1] + ",\n");
					bw.write(tabs3 + "\t\"w\":" + singleDims[0] + ",\n");
					bw.write(tabs3 + "\t\"h\":" + singleDims[1] + "\n");

					if (coords.size() > 1) {
						if (i < (coords.size() - 1)) {
							bw.write(tabs3 + "},\n");
						} else {
							bw.write(tabs3 + "}\n");
						}
					}
				}

				if (coords.size() > 1) {
					bw.write(tabs2 + "]");
				} else {
					bw.write(tabs2 + "}");
				}
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
		
		Map<String, List<String[]>> typePositions;
		Map<String, List<String[]>> typeDimensions;
		if (this.positions.containsKey(type)) {
			typePositions = this.positions.get(type);
		} else {
			typePositions = new LinkedHashMap<String, List<String[]>>();
		}
		if (this.dimensions.containsKey(type)) {
			typeDimensions = this.dimensions.get(type);
		} else {
			typeDimensions = new LinkedHashMap<String, List<String[]>>();
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
			List<String[]> list;
			if (typePositions.keySet().contains(values[0])) {
				list = typePositions.get(values[0]);
			} else {
				list = new ArrayList<String[]>();
			}
			list.add(coord);
			typePositions.put(values[0], list);
			if (typeDimensions.keySet().contains(values[0])) {
				list = typeDimensions.get(values[0]);
			} else {
				list = new ArrayList<String[]>();
			}
			list.add(dims);
			typeDimensions.put(values[0], list);
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
