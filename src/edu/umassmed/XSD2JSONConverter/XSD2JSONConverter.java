package edu.umassmed.XSD2JSONConverter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import org.w3c.dom.bootstrap.DOMImplementationRegistry;

import com.sun.org.apache.xerces.internal.impl.xs.XSImplementationImpl;
import com.sun.org.apache.xerces.internal.xs.StringList;
import com.sun.org.apache.xerces.internal.xs.XSAnnotation;
import com.sun.org.apache.xerces.internal.xs.XSAttributeDeclaration;
import com.sun.org.apache.xerces.internal.xs.XSAttributeUse;
import com.sun.org.apache.xerces.internal.xs.XSComplexTypeDefinition;
import com.sun.org.apache.xerces.internal.xs.XSConstants;
import com.sun.org.apache.xerces.internal.xs.XSElementDeclaration;
import com.sun.org.apache.xerces.internal.xs.XSLoader;
import com.sun.org.apache.xerces.internal.xs.XSModel;
import com.sun.org.apache.xerces.internal.xs.XSModelGroup;
import com.sun.org.apache.xerces.internal.xs.XSNamedMap;
import com.sun.org.apache.xerces.internal.xs.XSObject;
import com.sun.org.apache.xerces.internal.xs.XSObjectList;
import com.sun.org.apache.xerces.internal.xs.XSParticle;
import com.sun.org.apache.xerces.internal.xs.XSSimpleTypeDefinition;
import com.sun.org.apache.xerces.internal.xs.XSTerm;
import com.sun.org.apache.xerces.internal.xs.XSTypeDefinition;

public class XSD2JSONConverter {

	public static boolean forceVersion = true;
	public static String version = "2.01.0";

	public static boolean useProgress = false;
	public static String inputFileVersionProgress = "v02-10/";
	public static String inputFileVersionStable = "v02-01/";
	public static String versionType_Stable = "stable%20version/";
	public static String versionType_Progress = "in%20progress/";
	public static String githubPrefix = "https://raw.githubusercontent.com/WU-BIMAC/NBOMicroscopyMetadataSpecs/master/Model/";
	public static String fileName = "NBO_MicroscopyMetadataSpecifications_ALL.xsd";

	public static String tempSchemaFile = "schema.xsd";

	public static String extension = "Extension=";
	public static String domain = "Domain=";
	public static String category = "Category=";
	public static String tier = "Tier=";
	public static String desc = "Description=";
	public static String split = "Split=";
	public static String model_settings = "Model_Settings=";

	public static String id_attr = "ID";
	public static String tier_attr = "Tier";
	public static String name_attr = "Name";

	public static String version_tag = "modelVersion";
	public static String desc_tag = "description";

	public static String value_not_assigned = "NA";

	public static String generic_cat_attr = "General";

	public static String microscope_main_instrument = "Instrument";
	public static String microscope_main_stand = "MicroscopeStand";
	public static String image = "Image";
	public static String experiment = "Experiment";

	public static List<String> extension_exclusion_list = new ArrayList<String>();
	public static List<String> domain_exclusion_list = new ArrayList<String>();
	public static List<String> category_exclusion_list = new ArrayList<String>();
	public static List<String> element_exclusion_list = new ArrayList<String>();
	public static List<String> elementRef_exclusion_list = new ArrayList<String>();
	public static List<String> attribute_exclusion_list = new ArrayList<String>();
	static {
		XSD2JSONConverter.extension_exclusion_list.add("Advanced+Confocal");
		XSD2JSONConverter.extension_exclusion_list
				.add("Calibration and Performance");
		
		XSD2JSONConverter.domain_exclusion_list.add("VendorSpecifications");

		XSD2JSONConverter.category_exclusion_list.add("\"Reference\"");
		XSD2JSONConverter.category_exclusion_list.add("\"Annotation\"");
		XSD2JSONConverter.category_exclusion_list.add("\"TypeAnnotation\"");
		XSD2JSONConverter.category_exclusion_list.add("\"Settings\"");
		XSD2JSONConverter.category_exclusion_list.add("\"Instrument\"");
		// XSD2JSONConverter.category_exclusion_list.add("\"SpecsFile\"");
		XSD2JSONConverter.category_exclusion_list
				.add("\"IlluminationWavelengthRangeType\"");
		XSD2JSONConverter.category_exclusion_list
				.add("\"WavelengthRangeType\"");

		// FIXME excludere category imagedimensions + data
		XSD2JSONConverter.element_exclusion_list.add("BinData");
		XSD2JSONConverter.element_exclusion_list.add("VendorBinData");
		XSD2JSONConverter.element_exclusion_list.add("NGFFData");
		XSD2JSONConverter.element_exclusion_list.add("TiffData");
		XSD2JSONConverter.element_exclusion_list.add("TheAdditionalDimension");
		XSD2JSONConverter.element_exclusion_list.add("AdditionalDimensionMap");
		XSD2JSONConverter.element_exclusion_list
				.add("PlaneTransformationMatrix");
		XSD2JSONConverter.element_exclusion_list.add("MetadataOnly");
		XSD2JSONConverter.element_exclusion_list.add("MapAnnotation");
		// Fare un caso specifico per transformarlo in una string ?
		// XSD2JSONConverter.element_exclusion_list.add("SpecsFile");
		XSD2JSONConverter.element_exclusion_list.add("AcoustoOpticalDevice");
		XSD2JSONConverter.element_exclusion_list
				.add("AcoustoOpticalBeamSplitter");
		XSD2JSONConverter.element_exclusion_list.add("AcoustoOpticalDeflector");
		XSD2JSONConverter.element_exclusion_list.add("AcoustoOpticalModulator");
		XSD2JSONConverter.element_exclusion_list
				.add("AcoustoOpticalTuneableFilter");
		XSD2JSONConverter.element_exclusion_list.add("AcoustoOpticalDeviceRef");
		// XSD2JSONConverter.element_exclusion_list.add("Pump");
		
		// XSD2JSONConverter.attribute_exclusion_list.add("SpecsFile");
		for (final String s : XSD2JSONConverter.element_exclusion_list) {
			XSD2JSONConverter.elementRef_exclusion_list.add(s + "Ref");
		}
	}

	public static String subComponents_category = "ChildrenElement";

	public static String generic_cat_desc = "General information about the element";

	public static String generic_cat_fullstring = "\""
			+ XSD2JSONConverter.generic_cat_attr + "\"" + ":" + "\""
			+ XSD2JSONConverter.generic_cat_desc + "\"";

	public static String outputFile = "fullSchema";
	public static String outputFile_ext = ".json";
	public static String outputFolder = "./versions/";
	public static String outputFolderSingleSchemas = "schemas";
	public static String currentVersionLink = "./latest/";

	private XSModel model;
	private final List<XSElementDeclaration> elementList;

	private final StringBuffer errors, references;

	private static String image_ext_png = ".png";
	private static String image_ext_svg = ".svg";

	public XSD2JSONConverter() {
		this.model = null;
		this.elementList = new ArrayList<XSElementDeclaration>();
		this.errors = new StringBuffer();
		this.references = new StringBuffer();
	}

	private void createTempSchemaFile(final String fileURL,
			final String tmpFileName) throws IOException {
		final URL inputFileURL = new URL(fileURL);
		final ReadableByteChannel rbc = Channels.newChannel(inputFileURL
				.openStream());
		final FileOutputStream fos = new FileOutputStream(tmpFileName);
		fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
		fos.close();
		rbc.close();
	}

	private void parseXSDFile(final String fileName)
			throws ClassNotFoundException, InstantiationException,
			IllegalAccessException, ClassCastException {
		System.setProperty(DOMImplementationRegistry.PROPERTY,
				"com.sun.org.apache.xerces.internal.dom.DOMXSImplementationSourceImpl");
		final DOMImplementationRegistry registry = DOMImplementationRegistry
				.newInstance();
		final com.sun.org.apache.xerces.internal.impl.xs.XSImplementationImpl impl = (XSImplementationImpl) registry
				.getDOMImplementation("XS-Loader");
		final XSLoader schemaLoader = impl.createXSLoader(null);
		this.model = schemaLoader.loadURI(fileName);
	}

	private void retrieveElementList() {
		final XSNamedMap map = this.model
				.getComponents(XSConstants.ELEMENT_DECLARATION);
		for (int i = 0; i < map.getLength(); i++) {
			final XSObject obj = map.item(i);
			if (obj instanceof XSElementDeclaration) {
				final XSElementDeclaration element = (XSElementDeclaration) obj;
				final XSObjectList annotations = element.getAnnotations();
				for (int y = 0; y < annotations.getLength(); y++) {
					final XSObject annotationObj = annotations.item(y);
					if (annotationObj instanceof XSAnnotation) {
						final XSAnnotation annotation = (XSAnnotation) annotationObj;
						final String annot = annotation.getAnnotationString();
						if (annot.contains(XSD2JSONConverter.domain)) {
							this.elementList.add(element);
						}
					}
				}
			}
		}
	}

	private Map<String, String> parseElement(final XSTypeDefinition typeDef,
			final Map<String, String> attributeMap) {
		if ((typeDef instanceof XSComplexTypeDefinition)
				&& !typeDef.getName().equals("anyType")) {
			final Map<String, String> newAttributeMap = new LinkedHashMap<String, String>();
			final XSComplexTypeDefinition complTypeDef = (XSComplexTypeDefinition) typeDef;
			newAttributeMap.putAll(this.parseElement(
					complTypeDef.getBaseType(), attributeMap));
			final String name = complTypeDef.getName();
			final XSObjectList attrList = complTypeDef.getAttributeUses();
			for (int i = 0; i < attrList.getLength(); i++) {
				final XSObject obj = attrList.item(i);
				if (obj instanceof XSAttributeUse) {
					final XSAttributeUse attributeUse = (XSAttributeUse) obj;
					final XSAttributeDeclaration attribute = attributeUse
							.getAttrDeclaration();
					final String attrName = attribute.getName();
					if (!newAttributeMap.containsKey(attrName)) {
						newAttributeMap.put(attrName, name);
					}
				}
			}
			return newAttributeMap;
		} else
			return attributeMap;

	}

	private Map<XSElementDeclaration, Map<String, String>> parseElements() {
		final Map<XSElementDeclaration, Map<String, String>> elementAttributeCategoryMap = new LinkedHashMap<XSElementDeclaration, Map<String, String>>();
		for (final XSElementDeclaration element : this.elementList) {
			final XSTypeDefinition typeDef = element.getTypeDefinition();
			final Map<String, String> attributeMap = new LinkedHashMap<String, String>();
			if (typeDef instanceof XSComplexTypeDefinition) {
				final XSComplexTypeDefinition complTypeDef = (XSComplexTypeDefinition) typeDef;
				attributeMap.putAll(this.parseElement(
						complTypeDef.getBaseType(), attributeMap));
				final String name = element.getName();
				final XSObjectList attrList = complTypeDef.getAttributeUses();
				for (int i = 0; i < attrList.getLength(); i++) {
					final XSObject obj = attrList.item(i);
					if (obj instanceof XSAttributeUse) {
						final XSAttributeUse attributeUse = (XSAttributeUse) obj;
						final XSAttributeDeclaration attribute = attributeUse
								.getAttrDeclaration();
						final String attrName = attribute.getName();
						if (!attributeMap.containsKey(attrName)) {
							attributeMap.put(attrName, name);
						}
					}
				}
			}
			elementAttributeCategoryMap.put(element, attributeMap);
			// for (final String key : attributeMap.keySet()) {
			// System.out.println(key + " - " + attributeMap.get(key));
			// }

		}
		return elementAttributeCategoryMap;

	}

	private void writeJSONFiles(
			final Map<XSElementDeclaration, Map<String, String>> map,
			final String path) throws IOException {
		XSElementDeclaration instrument = null;
		XSElementDeclaration image = null;
		final List<String> jsons = new ArrayList<String>();
		for (final XSElementDeclaration element : map.keySet()) {
			final String elementName = element.getName();
			if (elementName
					.equals(XSD2JSONConverter.microscope_main_instrument)) {
				instrument = element;
				// } else if (elementName
				// .equals(XSD2JSONConverter.microscope_main_stand)) {
				// microscopeBody = element;
			} else if (elementName.equals(XSD2JSONConverter.image)) {
				image = element;
			} else {
				final XSObjectList annotations = element.getAnnotations();
				if (XSD2JSONConverter.element_exclusion_list
						.contains(elementName)
						|| XSD2JSONConverter.extension_exclusion_list
								.contains(this.getExtension(elementName,
										annotations))
						|| XSD2JSONConverter.domain_exclusion_list
								.contains(this.getDomain(elementName,
										annotations))) {
					continue;
				}
				final List<String> compsJson = this.writeComponentJSONFile(
						element, map.get(element), path);
				jsons.addAll(compsJson);
			}
		}

		if ((instrument != null) /* && (microscopeBody != null) */) {
			final Map<String, String> microscopeMap = new LinkedHashMap<String, String>();
			final Map<String, String> instrumentMap = map.get(instrument);
			microscopeMap.putAll(instrumentMap);
			// final Map<String, String> microscopeBodyMap = map
			// .get(microscopeBody);
			// microscopeMap.putAll(microscopeBodyMap);
			final String micJson = this.writeMicroscopeJSONFile(instrument,
			/* microscopeBody, */microscopeMap, path);
			jsons.add(0, micJson);
		}
		boolean isErrorOrTerminated = false;

		if (image != null) {
			// final Map<String, String> reviewImageMap = new
			// LinkedHashMap<String, String>();
			// System.out.println(reviewImageMap);
			final Map<String, String> imageMap = map.get(image);
			final String imageJson = this
					.writeImageAndTopLevelSettingsJsonFiles(image, imageMap,
							path);
			if (imageJson != null) {
				jsons.add(1, imageJson);
			} else {
				// ERROR OR TERMINATED
				isErrorOrTerminated = true;
			}
		}

		if (isErrorOrTerminated)
			return;

		final String fileName = path + File.separator + ".." + File.separator
				+ XSD2JSONConverter.outputFile
				+ XSD2JSONConverter.outputFile_ext;
		final File f = new File(fileName);
		// final FileWriter fw = new FileWriter(f);
		// final BufferedWriter bw = new BufferedWriter(fw);
		final FileOutputStream fos = new FileOutputStream(f);
		final OutputStreamWriter osw = new OutputStreamWriter(fos,
				StandardCharsets.UTF_8);
		osw.write("[\n");
		for (int i = 0; i < jsons.size(); i++) {
			final String json = jsons.get(i);
			osw.write(json);
			if (i < (jsons.size() - 1)) {
				osw.write(",\n");
			} else {
				osw.write("\n");
			}
		}
		osw.write("]");
		osw.close();
		fos.close();
		// fw.close();
		// bw.close();
	}

	private StringBuffer checkForChangesAndSetVersion(final File f,
			final StringBuffer sb) throws IOException {
		final FileReader fr = new FileReader(f);
		final BufferedReader br = new BufferedReader(fr);

		final StringBuffer oldSb = new StringBuffer();
		String line = br.readLine();
		while (line != null) {
			oldSb.append(line);
			oldSb.append("\n");
			line = br.readLine();
		}
		br.close();
		fr.close();

		final String[] oldString = oldSb.toString().split("\n");
		final String[] newString = sb.toString().split("\n");

		if ((oldString.length != newString.length)
				|| XSD2JSONConverter.forceVersion)
			return sb;

		int versionIndex = -1;
		for (int i = 0; i < newString.length; i++) {
			final String s1 = oldString[i];
			final String s2 = newString[i];
			if (!s1.contains(XSD2JSONConverter.version_tag) && !s1.equals(s2)
					&& !s1.contains(XSD2JSONConverter.desc_tag))
				return sb;
			if (s1.contains(XSD2JSONConverter.version_tag)) {
				versionIndex = i;
			}
		}
		newString[versionIndex] = oldString[versionIndex];

		final StringBuffer newSb = new StringBuffer();
		for (final String s : newString) {
			newSb.append(s);
			newSb.append("\n");
		}
		return newSb;
	}

	private String writeImageAndTopLevelSettingsJsonFiles(
			final XSElementDeclaration image, final Map<String, String> map,
			final String path) throws IOException {
		final XSObjectList annotations = image.getAnnotations();
		final XSTypeDefinition imageTypeDef = image.getTypeDefinition();
		final String name = image.getName();
		if (imageTypeDef instanceof XSComplexTypeDefinition) {
			final XSComplexTypeDefinition imageComplTypeDef = (XSComplexTypeDefinition) imageTypeDef;

			final String originalExtension = this.getExtension(name,
					annotations);
			final String originalDomain = this.getDomain(name, annotations);
			final String originalModelSettings = this.getModelSettings(name,
					annotations);
			final String originalCategory = this.getCategory(name, annotations);

			final List<XSParticle> particles = this
					.getChildrenParticleList(imageComplTypeDef);

			this.errors.append(name);
			this.errors.append("\n");

			this.references.append(name);
			this.references.append("\n");

			final List<List<String>> childrenAttributesAndRequired = this
					.getChildrenAttributesAndRequired(particles, name,
							imageComplTypeDef, originalCategory);
			final List<String> childrenAttributes = childrenAttributesAndRequired
					.get(0);
			final List<String> childrenRequired = childrenAttributesAndRequired
					.get(1);

			final XSObjectList attrList = imageComplTypeDef.getAttributeUses();
			String catName = null;
			if (originalCategory.equals("ChildElement")) {
				catName = name;
			}

			final List<List<String>> attributesAndRequired = this
					.getAttributesAndRequired(catName, map, attrList, 0);
			final List<String> attributes = attributesAndRequired.get(0);
			final List<String> required = attributesAndRequired.get(1);
			attributes.addAll(childrenAttributes);
			required.addAll(childrenRequired);

			// final List<String> attributes = new ArrayList<String>();
			final List<String> subCategoriesOrder = this.getSubCategoriesOrder(
					image, attributes);
			// subCategoriesOrder.add(0,
			// XSD2JSONConverter.generic_cat_fullstring);

			final List<String> toRemove = new ArrayList<String>();
			for (final String categoryToExclude : XSD2JSONConverter.category_exclusion_list) {
				for (final String category : subCategoriesOrder) {
					if (category.startsWith(categoryToExclude)) {
						toRemove.add(category);
					}
				}
			}
			subCategoriesOrder.removeAll(toRemove);

			final Integer tier = this.getTier(name, annotations);
			final String desc = this.getDescription(name, annotations);
			final StringBuffer sb = new StringBuffer();
			sb.append("{\n");
			sb.append("\t\"$schema\":\"http://json-schema.org/draft-07/schema\",\n");
			sb.append("\t\"ID\":\"Image.json\",\n");
			sb.append("\t\"" + XSD2JSONConverter.version_tag + "\":\""
					+ XSD2JSONConverter.version + "\",\n");
			sb.append("\t\"type\":\"object\",\n");
			sb.append("\t\"title\":\"Image\",\n");
			sb.append("\t\"description\":\"" + desc + "\",\n");
			sb.append("\t\"modelSettings\":\"" + originalModelSettings
					+ "\",\n");
			sb.append("\t\"extension\":\"" + originalExtension + "\",\n");
			sb.append("\t\"domain\":\"" + originalDomain + "\",\n");
			sb.append("\t\"category\":\"" + originalCategory + "\",\n");
			sb.append("\t\"tier\":" + tier + ",\n");
			sb.append("\t\"subCategoriesOrder\": {\n");
			for (int i = 0; i < subCategoriesOrder.size(); i++) {
				sb.append("\t\t" + subCategoriesOrder.get(i));
				if (i < (subCategoriesOrder.size() - 1)) {
					sb.append(",\n");
				} else {
					sb.append("\n");
				}
			}
			sb.append("\t},\n");
			sb.append("\t\"properties\": {\n");
			for (final String s : attributes) {
				sb.append(s);
				sb.append(",\n");
			}
			// int currentIndex = sb.length();
			// int previousIndex = sb.length();
			// for (int i = 0; i < attrList.getLength(); i++) {
			// boolean insert = false;
			// final StringBuffer aSB = new StringBuffer();
			// // use="optional" type="xsd:string"
			// final XSObject obj = attrList.item(i);
			// if (obj instanceof XSAttributeUse) {
			// final XSAttributeUse attributeUse = (XSAttributeUse) obj;
			// insert = this.getAttribute(attributeUse, aSB, null, map,
			// required, 0);
			// aSB.append(",\n");
			// }
			// if (insert) {
			// sb.insert(previousIndex, aSB.toString());
			// } else {
			// sb.append(aSB);
			// }
			// previousIndex = currentIndex;
			// currentIndex = sb.length();
			// }
			sb.append("\t\t\"Tier\": {\n");
			sb.append("\t\t\t\"type\":\"integer\",\n");
			sb.append("\t\t\t\"description\":\"The tier level of the microscope.\",\n");
			sb.append("\t\t\t\"tier\":" + tier + ",\n");
			sb.append("\t\t\t\"category\":\""
					+ XSD2JSONConverter.generic_cat_attr + "\",\n");
			sb.append("\t\t\t\"readonly\":true\n");
			sb.append("\t\t}\n");
			sb.append("\t}");
			required.add("Tier");
			
			if (required.size() > 0) {
				sb.append(",\n");
				sb.append("\t\"required\": [\n");
				for (int i = 0; i < required.size(); i++) {
					sb.append("\t\t\"" + required.get(i) + "\"");
					if (i < (required.size() - 1)) {
						sb.append(",\n");
					} else {
						sb.append("\n");
					}
				}
				sb.append("\t]\n");
			} else {
				sb.append("\n");
			}
			sb.append("}");

			this.errors.append("**********");
			this.errors.append("\n");
			this.references.append("**********");
			this.references.append("\n");
			final String fileName = path + File.separator + "Image"
					+ XSD2JSONConverter.outputFile_ext;
			final File f = new File(fileName);
			StringBuffer versionedSb = null;
			if (f.exists()) {
				versionedSb = this.checkForChangesAndSetVersion(f, sb);
			} else {
				versionedSb = sb;
			}

			// final FileWriter fw = new FileWriter(f);
			// final BufferedWriter bw = new BufferedWriter(fw);
			final FileOutputStream fos = new FileOutputStream(f);
			final OutputStreamWriter osw = new OutputStreamWriter(fos,
					StandardCharsets.UTF_8);
			osw.write(versionedSb.toString());
			osw.close();
			fos.close();
			return versionedSb.toString();
		}
		return null;
	}

	private String writeMicroscopeJSONFile(
			final XSElementDeclaration instrument,
			// final XSElementDeclaration microscopeBody,
			final Map<String, String> map, final String path)
			throws IOException {
		final XSObjectList annotations = instrument.getAnnotations();
		final XSTypeDefinition instrumentTypeDef = instrument
				.getTypeDefinition();
		// final XSTypeDefinition microscopeBodyTypeDef = microscopeBody
		// .getTypeDefinition();
		final String name = instrument.getName();
		if ((instrumentTypeDef instanceof XSComplexTypeDefinition)
		// && (microscopeBodyTypeDef instanceof XSComplexTypeDefinition)
		) {
			final XSComplexTypeDefinition instrumentComplTypeDef = (XSComplexTypeDefinition) instrumentTypeDef;
			// final XSComplexTypeDefinition microscopeBodyComplTypeDef =
			// (XSComplexTypeDefinition) microscopeBodyTypeDef;
			
			final List<String> attributes = new ArrayList<String>();
			// final List<String> subCategoriesOrder = new ArrayList<String>();
			final List<String> subCategoriesOrder = this.getSubCategoriesOrder(
					instrument, attributes);
			// final List<String> subCategoriesOrder =
			// this.getSubCategoriesOrder(microscopeBody, attributes);
			subCategoriesOrder.add(0, XSD2JSONConverter.generic_cat_fullstring);
			
			final List<String> toRemove = new ArrayList<String>();
			for (final String categoryToExclude : XSD2JSONConverter.category_exclusion_list) {
				for (final String category : subCategoriesOrder) {
					if (category.startsWith(categoryToExclude)) {
						toRemove.add(category);
					}
				}
			}
			subCategoriesOrder.removeAll(toRemove);
			
			final Integer tier = this.getTier(name, annotations);
			final String desc = this.getDescription(name, annotations);
			// final String image = "Microscope_Empty_new" +
			// XSD2JSONConverter.image_ext_svg;
			
			final StringBuffer sb = new StringBuffer();
			sb.append("{\n");
			sb.append("\t\"$schema\":\"http://json-schema.org/draft-07/schema\",\n");
			sb.append("\t\"ID\":\"Instrument.json\",\n");
			sb.append("\t\"" + XSD2JSONConverter.version_tag + "\":\""
					+ XSD2JSONConverter.version + "\",\n");
			sb.append("\t\"type\":\"object\",\n");
			sb.append("\t\"title\":\"Instrument\",\n");
			sb.append("\t\"description\":\"" + desc + "\",\n");
			// sb.append("\t\"modelSettings\":\"" + "MicroscopeSettings.json" +
			// "\",\n");
			// sb.append("\t\"image\":\"" + XSD2JSONConverter.image + "\",\n");
			sb.append("\t\"tier\":" + tier + ",\n");
			sb.append("\t\"subCategoriesOrder\": {\n");
			for (int i = 0; i < subCategoriesOrder.size(); i++) {
				sb.append("\t\t" + subCategoriesOrder.get(i));
				if (i < (subCategoriesOrder.size() - 1)) {
					sb.append(",\n");
				} else {
					sb.append("\n");
				}
			}
			sb.append("\t},\n");
			sb.append("\t\"properties\": {\n");
			final XSObjectList attrList = instrumentComplTypeDef
					.getAttributeUses();
			final List<String> required = new ArrayList<String>();
			int currentIndex = sb.length();
			int previousIndex = sb.length();
			for (int i = 0; i < attrList.getLength(); i++) {
				boolean insert = false;
				final StringBuffer aSB = new StringBuffer();
				// use="optional" type="xsd:string"
				final XSObject obj = attrList.item(i);
				if (obj instanceof XSAttributeUse) {
					final XSAttributeUse attributeUse = (XSAttributeUse) obj;
					insert = this.getAttribute(attributeUse, aSB, null, map,
							required, 0);
					aSB.append(",\n");
				}
				if (insert) {
					sb.insert(previousIndex, aSB.toString());
				} else {
					sb.append(aSB);
				}
				previousIndex = currentIndex;
				currentIndex = sb.length();
			}
			sb.append("\t\t\"Description\": {\n");
			sb.append("\t\t\t\"type\":\"string\",\n");
			sb.append("\t\t\t\"description\":\"This is a simple multi-line comment or annotation describing this component.\",\n");
			sb.append("\t\t\t\"tier\":" + 1 + ",\n");
			sb.append("\t\t\t\"category\":\""
					+ XSD2JSONConverter.generic_cat_attr + "\"\n");
			sb.append("\t\t},\n");
			sb.append("\t\t\"Tier\": {\n");
			sb.append("\t\t\t\"type\":\"integer\",\n");
			sb.append("\t\t\t\"description\":\"The tier level of the microscope.\",\n");
			sb.append("\t\t\t\"tier\":" + tier + ",\n");
			sb.append("\t\t\t\"category\":\""
					+ XSD2JSONConverter.generic_cat_attr + "\",\n");
			sb.append("\t\t\t\"readonly\":true\n");
			sb.append("\t\t}\n");
			sb.append("\t}");
			required.add("Tier");
			
			if (required.size() > 0) {
				sb.append(",\n");
				sb.append("\t\"required\": [\n");
				for (int i = 0; i < required.size(); i++) {
					sb.append("\t\t\"" + required.get(i) + "\"");
					if (i < (required.size() - 1)) {
						sb.append(",\n");
					} else {
						sb.append("\n");
					}
				}
				sb.append("\t]\n");
			} else {
				sb.append("\n");
			}
			sb.append("}");
			final String fileName = path + File.separator + "Instrument"
					+ XSD2JSONConverter.outputFile_ext;
			final File f = new File(fileName);
			StringBuffer versionedSb = null;
			if (f.exists()) {
				versionedSb = this.checkForChangesAndSetVersion(f, sb);
			} else {
				versionedSb = sb;
			}
			
			// final FileWriter fw = new FileWriter(f);
			// final BufferedWriter bw = new BufferedWriter(fw);
			final FileOutputStream fos = new FileOutputStream(f);
			final OutputStreamWriter osw = new OutputStreamWriter(fos,
					StandardCharsets.UTF_8);
			osw.write(versionedSb.toString());
			osw.close();
			fos.close();
			return versionedSb.toString();
		}
		return null;
	}

	private boolean getAttribute(final XSAttributeUse attributeUse,
			final StringBuffer aSB, final String catName,
			final Map<String, String> attrCategories,
			final List<String> required, final Integer extraTabs) {
		boolean insert = false;
		final XSAttributeDeclaration attribute = attributeUse
				.getAttrDeclaration();
		final XSObjectList attrAnnotations = attribute.getAnnotations();
		final String attrName = attribute.getName();
		final Integer attrTier = this.getTier(attrName, attrAnnotations);
		final String attrDesc = this.getDescription(attrName, attrAnnotations);

		final XSSimpleTypeDefinition typeDef = attribute.getTypeDefinition();
		final StringList enums = typeDef.getLexicalEnumeration();
		// if (!enums.isEmpty()) {
		// System.out.println(enums);
		// }

		final String attrCategory;
		if (catName != null) {
			attrCategory = catName;
		} else if (attrName.equalsIgnoreCase(XSD2JSONConverter.id_attr)
				|| attrName.equals(XSD2JSONConverter.name_attr)) {
			if (attrName.equals(XSD2JSONConverter.name_attr)) {
				insert = true;
			}
			attrCategory = XSD2JSONConverter.generic_cat_attr;
		} else {
			attrCategory = attrCategories.get(attrName);
		}
		if (attributeUse.getRequired()) {
			required.add(attrName);
		}
		String defaultValue = null;
		String fixedValue = null;
		if (attributeUse.getConstraintType() == XSConstants.VC_DEFAULT) {
			defaultValue = attributeUse.getConstraintValue();
		} else if (attributeUse.getConstraintType() == XSConstants.VC_FIXED) {
			fixedValue = attributeUse.getConstraintValue();
		}
		String attrType;
		// System.out.println(attribute.getName() + " - "
		final short builtInKind = typeDef.getBuiltInKind();
		if ((builtInKind == XSConstants.INTEGER_DT)
				|| (builtInKind == XSConstants.INT_DT)
				|| (builtInKind == XSConstants.POSITIVEINTEGER_DT)
				|| (builtInKind == XSConstants.NONPOSITIVEINTEGER_DT)
				|| (builtInKind == XSConstants.NEGATIVEINTEGER_DT)
				|| (builtInKind == XSConstants.NONNEGATIVEINTEGER_DT)
				|| (builtInKind == XSConstants.LONG_DT)
				|| (builtInKind == XSConstants.SHORT_DT)) {
			attrType = "integer";
		} else if ((builtInKind == XSConstants.FLOAT_DT)
				|| (builtInKind == XSConstants.DOUBLE_DT)
				|| (builtInKind == XSConstants.DECIMAL_DT)) {
			attrType = "number";
		} else if (builtInKind == XSConstants.BOOLEAN_DT) {
			attrType = "boolean";
			// } else if (builtInKind == XSConstants.ANYURI_DT) {
			// attrType = "uri";
		} else {
			attrType = "string";
		}
		for (int i = 0; i < extraTabs; i++) {
			aSB.append("\t");
		}
		aSB.append("\t\t\"" + attrName + "\": {\n");
		for (int i = 0; i < extraTabs; i++) {
			aSB.append("\t");
		}
		aSB.append("\t\t\t\"type\":\"" + attrType + "\",\n");
		for (int i = 0; i < extraTabs; i++) {
			aSB.append("\t");
		}
		aSB.append("\t\t\t\"description\":\"" + attrDesc + "\",\n");
		for (int i = 0; i < extraTabs; i++) {
			aSB.append("\t");
		}
		aSB.append("\t\t\t\"tier\":" + attrTier + ",\n");
		if (defaultValue != null) {
			for (int i = 0; i < extraTabs; i++) {
				aSB.append("\t");
			}
			aSB.append("\t\t\t\"default\":\"" + defaultValue + "\",\n");
		}
		boolean readonly = false;
		if (fixedValue != null) {
			for (int i = 0; i < extraTabs; i++) {
				aSB.append("\t");
			}
			aSB.append("\t\t\t\"const\":\"" + fixedValue + "\",\n");
			readonly = true;
		}
		if (!enums.isEmpty()) {
			for (int i = 0; i < extraTabs; i++) {
				aSB.append("\t");
			}
			aSB.append("\t\t\t\"enum\": [\n");
			for (int k = 0; k < enums.getLength(); k++) {
				final String s = enums.item(k);
				for (int i = 0; i < extraTabs; i++) {
					aSB.append("\t");
				}
				aSB.append("\t\t\t\t\"" + s + "\"");
				if (k < (enums.getLength() - 1)) {
					aSB.append(",\n");
				} else {
					aSB.append("\n");
				}
			}
			for (int i = 0; i < extraTabs; i++) {
				aSB.append("\t");
			}
			aSB.append("\t\t\t],\n");
		}
		for (int i = 0; i < extraTabs; i++) {
			aSB.append("\t");
		}
		aSB.append("\t\t\t\"category\":\"" + attrCategory + "\"");
		if (attrName.equals(XSD2JSONConverter.tier_attr)
				|| attrName.equals(XSD2JSONConverter.id_attr) || readonly) {
			aSB.append(",\n");
			for (int i = 0; i < extraTabs; i++) {
				aSB.append("\t");
			}
			aSB.append("\t\t\t\"readonly\":true\n");
		} else {
			aSB.append("\n");
		}
		for (int i = 0; i < extraTabs; i++) {
			aSB.append("\t");
		}
		aSB.append("\t\t}");
		return insert;
	}

	private List<XSParticle> getAllParticles(final XSParticle containerParticle) {
		final List<XSParticle> list = new ArrayList<XSParticle>();
		final XSTerm containerTerm = containerParticle.getTerm();
		if (containerTerm instanceof XSElementDeclaration) {
			list.add(containerParticle);
			return list;
		}
		if (!(containerTerm instanceof XSModelGroup))
			return list;
		final XSModelGroup containerGroup = (XSModelGroup) containerTerm;
		final XSObjectList particles = containerGroup.getParticles();
		for (int i = 0; i < particles.size(); i++) {
			final XSParticle particle = (XSParticle) particles.item(i);
			list.addAll(this.getAllParticles(particle));
		}
		return list;
	}

	private List<XSParticle> getChildrenParticleList(
			final XSComplexTypeDefinition complTypeDef) {
		final List<XSParticle> list = new ArrayList<XSParticle>();
		if (complTypeDef.getParticle() == null)
			return list;
		final XSParticle containerParticle = complTypeDef.getParticle();
		final XSTerm containerTerm = containerParticle.getTerm();
		if (!(containerTerm instanceof XSModelGroup))
			return list;
		final XSModelGroup containerGroup = (XSModelGroup) containerTerm;
		final XSObjectList particles = containerGroup.getParticles();
		for (int i = 0; i < particles.size(); i++) {
			final XSParticle particle = (XSParticle) particles.item(i);
			list.addAll(this.getAllParticles(particle));
		}
		return list;
	}

	private List<List<String>> getChildrenAttributesAndRequired(
			final List<XSParticle> particles, final String name,
			final XSComplexTypeDefinition complTypeDef,
			final String parentCategory) {
		System.out.println("Considering " + name);
		final List<List<String>> returns = new ArrayList<List<String>>();
		final List<String> attributes = new ArrayList<String>();
		final List<String> required = new ArrayList<String>();
		for (int i = 0; i < particles.size(); i++) {
			StringBuffer aSB = new StringBuffer();
			final XSParticle particle = particles.get(i);
			final XSTerm term = particle.getTerm();
			final int min = particle.getMinOccurs();
			final int max = particle.getMaxOccurs();
			final String maxS = String.valueOf(max);
			final XSElementDeclaration element = (XSElementDeclaration) term;
			final String elementName = element.getName();
			final String elementTypeName = element.getTypeDefinition()
					.getName();
			this.references
					.append(elementName + " from " + min + " to " + maxS);
			this.references.append("\n");
			if (elementName.contains("AnnotationRef")) {
				// final FIXME skip for final the moment
				final XSObjectList annotations = element.getAnnotations();
				final Integer attrTier = this.getTier(elementName, annotations);
				final String attrDesc = this.getDescription(elementName,
						annotations);
				final String attrName = "Description";
				final String attrType = "string";
				String attrCategory = XSD2JSONConverter.generic_cat_attr;
				if (parentCategory.equals("ChildElement")) {
					attrCategory = name;
				}
				aSB.append("\t\t\"" + attrName + "\": {\n");
				aSB.append("\t\t\t\"title\":\"" + attrName + "\",\n");
				aSB.append("\t\t\t\"type\":\"" + attrType + "\",\n");
				aSB.append("\t\t\t\"description\":\"" + attrDesc + "\",\n");
				aSB.append("\t\t\t\"tier\":" + attrTier + ",\n");
				aSB.append("\t\t\t\"category\":\"" + attrCategory + "\"");
				aSB.append("\n");
				aSB.append("\t\t}");
				// required.add(attrName);
				attributes.add(aSB.toString());
			} else if (elementName.endsWith("InstrumentRef")
					&& name.equals(XSD2JSONConverter.image)) {
				// TODO special case create field with microscope name + uuid to
				// be filled in the app
				final String attrName = "InstrumentName";
				final String attrType = "string";
				String attrCategory = XSD2JSONConverter.generic_cat_attr;
				if (parentCategory.equals("ChildElement")) {
					attrCategory = name;
				}
				aSB.append("\t\t\"" + attrName + "\": {\n");
				aSB.append("\t\t\t\"title\":\"" + attrName + "\",\n");
				aSB.append("\t\t\t\"type\":\"" + attrType + "\",\n");
				aSB.append("\t\t\t\"description\":\""
						+ "Name of the instrument" + "\",\n");
				aSB.append("\t\t\t\"tier\":" + 1 + ",\n");
				aSB.append("\t\t\t\"category\":\"" + attrCategory + "\",\n");
				aSB.append("\t\t\t\"readonly\":true");
				aSB.append("\n");
				aSB.append("\t\t}");
				required.add(attrName);
				attributes.add(aSB.toString());
				aSB = new StringBuffer();
				final String attrName2 = "InstrumentID";
				aSB.append("\t\t\"" + attrName2 + "\": {\n");
				aSB.append("\t\t\t\"title\":\"" + attrName2 + "\",\n");
				aSB.append("\t\t\t\"type\":\"" + attrType + "\",\n");
				aSB.append("\t\t\t\"description\":\"" + "ID of the instrument"
						+ "\",\n");
				aSB.append("\t\t\t\"tier\":" + 1 + ",\n");
				aSB.append("\t\t\t\"category\":\"" + attrCategory + "\",\n");
				aSB.append("\t\t\t\"readonly\":true");
				aSB.append("\n");
				aSB.append("\t\t}");
				required.add(attrName);
				attributes.add(aSB.toString());
			} else if ((elementName.endsWith("ExperimentRef")
					|| elementName.endsWith("ExperimenterRef")
					|| elementName.endsWith("ExperimenterGroupRef") || (elementName
					.endsWith("SampleRef") | elementName.endsWith("ROIRef")))
					&& name.equals(XSD2JSONConverter.image)) {
				// TODO special case dont know how to handle yet
				// I can probably just ignore because Experiment has is own file
				// and I can handle it separately in the application
			} else if (elementName.endsWith("MicrobeamManipulationRef")
					&& (name.equals(XSD2JSONConverter.image) || name
							.equals(XSD2JSONConverter.experiment))) {
				// TODO special case need to be removed from both because it has
				// its own sub-categories but how to handle it ?
			} else if ((elementName.endsWith("Plane")
					|| elementName.endsWith("Channel") || elementName
						.endsWith("Pixels"))
					&& name.equals(XSD2JSONConverter.image)) {
				// TODO special case create separate json with only array of
				// these elements, but how ?
			}
			// else if ((elementName.endsWith("ImagingEnvironment")
			// || elementName.endsWith("ObjectiveSettings")
			// || elementName.endsWith("TIRFSettings") || elementName
			// .endsWith("MicroscopeSettings"))
			// && name.equals(XSD2JSONConverter.image)) {
			// // TODO special case needs to be discarded because we have
			// // single files for these, needs to be double checked if more
			// // stuff here
			// }
			else if (elementName.endsWith("TheAdditionalDimension")
					&& name.equals("Plane")) {
				// FIXME temporary exclusion case
			} else if (XSD2JSONConverter.element_exclusion_list
					.contains(elementName)
					|| XSD2JSONConverter.elementRef_exclusion_list
							.contains(elementName)) {
				// TODO all the special cases that are completely disregarded
				// at the moment
			} else if (elementName.endsWith("ProfileFile")
					|| elementName.endsWith("ProfileFileRef")) {
				// FIXME build special case for this
			} else if (elementName.endsWith("Map")) {
				// TODO unknown case
			} else if (elementName.contains("SpecsFile")) {
				final XSObjectList annotations = element.getAnnotations();
				final Integer attrTier = this.getTier(elementName, annotations);
				final String attrDesc = this.getDescription(elementName,
						annotations);
				final String attrName = elementName;
				final String attrType = "string";
				final String attrCategory = "ManufacturerSpec";
				// this.getCategory(elementName, annotations);
				// String attrCategory = XSD2JSONConverter.generic_cat_attr;
				// if (parentCategory.equals("ChildElement")) {
				// attrCategory = name;
				// }
				aSB.append("\t\t\"" + attrName + "\": {\n");
				aSB.append("\t\t\t\"title\":\"" + attrName + "\",\n");
				aSB.append("\t\t\t\"type\":\"" + attrType + "\",\n");
				aSB.append("\t\t\t\"description\":\"" + attrDesc + "\",\n");
				aSB.append("\t\t\t\"tier\":" + attrTier + ",\n");
				aSB.append("\t\t\t\"category\":\"" + attrCategory + "\"");
				aSB.append("\n");
				aSB.append("\t\t}");
				// required.add(attrName);
				attributes.add(aSB.toString());
			} else if (elementName.endsWith("Ref")
					|| ((elementTypeName != null) && elementTypeName
							.endsWith("Ref"))) {
				
				if (name.equals("LightPath")) {
					continue;
				}

				// FIXME this need to be double checked
				final XSObjectList annotations = element.getAnnotations();
				final Integer attrTier = this.getTier(elementName, annotations);
				final String attrDesc = this.getDescription(elementName,
						annotations);
				// ("Ref", "")
				String attrName = elementName;
				if (elementName.endsWith("Ref")) {
					attrName = elementName.substring(0,
							elementName.length() - 3);
				}
				String attrCategory = attrName;
				if (parentCategory.equals("ChildElement")) {
					attrCategory = name;
				}
				final String attrType = "string";
				String attrLinkTo = attrName;
				if (!elementName.endsWith("Ref")) {
					final String attrTypeName = elementTypeName.substring(0,
							elementTypeName.length() - 3);
					attrLinkTo = attrTypeName;
				}
				if (attrLinkTo.equals("Laser")) {
					attrLinkTo = "Fluorescence_LightSource_Laser";
				}
				aSB.append("\t\t\"" + attrName + "\": {\n");
				boolean isArray = false;
				if ((max > 1) || (max == -1) /* || ((min == 0) && (max == 1)) */) {
					aSB.append("\t\t\t\"type\":\"array\",\n");
					aSB.append("\t\t\t\"items\": {\n");
					isArray = true;
				} else {
					aSB.append("\t\t\t\"type\":\"" + attrType + "\",\n");
				}
				if (isArray) {
					aSB.append("\t");
				}
				aSB.append("\t\t\t\"description\":\"" + attrDesc + "\",\n");
				if (isArray) {
					aSB.append("\t");
					aSB.append("\t\t\t\"type\":\"" + attrType + "\",\n");
				}
				if (isArray) {
					aSB.append("\t");
				}
				aSB.append("\t\t\t\"tier\":" + attrTier + ",\n");
				if (isArray) {
					aSB.append("\t");
				}
				aSB.append("\t\t\t\"category\":\"" + attrCategory + "\",\n");
				if (isArray) {
					aSB.append("\t");
				}
				aSB.append("\t\t\t\"linkTo\":\"" + attrLinkTo + "\"\n");
				if (isArray) {
					aSB.append("\t\t\t}\n");
				}
				aSB.append("\t\t}");
				if (min == 1) {
					required.add(attrName);
				}
				// aSB.append("\t\t\"" + attrNameCat + "\": {\n");
				// aSB.append("\t\t\t\t\t\"type\":\"" + attrType + "\",\n");
				// aSB.append("\t\t\t\"description\":\"" + attrDesc +
				// "\",\n");
				// aSB.append("\t\t\t\"tier\":" + attrTier + ",\n");
				// aSB.append("\t\t\t\"schemaID\":\"" + attrName +
				// ".json\"\n");
				// aSB.append("\t\t\t\"category\":\"" + attrName + "\"");
				// required.add(attrName);
				attributes.add(aSB.toString());
			} else {
				if (!(element.getTypeDefinition() instanceof XSComplexTypeDefinition)) {
					this.errors.append(elementName + " in " + name
							+ " is not complex");
					this.errors.append("\n");

					// CREATE FIELD WITH CONTAINSELEMENT similar to linkTo

					// final XSObjectList annotations =
					// element.getAnnotations();
					// final Integer attrTier = this.getTier(elementName,
					// annotations);
					// final String attrDesc = this.getDescription(elementName,
					// annotations);
					// final String attrName = elementName;
					// // String attrNameCat = attrName;
					// aSB.append("\t\t\"" + attrName + "\": {\n");
					// aSB.append("\t\t\t\"type\":\"object\",\n");
					// aSB.append("\t\t\t\"description\":\"" + attrDesc +
					// "\",\n");
					// aSB.append("\t\t\t\"tier\":" + attrTier + ",\n");
					// aSB.append("\t\t\t\"type\":\"object\",\n");
					// aSB.append("\t\t\t\"description\":\"" + attrDesc +
					// "\",\n");
					// aSB.append("\t\t\t\"tier\":" + attrTier + ",\n");
					// aSB.append("\t\t}");
					// if (min == 1) {
					// required.add(attrName);
					// }
					// attributes.add(aSB.toString());
				} else {

					final XSComplexTypeDefinition elementComplTypeDef = (XSComplexTypeDefinition) element
							.getTypeDefinition();
					final XSObjectList attrList = elementComplTypeDef
							.getAttributeUses();
					final XSObjectList annotations = element.getAnnotations();
					final Integer attrTier = this.getTier(elementName,
							annotations);
					final String attrDesc = this.getDescription(elementName,
							annotations);
					final String attrName = elementName;
					final String category = this.getCategory(attrName,
							annotations);
					if ((category != null) && !category.equals("ChildElement")) {

						// FIXME need to extrapolate all these classes instead
						// of wrap them
						System.out.println("Element not ChildElement -> "
								+ attrName + " - " + category);

						// final String attrType = "string";
						// aSB.append("\t\t\"" + attrName + "\": {\n");
						// final boolean isArray = false;
						// if ((max > 1) || (max == -1)) {
						// aSB.append("\t\t\t\"type\":\"" + attrType + "\",\n");
						// // FIXME temporary workaround to fix the fact lots
						// // of min/max in the xsd file are wrong
						// // aSB.append("\t\t\t\"type\":\"array\",\n");
						// // aSB.append("\t\t\t\"items\": {\n");
						// // isArray = true;
						// } else {
						// aSB.append("\t\t\t\"type\":\"" + attrType + "\",\n");
						// }
						// if (isArray) {
						// aSB.append("\t");
						// }
						// aSB.append("\t\t\t\"description\":\"" + attrDesc
						// + "\",\n");
						// if (isArray) {
						// aSB.append("\t");
						// aSB.append("\t\t\t\"type\":\"" + attrType + "\",\n");
						// }
						// if (isArray) {
						// aSB.append("\t");
						// }
						// aSB.append("\t\t\t\"tier\":" + attrTier + ",\n");
						// if (isArray) {
						// aSB.append("\t");
						// }
						// aSB.append("\t\t\t\"category\":\"" + attrName +
						// "\",\n");
						// if (isArray) {
						// aSB.append("\t");
						// }
						// aSB.append("\t\t\t\"contains\":\"" + attrName +
						// "\"\n");
						// if (isArray) {
						// aSB.append("\t\t\t}\n");
						// }
						// aSB.append("\t\t}");
						// if (min == 1) {
						// required.add(attrName);
						// }
						// attributes.add(aSB.toString());
					} else {
						// String attrNameCat = attrName;
						aSB.append("\t\t\"" + attrName + "\": {\n");
						boolean isArray = false;
						if ((max > 1) || (max == -1)
						/* || ((min == 0) && (max == 1)) */) {
							aSB.append("\t\t\t\"type\":\"array\",\n");
							isArray = true;
						} else {
							aSB.append("\t\t\t\"type\":\"object\",\n");
						}
						aSB.append("\t\t\t\"description\":\"" + attrDesc
								+ "\",\n");
						aSB.append("\t\t\t\"tier\":" + attrTier + ",\n");
						// aSB.append("\t\t\t\"schemaID\":\"" + attrName
						// + ".json\",\n");
						if (isArray) {
							aSB.append("\t\t\t\"items\": {\n");
							aSB.append("\t");
						}
						aSB.append("\t\t\t\"properties\": {\n");
						if ((category != null)
								&& category.equals("ChildElement")) {
							final List<XSParticle> subParticles = this
									.getChildrenParticleList(elementComplTypeDef);
							for (int k = 0; k < subParticles.size(); k++) {
								final XSParticle subParticle = subParticles
										.get(k);
								final XSTerm subTerm = subParticle.getTerm();
								final int subMin = subParticle.getMinOccurs();
								final int subMax = subParticle.getMaxOccurs();
								String.valueOf(subMax);
								final XSElementDeclaration subElement = (XSElementDeclaration) subTerm;
								final String subElementName = subElement
										.getName();
								if (subElementName.contains("AnnotationRef")) {
									// final FIXME skip for final the moment
									final XSObjectList subAnnotations = subElement
											.getAnnotations();
									final Integer subAttrTier = this.getTier(
											subElementName, subAnnotations);
									final String subAttrDesc = this
											.getDescription(subElementName,
													subAnnotations);
									final String subAttrName = "Description";
									final String subAttrType = "string";
									aSB.append("\t\t\t\t\"" + subAttrName
											+ "\": {\n");
									aSB.append("\t\t\t\t\t\"title\":\""
											+ subAttrName + "\",\n");
									aSB.append("\t\t\t\t\t\"type\":\""
											+ subAttrType + "\",\n");
									aSB.append("\t\t\t\t\t\"description\":\""
											+ subAttrDesc + "\",\n");
									aSB.append("\t\t\t\t\t\"tier\":"
											+ subAttrTier + ",\n");
									aSB.append("\t\t\t\t\t\"category\":\""
											+ attrName + "\"");
									aSB.append("\n");
									aSB.append("\t\t\t\t}");
									// required.add(attrName);
									attributes.add(aSB.toString());
									if (k < (subParticles.size() - 1)) {
										aSB.append(",\n");
									} else {
										aSB.append("\n");
									}
								} else if (subElementName.endsWith("Ref")
										&& !XSD2JSONConverter.element_exclusion_list
												.contains(subElementName)) {
									final XSObjectList subAnnotations = element
											.getAnnotations();
									final Integer subAttrTier = this.getTier(
											subElementName, subAnnotations);
									final String subAttrDesc = this
											.getDescription(elementName,
													subAnnotations);
									// ("Ref", "")
									final String subAttrName = subElementName
											.substring(0,
													subElementName.length() - 3);
									final String subAttrCategory = attrName;
									final String subAttrType = "string";
									aSB.append("\t\t\t\t\"" + subAttrName
											+ "\": {\n");
									boolean subIsArray = false;
									if ((subMax > 1) || (subMax == -1)/*
																	 * || ((min
																	 * == 0) &&
																	 * (max ==
																	 * 1))
																	 */) {
										aSB.append("\t\t\t\t\t\"type\":\"array\",\n");
										aSB.append("\t\t\t\t\t\"items\": {\n");
										subIsArray = true;
									} else {
										aSB.append("\t\t\t\t\t\"type\":\""
												+ subAttrType + "\",\n");
									}
									if (subIsArray) {
										aSB.append("\t");
									}
									aSB.append("\t\t\t\t\t\"description\":\""
											+ subAttrDesc + "\",\n");
									if (subIsArray) {
										aSB.append("\t");
										aSB.append("\t\t\t\t\t\"type\":\""
												+ subAttrType + "\",\n");
									}
									if (subIsArray) {
										aSB.append("\t");
									}
									aSB.append("\t\t\t\t\t\"tier\":"
											+ subAttrTier + ",\n");
									if (subIsArray) {
										aSB.append("\t");
									}
									aSB.append("\t\t\t\t\t\"category\":\""
											+ subAttrCategory + "\",\n");
									if (subIsArray) {
										aSB.append("\t");
									}
									aSB.append("\t\t\t\t\t\"linkTo\":\""
											+ subAttrName + "\"\n");
									if (subIsArray) {
										aSB.append("\t\t\t\t\t}\n");
									}
									aSB.append("\t\t\t\t}");
									if (subMin == 1) {
										required.add(subAttrName);
									}
									if (k < (subParticles.size() - 1)) {
										aSB.append(",\n");
									} else {
										aSB.append("\n");
									}
								}
							}
						}
						String catName = attrName;
						if (parentCategory.equals("ChildElement")) {
							catName = parentCategory;
						}

						final List<List<String>> attributesAndRequiredLocal = this
								.getAttributesAndRequired(catName, null,
										attrList, isArray ? 3 : 2);
						final List<String> attributesLocal = attributesAndRequiredLocal
								.get(0);
						final List<String> requiredLocal = attributesAndRequiredLocal
								.get(1);
						for (int k = 0; k < attributesLocal.size(); k++) {
							// for (final String s : attributesLocal) {
							final String s = attributesLocal.get(k);
							aSB.append(s);
							if (k < (attributesLocal.size() - 1)) {
								aSB.append(",\n");
							} else {
								aSB.append("\n");
							}
						}
						if (isArray) {
							aSB.append("\t");
						}
						aSB.append("\t\t\t}");
						// TODO add } only if max > 1
						if (requiredLocal.size() > 0) {
							aSB.append(",\n");
							if (isArray) {
								aSB.append("\t");
							}
							aSB.append("\t\t\t\"required\": [\n");
							// attributes.addAll(attributesLocal);
							// required.addAll(requiredLocal);
							for (int k = 0; k < requiredLocal.size(); k++) {
								final String s = requiredLocal.get(k);
								if (isArray) {
									aSB.append("\t");
								}
								aSB.append("\t\t\t\t\"" + s + "\"");
								if (k < (requiredLocal.size() - 1)) {
									aSB.append(",\n");
								} else {
									aSB.append("\n");
								}
							}
							if (isArray) {
								aSB.append("\t");
							}
							aSB.append("\t\t\t]\n");
						} else {
							aSB.append("\n");
						}
						if (isArray) {
							aSB.append("\t\t\t}\n");
						}
						aSB.append("\t\t}");
						if (min == 1) {
							required.add(attrName);
						}
						attributes.add(aSB.toString());
					}
				}
			}
		}
		returns.add(attributes);
		returns.add(required);
		return returns;
	}

	private List<List<String>> getAttributesAndRequired(final String catName,
			final Map<String, String> attrCategories,
			final XSObjectList attrList, final Integer extraTabs) {
		final List<List<String>> returns = new ArrayList<List<String>>();
		final List<String> jsonAttributes = new ArrayList<String>();
		int currentIndex = jsonAttributes.size();
		int previousIndex = jsonAttributes.size();
		final List<String> required = new ArrayList<String>();
		for (int k = 0; k < attrList.getLength(); k++) {
			boolean insert = false;
			final StringBuffer attrSB = new StringBuffer();
			// use="optional" type="xsd:string"
			final XSObject obj = attrList.item(k);
			if (obj instanceof XSAttributeUse) {
				final XSAttributeUse attributeUse = (XSAttributeUse) obj;
				insert = this.getAttribute(attributeUse, attrSB, catName,
						attrCategories, required, extraTabs);
				// attrSB.append(",\n");
			}
			if (insert) {
				jsonAttributes.add(previousIndex, attrSB.toString());
			} else {
				jsonAttributes.add(attrSB.toString());
			}
			previousIndex = currentIndex;
			currentIndex = jsonAttributes.size();
		}
		returns.add(jsonAttributes);
		returns.add(required);
		return returns;
	}

	private List<String> getSubCategoriesOrder(
			final XSElementDeclaration element, final List<String> attributes) {
		final List<String> subCategoriesOrder = new ArrayList<String>();
		final List<String> categoriesNeeded = new ArrayList<String>();
		boolean hasGeneric = false;
		final String categoryTag = "\"category\":";
		final String descriptionTag = "\"description\":";
		for (final String s : attributes) {
			if (s.contains(categoryTag)) {
				final int index1 = s.indexOf(categoryTag)
						+ categoryTag.length();
				final int index2 = s.indexOf("\n", index1);
				String cat = s.substring(index1, index2);
				cat = cat.replace(",", "");
				// cat = cat.replace("\"", "");
				if (cat.equals("\"" + XSD2JSONConverter.generic_cat_attr + "\"")) {
					hasGeneric = true;
				} else {
					if (!categoriesNeeded.contains(cat)
							&& !cat.equals("\"" + element.getName() + "\"")) {
						if (s.contains(descriptionTag)) {
							final int index3 = s.indexOf(descriptionTag)
									+ descriptionTag.length();
							final int index4 = s.indexOf("\n", index3);
							String desc = s.substring(index3, index4);
							desc = desc.replace(",", "");
							// desc = desc.replace("\"", "");
							cat += ":" + desc;
						} else {
							cat += ":" + "\"" + "" + "\"";
						}
						categoriesNeeded.add(cat);
					}
				}
			}
		}
		int insertIndex = 0;
		if (hasGeneric) {
			subCategoriesOrder.add(XSD2JSONConverter.generic_cat_fullstring);
			insertIndex = 1;
		}
		XSTypeDefinition typeDef = element.getTypeDefinition();
		final XSObjectList eleAnnotations = element.getAnnotations();
		String eleDesc = this.getDescription(typeDef.getName(), eleAnnotations);
		if (eleDesc == null) {
			eleDesc = "";
		}

		if (typeDef instanceof XSComplexTypeDefinition) {
			typeDef = typeDef.getBaseType();
			while ((typeDef instanceof XSComplexTypeDefinition)
					&& !typeDef.getName().equals("anyType")) {
				final String typeDefName = typeDef.getName();
				if (XSD2JSONConverter.category_exclusion_list
						.contains(typeDefName)
						|| XSD2JSONConverter.element_exclusion_list
								.contains(typeDefName)) {
					typeDef = typeDef.getBaseType();
					continue;
				}
				final XSObjectList annotations = ((XSComplexTypeDefinition) typeDef)
						.getAnnotations();
				String description = this.getDescription(typeDef.getName(),
						annotations);
				if (description == null) {
					description = "";
				}
				final String name = "\"" + typeDef.getName() + "\"";
				final String desc = "\"" + description + "\"";
				final String cat = name + ":" + desc;
				subCategoriesOrder.add(insertIndex, cat);
				final List<String> toRemove = new ArrayList<String>();
				for (final String s : categoriesNeeded) {
					if (s.startsWith(name)) {
						toRemove.add(s);
					}
				}
				categoriesNeeded.removeAll(toRemove);
				typeDef = typeDef.getBaseType();
			}
		}
		final String eleCat = "\"" + element.getName() + "\"" + ":" + "\""
				+ eleDesc + "\"";
		subCategoriesOrder.add(eleCat);
		subCategoriesOrder.addAll(categoriesNeeded);
		return subCategoriesOrder;
	}

	private List<String> writeComponentJSONFile(
			final XSElementDeclaration element, final Map<String, String> map,
			final String path) throws IOException {

		final XSObjectList annotations = element.getAnnotations();
		final XSTypeDefinition typeDef = element.getTypeDefinition();
		final String name = element.getName();
		if (typeDef instanceof XSComplexTypeDefinition) {
			final XSComplexTypeDefinition complTypeDef = (XSComplexTypeDefinition) typeDef;

			final String originalExtension = this.getExtension(name,
					annotations);
			final String originalDomain = this.getDomain(name, annotations);
			final String originalModelSettings = this.getModelSettings(name,
					annotations);
			final String originalCategory = this.getCategory(name, annotations);

			final List<XSParticle> particles = this
					.getChildrenParticleList(complTypeDef);

			this.errors.append(name);
			this.errors.append("\n");

			this.references.append(name);
			this.references.append("\n");

			final List<List<String>> childrenAttributesAndRequired = this
					.getChildrenAttributesAndRequired(particles, name,
							complTypeDef, originalCategory);
			final List<String> childrenAttributes = childrenAttributesAndRequired
					.get(0);
			final List<String> childrenRequired = childrenAttributesAndRequired
					.get(1);
			if (childrenRequired.contains("Description")) {
				System.out.println(childrenRequired);
			}

			final XSObjectList attrList = complTypeDef.getAttributeUses();
			String catName = null;
			if (originalCategory.equals("ChildElement")) {
				catName = name;
			}
			final List<List<String>> attributesAndRequired = this
					.getAttributesAndRequired(catName, map, attrList, 0);
			final List<String> attributes = attributesAndRequired.get(0);
			final List<String> required = attributesAndRequired.get(1);
			attributes.addAll(childrenAttributes);
			required.addAll(childrenRequired);

			final List<String> subCategoriesOrder = this.getSubCategoriesOrder(
					element, attributes);

			final List<String> toRemove = new ArrayList<String>();
			for (final String categoryToExclude : XSD2JSONConverter.category_exclusion_list) {
				for (final String category : subCategoriesOrder) {
					if (category.startsWith(categoryToExclude)) {
						toRemove.add(category);
					}
				}
			}
			subCategoriesOrder.removeAll(toRemove);
			// System.out.println(element.getName() + " - " +
			// subCategoriesOrder);

			// final int index = category.lastIndexOf(".") + 1;
			// if (index != -1) {
			// category = category.substring(index);
			// }
			final Integer tier = this.getTier(name, annotations);
			final String desc = this.getDescription(name, annotations);

			String[] splitCategories = this.getSplitCategories(name,
					annotations);

			boolean isSplit = true;
			if (splitCategories == null) {
				splitCategories = new String[1];
				splitCategories[0] = originalCategory;
				isSplit = false;
			}

			final List<String> comps = new ArrayList<String>();
			for (final String category : splitCategories) {

				final String newName = category.replaceAll("\\.", "_") + "_"
						+ name;

				final String image = newName + XSD2JSONConverter.image_ext_svg;

				final StringBuffer sb = new StringBuffer();
				sb.append("{\n");
				sb.append("\t\"$schema\":\"http://json-schema.org/draft-07/schema\",\n");
				if (isSplit) {
					sb.append("\t\"ID\":\"" + newName + ".json\",\n");
				} else {
					sb.append("\t\"ID\":\"" + name + ".json\",\n");
				}
				sb.append("\t\"" + XSD2JSONConverter.version_tag + "\":\""
						+ XSD2JSONConverter.version + "\",\n");
				sb.append("\t\"type\":\"object\",\n");
				sb.append("\t\"title\":\"" + name + "\",\n");
				sb.append("\t\"description\":\"" + desc + "\",\n");
				sb.append("\t\"modelSettings\":\"" + originalModelSettings
						+ "\",\n");
				sb.append("\t\"extension\":\"" + originalExtension + "\",\n");
				sb.append("\t\"domain\":\"" + originalDomain + "\",\n");
				sb.append("\t\"category\":\"" + category + "\",\n");
				if (!category.equals(XSD2JSONConverter.subComponents_category)) {
					sb.append("\t\"image\":\"" + image + "\",\n");
				}
				// TODO is this neeeded for subComponent?
				sb.append("\t\"tier\":" + tier + ",\n");
				sb.append("\t\"subCategoriesOrder\": {\n");
				for (int i = 0; i < subCategoriesOrder.size(); i++) {
					sb.append("\t\t" + subCategoriesOrder.get(i));
					if (i < (subCategoriesOrder.size() - 1)) {
						sb.append(",\n");
					} else {
						sb.append("\n");
					}
				}
				sb.append("\t},\n");
				// sb.append(references);
				// sb.append(",\n");
				sb.append("\t\"properties\": {\n");
				
				int c = 0;
				for (final String s : attributes) {
					sb.append(s);
					if (c < (attributes.size() - 1)) {
						sb.append(",\n");
					}
					// else {
					// sb.append("\n");
					// }

					c++;
				}
				// TODO is this needed for subComponents?
				if (!category.equals("ChildElement")) {
					sb.append(",\n");
					sb.append("\t\t\"Tier\": {\n");
					sb.append("\t\t\t\"type\":\"integer\",\n");
					sb.append("\t\t\t\"description\":\"The tier level of this component.\",\n");
					sb.append("\t\t\t\"tier\":" + tier + ",\n");
					sb.append("\t\t\t\"category\":\""
							+ XSD2JSONConverter.generic_cat_attr + "\",\n");
					sb.append("\t\t\t\"readonly\":true\n");
					sb.append("\t\t}\n");
					required.add("Tier");
				} else {
					sb.append("\n");
				}

				sb.append("\t}");

				if (required.size() > 0) {
					sb.append(",\n");
					sb.append("\t\"required\": [\n");
					for (int i = 0; i < required.size(); i++) {
						sb.append("\t\t\"" + required.get(i) + "\"");
						if (i < (required.size() - 1)) {
							sb.append(",\n");
						} else {
							sb.append("\n");
						}
					}
					sb.append("\t]\n");
				} else {
					sb.append("\n");
				}
				sb.append("}");

				this.errors.append("**********");
				this.errors.append("\n");
				this.references.append("**********");
				this.references.append("\n");

				final File f;
				if (isSplit) {
					final String fileName = path + File.separator + newName
							+ XSD2JSONConverter.outputFile_ext;
					f = new File(fileName);
				} else {
					final String fileName = path + File.separator + name
							+ XSD2JSONConverter.outputFile_ext;
					f = new File(fileName);
				}
				StringBuffer versionedSb = null;
				if (f.exists()) {
					versionedSb = this.checkForChangesAndSetVersion(f, sb);
				} else {
					versionedSb = sb;
				}
				final FileOutputStream fos = new FileOutputStream(f);
				final OutputStreamWriter osw = new OutputStreamWriter(fos,
						StandardCharsets.UTF_8);
				// final FileWriter fw = new FileWriter(f);
				// final BufferedWriter bw = new BufferedWriter(fw);
				osw.write(versionedSb.toString());
				osw.close();
				fos.close();
				comps.add(versionedSb.toString());
			}
			return comps;
		}
		return null;
	}

	private String[] getSplitCategories(final String name,
			final XSObjectList annotations) {
		for (int y = 0; y < annotations.getLength(); y++) {
			final XSObject annotationObj = annotations.item(y);
			if (annotationObj instanceof XSAnnotation) {
				final XSAnnotation annotation = (XSAnnotation) annotationObj;
				final String annot = annotation.getAnnotationString();
				if (annot.contains(XSD2JSONConverter.split)) {
					final int begin = annot.indexOf(XSD2JSONConverter.split);
					final int end = annot.indexOf("</", begin);
					String splitCategoriesS = annot.substring(begin, end);
					splitCategoriesS = splitCategoriesS.replace(
							XSD2JSONConverter.split, "");
					splitCategoriesS = splitCategoriesS.replaceAll("\\[", "");
					splitCategoriesS = splitCategoriesS.replaceAll("\\]", "");
					splitCategoriesS = splitCategoriesS.replaceAll(" ", "");
					splitCategoriesS = splitCategoriesS.replaceAll("\\.", "");

					if (splitCategoriesS.contains("null")) {
						this.errors.append(name + " category is null");
						this.errors.append("\n");
						splitCategoriesS = XSD2JSONConverter.value_not_assigned;
					}
					if (splitCategoriesS.contains("\t")) {
						this.errors.append(name + " category contains TAB");
						this.errors.append("\n");
						splitCategoriesS = splitCategoriesS
								.replaceAll("\t", "");
					}
					if (splitCategoriesS.contains("\n")) {
						this.errors.append(name + " category contains NEWLINE");
						this.errors.append("\n");
						splitCategoriesS = splitCategoriesS
								.replaceAll("\n", "");
					}
					final String[] splitCategories = splitCategoriesS
							.split(";");
					return splitCategories;
				}
			}
		}
		return null;
	}

	private String getDescription(final String name,
			final XSObjectList annotations) {
		if (annotations.getLength() == 0) {
			this.errors.append(name
					+ " description is missing (no annotations)");
			this.errors.append("\n");
			return XSD2JSONConverter.value_not_assigned;
		}
		for (int y = 0; y < annotations.getLength(); y++) {
			final XSObject annotationObj = annotations.item(y);
			if (annotationObj instanceof XSAnnotation) {
				final XSAnnotation annotation = (XSAnnotation) annotationObj;
				final String annot = annotation.getAnnotationString();
				if (annot.contains(XSD2JSONConverter.desc)) {
					final int begin = annot.indexOf(XSD2JSONConverter.desc);
					final int end = annot.indexOf("</", begin);
					String description = annot.substring(begin, end);
					description = description.replace(XSD2JSONConverter.desc,
							"");
					if (description.contains("null")) {
						this.errors.append(name + " description is null");
						this.errors.append("\n");
						description = XSD2JSONConverter.value_not_assigned;
					}
					if (description.contains("\t")) {
						this.errors.append(name + " description contains TAB");
						this.errors.append("\n");
						description = description.replaceAll("\t", "");
					}
					if (description.contains("\n")) {
						this.errors.append(name
								+ " description contains NEWLINE");
						this.errors.append("\n");
						description = description.replaceAll("\n", "");
					}
					if (description.contains("\"")) {
						this.errors.append(name + " description contains \"");
						this.errors.append("\n");
						description = description.replaceAll("\"", "'");
					}
					return description;
				} else {
					this.errors.append(name + " description is missing");
					this.errors.append("\n");
					return XSD2JSONConverter.value_not_assigned;
				}
			}
		}
		return null;
	}

	private String getExtension(final String name,
			final XSObjectList annotations) {
		if (annotations.getLength() == 0) {
			this.errors.append(name + " extension is missing (no annotations)");
			this.errors.append("\n");
		}
		for (int y = 0; y < annotations.getLength(); y++) {
			final XSObject annotationObj = annotations.item(y);
			if (annotationObj instanceof XSAnnotation) {
				final XSAnnotation annotation = (XSAnnotation) annotationObj;
				final String annot = annotation.getAnnotationString();
				if (annot.contains(XSD2JSONConverter.extension)) {
					final int begin = annot
							.indexOf(XSD2JSONConverter.extension);
					final int end = annot.indexOf("</", begin);
					String extension = annot.substring(begin, end);
					extension = extension.replace(XSD2JSONConverter.extension,
							"");
					if (extension.contains("null")) {
						this.errors.append(name + " extension is null");
						this.errors.append("\n");
						extension = XSD2JSONConverter.value_not_assigned;
					}
					if (extension.contains(" ")) {
						this.errors.append(name + " extension contains SPACE");
						this.errors.append("\n");
						// extension = extension.replaceAll(" ", "");
					}
					if (extension.contains("\t")) {
						this.errors.append(name + " extension contains TAB");
						this.errors.append("\n");
						extension = extension.replaceAll("\t", "");
					}
					if (extension.contains("\n")) {
						this.errors
								.append(name + " extension contains NEWLINE");
						this.errors.append("\n");
						extension = extension.replaceAll("\n", "");
					}
					return extension;
				} else {
					this.errors.append(name + " extension is missing");
					this.errors.append("\n");
					return XSD2JSONConverter.value_not_assigned;
				}
			}
		}
		return null;
	}
	
	private String getModelSettings(final String name,
			final XSObjectList annotations) {
		if (annotations.getLength() == 0) {
			this.errors.append(name
					+ " model settings is missing (no annotations)");
			this.errors.append("\n");
		}
		for (int y = 0; y < annotations.getLength(); y++) {
			final XSObject annotationObj = annotations.item(y);
			if (annotationObj instanceof XSAnnotation) {
				final XSAnnotation annotation = (XSAnnotation) annotationObj;
				final String annot = annotation.getAnnotationString();
				if (annot.contains(XSD2JSONConverter.model_settings)) {
					final int begin = annot
							.indexOf(XSD2JSONConverter.model_settings);
					final int end = annot.indexOf("</", begin);
					String domain = annot.substring(begin, end);
					domain = domain.replace(XSD2JSONConverter.model_settings,
							"");
					if (domain.contains("null")) {
						this.errors.append(name + " model settings is null");
						this.errors.append("\n");
						domain = XSD2JSONConverter.value_not_assigned;
					}
					if (domain.contains(" ")) {
						this.errors.append(name
								+ " model settings contains SPACE");
						this.errors.append("\n");
						domain = domain.replaceAll(" ", "");
					}
					if (domain.contains("\t")) {
						this.errors.append(name
								+ " model settings contains TAB");
						this.errors.append("\n");
						domain = domain.replaceAll("\t", "");
					}
					if (domain.contains("\n")) {
						this.errors.append(name
								+ " model settings contains NEWLINE");
						this.errors.append("\n");
						domain = domain.replaceAll("\n", "");
					}
					return domain;
				} else {
					this.errors.append(name + " model settings is missing");
					this.errors.append("\n");
					return XSD2JSONConverter.value_not_assigned;
				}
			}
		}
		return null;
	}

	private String getDomain(final String name, final XSObjectList annotations) {
		if (annotations.getLength() == 0) {
			this.errors.append(name + " domain is missing (no annotations)");
			this.errors.append("\n");
		}
		for (int y = 0; y < annotations.getLength(); y++) {
			final XSObject annotationObj = annotations.item(y);
			if (annotationObj instanceof XSAnnotation) {
				final XSAnnotation annotation = (XSAnnotation) annotationObj;
				final String annot = annotation.getAnnotationString();
				if (annot.contains(XSD2JSONConverter.domain)) {
					final int begin = annot.indexOf(XSD2JSONConverter.domain);
					final int end = annot.indexOf("</", begin);
					String domain = annot.substring(begin, end);
					domain = domain.replace(XSD2JSONConverter.domain, "");
					if (domain.contains("null")) {
						this.errors.append(name + " domain is null");
						this.errors.append("\n");
						domain = XSD2JSONConverter.value_not_assigned;
					}
					if (domain.contains(" ")) {
						this.errors.append(name + " domain contains SPACE");
						this.errors.append("\n");
						domain = domain.replaceAll(" ", "");
					}
					if (domain.contains("\t")) {
						this.errors.append(name + " domain contains TAB");
						this.errors.append("\n");
						domain = domain.replaceAll("\t", "");
					}
					if (domain.contains("\n")) {
						this.errors.append(name + " domain contains NEWLINE");
						this.errors.append("\n");
						domain = domain.replaceAll("\n", "");
					}
					return domain;
				} else {
					this.errors.append(name + " domain is missing");
					this.errors.append("\n");
					return XSD2JSONConverter.value_not_assigned;
				}
			}
		}
		return null;
	}

	private String getCategory(final String name, final XSObjectList annotations) {
		if (annotations.getLength() == 0) {
			this.errors.append(name + " category is missing (no annotations)");
			this.errors.append("\n");
		}
		for (int y = 0; y < annotations.getLength(); y++) {
			final XSObject annotationObj = annotations.item(y);
			if (annotationObj instanceof XSAnnotation) {
				final XSAnnotation annotation = (XSAnnotation) annotationObj;
				final String annot = annotation.getAnnotationString();
				if (annot.contains(XSD2JSONConverter.category)) {
					final int begin = annot.indexOf(XSD2JSONConverter.category);
					final int end = annot.indexOf("</", begin);
					String category = annot.substring(begin, end);
					category = category.replace(XSD2JSONConverter.category, "");
					if (category.contains("null")) {
						this.errors.append(name + " category is null");
						this.errors.append("\n");
						category = XSD2JSONConverter.value_not_assigned;
					}
					if (category.contains(" ")) {
						this.errors.append(name + " category contains SPACE");
						this.errors.append("\n");
						category = category.replaceAll(" ", "");
					}
					if (category.contains("\t")) {
						this.errors.append(name + " category contains TAB");
						this.errors.append("\n");
						category = category.replaceAll("\t", "");
					}
					if (category.contains("\n")) {
						this.errors.append(name + " category contains NEWLINE");
						this.errors.append("\n");
						category = category.replaceAll("\n", "");
					}
					return category;
				} else {
					this.errors.append(name + " category is missing");
					this.errors.append("\n");
					return XSD2JSONConverter.value_not_assigned;
				}
			}
		}
		return null;
	}

	private Integer getTier(final String name, final XSObjectList annotations) {
		if (annotations.getLength() == 0) {
			this.errors.append(name + " tier is missing (no annotations)");
			this.errors.append("\n");
			return 1;
		}
		for (int y = 0; y < annotations.getLength(); y++) {
			final XSObject annotationObj = annotations.item(y);
			if (annotationObj instanceof XSAnnotation) {
				final XSAnnotation annotation = (XSAnnotation) annotationObj;
				final String annot = annotation.getAnnotationString();
				if (annot.contains(XSD2JSONConverter.tier)) {
					final int begin = annot.indexOf(XSD2JSONConverter.tier);
					final int end = annot.indexOf("</", begin);
					String tierS = annot.substring(begin, end);
					tierS = tierS.replace(XSD2JSONConverter.tier, "");
					if (tierS.contains("null")) {
						this.errors.append(name + " tier is null");
						this.errors.append("\n");
						tierS = "1";
					}
					if (tierS.contains(" ")) {
						this.errors.append(name + " tier contains SPACE");
						this.errors.append("\n");
						tierS = tierS.replaceAll(" ", "");
					}
					if (tierS.contains("\t")) {
						this.errors.append(name + " tier contains TAB");
						this.errors.append("\n");
						tierS = tierS.replaceAll("\t", "");
					}
					if (tierS.contains("\n")) {
						this.errors.append(name + " tier contains NEWLINE");
						this.errors.append("\n");
						tierS = tierS.replaceAll("\n", "");
					}
					return Integer.valueOf(tierS);
				} else {
					this.errors.append(name + " tier is missing");
					this.errors.append("\n");
					return 1;
				}
			}
		}
		return null;
	}

	public void writeLogs(final String path) throws IOException {
		final String errorPath = path + File.separator + "errors.json";
		final File errorsFile = new File(errorPath);
		FileWriter fw = new FileWriter(errorsFile);
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(this.errors.toString());
		bw.close();
		fw.close();
		final String referencesPath = path + File.separator + "references.json";
		final File referencesFile = new File(referencesPath);
		fw = new FileWriter(referencesFile);
		bw = new BufferedWriter(fw);
		bw.write(this.references.toString());
		bw.close();
		fw.close();
	}

	public static void main(final String[] args) {
		final String versionFolder = (XSD2JSONConverter.useProgress
				? XSD2JSONConverter.versionType_Progress
				: XSD2JSONConverter.versionType_Stable);
		final String versionTag = (XSD2JSONConverter.useProgress
				? XSD2JSONConverter.inputFileVersionProgress
				: XSD2JSONConverter.inputFileVersionStable);
		final String fileURL = XSD2JSONConverter.githubPrefix + versionFolder
				+ versionTag + XSD2JSONConverter.fileName;
		final XSD2JSONConverter conv = new XSD2JSONConverter();

		String newOutputFolder = XSD2JSONConverter.outputFolder;
		final File dir = new File(newOutputFolder);
		if (!dir.exists()) {
			dir.mkdir();
		}
		if (XSD2JSONConverter.useProgress) {
			newOutputFolder += XSD2JSONConverter.inputFileVersionProgress;
		} else {
			newOutputFolder += XSD2JSONConverter.inputFileVersionStable;
		}
		final File dir2 = new File(newOutputFolder);
		if (!dir2.exists()) {
			dir2.mkdir();
		}

		final String logsFolder = newOutputFolder;
		try {
			final File currentLink = new File(
					XSD2JSONConverter.currentVersionLink);
			if (currentLink.exists()) {
				currentLink.delete();
			}
			// final Path linkPath = Paths.get(currentLink.getPath());
			// final Path targetPath = Paths.get(newOutputFolder);
			System.out.println(currentLink.getPath());
			System.out.println(newOutputFolder);
			// Files.createSymbolicLink(linkPath, targetPath);
			
			// FIX ME COMMAND SPECIFIC FOR WINDOWS - NEED TO BE REPLACED ON
			// LINUX / MAC
			final String cmd = "cmd /c mklink /j \""
					+ XSD2JSONConverter.currentVersionLink.substring(2)
					+ "\" \"" + newOutputFolder.substring(2) + "\"";
			System.out.println(cmd);
			final Process p = Runtime.getRuntime().exec(cmd, null,
					new File("."));
			while (p.isAlive()) {

			}
			System.out.println(p.exitValue());
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		newOutputFolder += XSD2JSONConverter.outputFolderSingleSchemas;

		final File dir3 = new File(newOutputFolder);
		if (!dir3.exists()) {
			dir3.mkdir();
		} else {
			// ASK TO EMPTY ?
			final int input = JOptionPane.showConfirmDialog(null,
					"Clear directory: " + XSD2JSONConverter.outputFolder + "?",
					"WARNING", JOptionPane.YES_NO_OPTION,
					JOptionPane.WARNING_MESSAGE);
			if (input == 0) {
				final File[] files = dir3.listFiles();
				for (final File f : files) {
					f.delete();
				}
			} // else
				// return;
		}

		try {
			final File f = new File(XSD2JSONConverter.tempSchemaFile);
			if (f.exists()) {
				f.delete();
			}
			final String schemaFileName = logsFolder
					+ XSD2JSONConverter.tempSchemaFile;
			conv.createTempSchemaFile(fileURL, schemaFileName);
			conv.parseXSDFile(schemaFileName);
			conv.retrieveElementList();
			final Map<XSElementDeclaration, Map<String, String>> map = conv
					.parseElements();
			conv.writeJSONFiles(map, newOutputFolder);
			conv.writeLogs(logsFolder);
		} catch (final IOException ex) {
			ex.printStackTrace();
		} catch (final ClassNotFoundException ex) {
			ex.printStackTrace();
		} catch (final InstantiationException ex) {
			ex.printStackTrace();
		} catch (final IllegalAccessException ex) {
			ex.printStackTrace();
		} catch (final ClassCastException ex) {
			ex.printStackTrace();
		}
	}
}
