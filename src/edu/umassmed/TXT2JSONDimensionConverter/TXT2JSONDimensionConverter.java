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
	final Map<String, List<String[]>> positions;
	final Map<String, List<String[]>> dimensions;
	
	public TXT2JSONDimensionConverter() {
		this.positions = new LinkedHashMap<String, List<String[]>>();
		this.dimensions = new LinkedHashMap<String, List<String[]>>();
	}
	
	public void writeDimensionsFile(final String f) throws IOException {
		final FileWriter fw = new FileWriter(f);
		final BufferedWriter bw = new BufferedWriter(fw);
		bw.write("{\n");
		int counter = 0;
		for (final String key : this.positions.keySet()) {
			
			final List<String[]> coords = this.positions.get(key);
			final List<String[]> dims = this.dimensions.get(key);
			bw.write("\t\"" + key + "\":");
			String tabs = "\t";
			if (coords.size() > 1) {
				bw.write("[\n");
				tabs += "\t";
			} else {
				bw.write("{\n");
			}

			for (int i = 0; i < coords.size(); i++) {
				final String[] singleCoord = coords.get(i);
				final String[] singleDims = dims.get(i);
				if (coords.size() > 1) {
					bw.write(tabs + "{\n");
				}
				
				bw.write(tabs + "\t\"x\":" + singleCoord[0] + ",\n");
				bw.write(tabs + "\t\"y\":" + singleCoord[1] + ",\n");
				bw.write(tabs + "\t\"w\":" + singleDims[0] + ",\n");
				bw.write(tabs + "\t\"h\":" + singleDims[1] + "\n");

				if (coords.size() > 1) {
					if (i < (coords.size() - 1)) {
						bw.write(tabs + "},\n");
					} else {
						bw.write(tabs + "}\n");
					}
				}
			}

			if (coords.size() > 1) {
				bw.write("\t]");
			} else {
				bw.write("\t}");
			}
			if (counter < (this.positions.keySet().size() - 1)) {
				bw.write(",\n");
			} else {
				bw.write("\n");
			}
			counter++;
		}
		bw.write("}");
		bw.close();
		fw.close();
	}

	public void parseDimensionsFile(final String f) throws IOException {
		final FileReader fr = new FileReader(f);
		final BufferedReader br = new BufferedReader(fr);

		String line = br.readLine();
		while (line != null) {
			final String[] values = line.split(":");
			
			final String[] coord = values[1].split(",");
			final String[] dims = values[2].split(",");
			List<String[]> list;
			if (this.positions.keySet().contains(values[0])) {
				list = this.positions.get(values[0]);
			} else {
				list = new ArrayList<String[]>();
			}
			list.add(coord);
			this.positions.put(values[0], list);
			if (this.dimensions.keySet().contains(values[0])) {
				list = this.dimensions.get(values[0]);
			} else {
				list = new ArrayList<String[]>();
			}
			list.add(dims);
			this.dimensions.put(values[0], list);
			line = br.readLine();
		}
		br.close();
		fr.close();
	}

	public static void main(final String[] args) {
		final TXT2JSONDimensionConverter conv = new TXT2JSONDimensionConverter();
		try {
			conv.parseDimensionsFile("./dimensions/Microscope_Inverted.txt");
			conv.writeDimensionsFile("./dimensions/Microscope_Inverted.json");
		} catch (final IOException ex) {
			ex.printStackTrace();
		}
	}
}
