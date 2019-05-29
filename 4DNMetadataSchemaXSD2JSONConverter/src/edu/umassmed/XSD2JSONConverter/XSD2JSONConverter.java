package edu.umassmed.XSD2JSONConverter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.bootstrap.DOMImplementationRegistry;

import com.sun.org.apache.xerces.internal.impl.xs.XSImplementationImpl;
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
import com.sun.org.apache.xerces.internal.xs.XSTerm;
import com.sun.org.apache.xerces.internal.xs.XSTypeDefinition;

public class XSD2JSONConverter {
	public static boolean useProgress = true;
	public static String inputFileVersion = "v01-05/";
	public static String versionType_Stable = "stable%20version/";
	public static String versionType_Progress = "in%20progress/";
	public static String githubPrefix = "https://raw.githubusercontent.com/WU-BIMAC/MicroscopyMetadata4DNGuidelines/master/Model/";
	public static String fileName = "4DN-OME-Microscopy%20Metadata.xsd";
	public static String outputFile = "schema.xsd";
	
	public static String category = "Category=";
	public static String tier = "Tier=";
	public static String desc = "Description=";
	public static String split = "Split=";
	
	public static String id_attr = "ID";
	public static String tier_attr = "Tier";
	public static String name_attr = "Name";
	
	public static String value_not_assigned = "NA";
	
	public static String generic_cat_attr = "Generic";
	
	public static String microscope_main_instrument = "Instrument";
	public static String microscope_main_body = "MicroscopeBody";
	
	public static String subComponents_category = "ChildrenElement";
	
	private XSModel model;
	private final List<XSElementDeclaration> elementList;
	
	private final StringBuffer errors, references;
	
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
	
	private void parseXSDFile() throws ClassNotFoundException,
			InstantiationException, IllegalAccessException, ClassCastException {
		System.setProperty(DOMImplementationRegistry.PROPERTY,
				"com.sun.org.apache.xerces.internal.dom.DOMXSImplementationSourceImpl");
		final DOMImplementationRegistry registry = DOMImplementationRegistry
				.newInstance();
		final com.sun.org.apache.xerces.internal.impl.xs.XSImplementationImpl impl = (XSImplementationImpl) registry
				.getDOMImplementation("XS-Loader");
		final XSLoader schemaLoader = impl.createXSLoader(null);
		this.model = schemaLoader.loadURI(XSD2JSONConverter.outputFile);
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
						if (annot.contains(XSD2JSONConverter.category)) {
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
			final Map<XSElementDeclaration, Map<String, String>> map)
			throws IOException {
		XSElementDeclaration instrument = null;
		XSElementDeclaration microscopeBody = null;
		final List<String> jsons = new ArrayList<String>();
		for (final XSElementDeclaration element : map.keySet()) {
			if (element.getName().equals(
					XSD2JSONConverter.microscope_main_instrument)) {
				instrument = element;
			} else if (element.getName().equals(
					XSD2JSONConverter.microscope_main_body)) {
				microscopeBody = element;
			} else {
				final List<String> compsJson = this.writeComponentJSONFile(
						element, map.get(element));
				jsons.addAll(compsJson);
			}
		}
		
		if ((instrument != null) && (microscopeBody != null)) {
			final Map<String, String> microscopeMap = new LinkedHashMap<String, String>();
			final Map<String, String> instrumentMap = map.get(instrument);
			microscopeMap.putAll(instrumentMap);
			final Map<String, String> microscopeBodyMap = map
					.get(microscopeBody);
			microscopeMap.putAll(microscopeBodyMap);
			final String micJson = this.writeMicroscopeJSONFile(instrument,
					microscopeBody, microscopeMap);
			jsons.add(0, micJson);
		}
		
		final File f = new File("./fullSchema.json");
		final FileWriter fw = new FileWriter(f);
		final BufferedWriter bw = new BufferedWriter(fw);
		bw.write("[\n");
		for (int i = 0; i < jsons.size(); i++) {
			final String json = jsons.get(i);
			bw.write(json);
			if (i < (jsons.size() - 1)) {
				bw.write(",\n");
			} else {
				bw.write("\n");
			}
		}
		bw.write("]");
		bw.close();
		fw.close();
	}
	
	private String writeMicroscopeJSONFile(
			final XSElementDeclaration instrument,
			final XSElementDeclaration microscopeBody,
			final Map<String, String> map) throws IOException {
		final XSObjectList annotations = instrument.getAnnotations();
		final XSTypeDefinition instrumentTypeDef = instrument
				.getTypeDefinition();
		final XSTypeDefinition microscopeBodyTypeDef = microscopeBody
				.getTypeDefinition();
		final String name = instrument.getName();
		if ((instrumentTypeDef instanceof XSComplexTypeDefinition)
				&& (microscopeBodyTypeDef instanceof XSComplexTypeDefinition)) {
			// final XSComplexTypeDefinition instrumentComplTypeDef =
			// (XSComplexTypeDefinition) instrumentTypeDef;
			final XSComplexTypeDefinition microscopeBodyComplTypeDef = (XSComplexTypeDefinition) microscopeBodyTypeDef;
			
			final List<String> attributes = new ArrayList<String>();
			final List<String> subCategoriesOrder = this.getSubCategoriesOrder(
					microscopeBody, attributes);
			subCategoriesOrder.add(0, XSD2JSONConverter.generic_cat_attr);
			
			final Integer tier = this.getTier(name, annotations);
			final String desc = this.getDescription(name, annotations);
			final String image = "Microscope_default.png";
			
			final StringBuffer sb = new StringBuffer();
			sb.append("{\n");
			sb.append("\t\"$schema\":\"http://json-schema.org/draft-07/schema\",\n");
			sb.append("\t\"ID\":\"Microscope.json\",\n");
			sb.append("\t\"type\":\"object\",\n");
			sb.append("\t\"title\":\"Microscope\",\n");
			sb.append("\t\"description\":\"" + desc + "\",\n");
			sb.append("\t\"image\":\"" + image + "\",\n");
			sb.append("\t\"tier\":" + tier + ",\n");
			sb.append("\t\"subCategoriesOrder\": [\n");
			for (int i = 0; i < subCategoriesOrder.size(); i++) {
				sb.append("\t\t\"" + subCategoriesOrder.get(i) + "\"");
				if (i < (subCategoriesOrder.size() - 1)) {
					sb.append(",\n");
				} else {
					sb.append("\n");
				}
			}
			sb.append("\t],\n");
			sb.append("\t\"properties\": {\n");
			final XSObjectList attrList = microscopeBodyComplTypeDef
					.getAttributeUses();
			final List<String> required = new ArrayList<String>();
			int currentIndex = sb.length();
			int previousIndex = sb.length();
			boolean insert = false;
			for (int i = 0; i < attrList.getLength(); i++) {
				insert = false;
				final StringBuffer aSB = new StringBuffer();
				// use="optional" type="xsd:string"
				final XSObject obj = attrList.item(i);
				if (obj instanceof XSAttributeUse) {
					final XSAttributeUse attributeUse = (XSAttributeUse) obj;
					final XSAttributeDeclaration attribute = attributeUse
							.getAttrDeclaration();
					final XSObjectList attrAnnotations = attribute
							.getAnnotations();
					final String attrName = attribute.getName();
					final Integer attrTier = this.getTier(attrName,
							attrAnnotations);
					final String attrDesc = this.getDescription(attrName,
							attrAnnotations);
					
					final String attrCategory;
					if (attrName.equalsIgnoreCase(XSD2JSONConverter.id_attr)
							|| attrName.equals(XSD2JSONConverter.name_attr)) {
						if (attrName.equals(XSD2JSONConverter.name_attr)) {
							insert = true;
						}
						attrCategory = XSD2JSONConverter.generic_cat_attr;
					} else {
						attrCategory = map.get(attrName);
					}
					if (attributeUse.getRequired()) {
						required.add(attrName);
					}
					String attrType;
					if ((attributeUse.getActualVCType() == XSConstants.INTEGER_DT)
							|| (attributeUse.getActualVCType() == XSConstants.SHORT_DT)) {
						attrType = "integer";
					} else if ((attributeUse.getActualVCType() == XSConstants.FLOAT_DT)
							|| (attributeUse.getActualVCType() == XSConstants.DOUBLE_DT)) {
						attrType = "number";
					} else if (attributeUse.getActualVCType() == XSConstants.BOOLEAN_DT) {
						attrType = "boolean";
					} else {
						attrType = "string";
					}
					aSB.append("\t\t\"" + attrName + "\": {\n");
					aSB.append("\t\t\t\"type\":\"" + attrType + "\",\n");
					aSB.append("\t\t\t\"description\":\"" + attrDesc + "\",\n");
					aSB.append("\t\t\t\"tier\":" + attrTier + ",\n");
					aSB.append("\t\t\t\"category\":\"" + attrCategory + "\"");
					if (attrName.equals(XSD2JSONConverter.tier_attr)
							|| attrName.equals(XSD2JSONConverter.id_attr)) {
						aSB.append(",\n");
						aSB.append("\t\t\t\"readonly\":true\n");
					} else {
						aSB.append("\n");
					}
					aSB.append("\t\t}");
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
			sb.append("\t\t\"tier\": {\n");
			sb.append("\t\t\t\"type\":\"integer\",\n");
			sb.append("\t\t\t\"description\":\"The tier level of the microscope.\",\n");
			sb.append("\t\t\t\"tier\":" + tier + ",\n");
			sb.append("\t\t\t\"category\":\""
					+ XSD2JSONConverter.generic_cat_attr + "\",\n");
			sb.append("\t\t\t\"readonly\":true\n");
			sb.append("\t\t}\n");
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
			final File f = new File("./schemas/Microscope.json");
			final FileWriter fw = new FileWriter(f);
			final BufferedWriter bw = new BufferedWriter(fw);
			bw.write(sb.toString());
			bw.close();
			fw.close();
			return sb.toString();
		}
		return null;
	}
	
	private List<XSParticle> getAllParticles(final XSParticle containerParticle) {
		final List<XSParticle> list = new ArrayList<XSParticle>();
		final XSTerm containerTerm = containerParticle.getTerm();
		if (containerTerm instanceof XSElementDeclaration) {
			list.add(containerParticle);
			return list;
		}
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
			final XSComplexTypeDefinition complTypeDef) {
		final List<List<String>> returns = new ArrayList<List<String>>();
		final List<String> attributes = new ArrayList<String>();
		final List<String> required = new ArrayList<String>();
		for (int i = 0; i < particles.size(); i++) {
			final StringBuffer aSB = new StringBuffer();
			final XSParticle particle = particles.get(i);
			final XSTerm term = particle.getTerm();
			final int min = particle.getMinOccurs();
			final int max = particle.getMaxOccurs();
			final String maxS = String.valueOf(max);
			final XSElementDeclaration element = (XSElementDeclaration) term;
			final String elementName = element.getName();
			this.references
					.append(elementName + " from " + min + " to " + maxS);
			this.references.append("\n");
			if (elementName.contains("AnnotationRef")) {
				final XSObjectList annotations = element.getAnnotations();
				final Integer attrTier = this.getTier(elementName, annotations);
				final String attrDesc = this.getDescription(elementName,
						annotations);
				final String attrName = "Description";
				final String attrType = "string";
				aSB.append("\t\t\"" + attrName + "\": {\n");
				aSB.append("\t\t\t\"title\":\"" + attrName + "\",\n");
				aSB.append("\t\t\t\"type\":\"" + attrType + "\",\n");
				aSB.append("\t\t\t\"description\":\"" + attrDesc + "\",\n");
				aSB.append("\t\t\t\"tier\":" + attrTier + ",\n");
				aSB.append("\t\t\t\"category\":\""
						+ XSD2JSONConverter.generic_cat_attr + "\"");
				aSB.append("\n");
				aSB.append("\t\t}");
				required.add(attrName);
				attributes.add(aSB.toString());
			} else if (elementName.endsWith("Ref")) {
				final XSObjectList annotations = element.getAnnotations();
				final Integer attrTier = this.getTier(elementName, annotations);
				final String attrDesc = this.getDescription(elementName,
						annotations);
				// ("Ref", "")
				final String attrName = elementName.substring(0,
						elementName.length() - 3);
				final String attrType = "string";
				aSB.append("\t\t\"" + attrName + "\": {\n");
				boolean isArray = false;
				if ((max > 1) || (max == -1)) {
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
				aSB.append("\t\t\t\"category\":\"" + attrName + "\",\n");
				if (isArray) {
					aSB.append("\t");
				}
				aSB.append("\t\t\t\"linkTo\":\"" + attrName + "\"\n");
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
				final XSComplexTypeDefinition elementComplTypeDef = (XSComplexTypeDefinition) element
						.getTypeDefinition();
				final XSObjectList attrList = elementComplTypeDef
						.getAttributeUses();
				final XSObjectList annotations = element.getAnnotations();
				final Integer attrTier = this.getTier(elementName, annotations);
				final String attrDesc = this.getDescription(elementName,
						annotations);
				final String attrName = elementName;
				// String attrNameCat = attrName;
				aSB.append("\t\t\"" + attrName + "\": {\n");
				boolean isArray = false;
				if ((max > 1) || (max == -1)) {
					aSB.append("\t\t\t\"type\":\"array\",\n");
					isArray = true;
				} else {
					aSB.append("\t\t\t\"type\":\"object\",\n");
				}
				aSB.append("\t\t\t\"description\":\"" + attrDesc + "\",\n");
				aSB.append("\t\t\t\"tier\":" + attrTier + ",\n");
				// aSB.append("\t\t\t\"schemaID\":\"" + attrName
				// + ".json\",\n");
				if (isArray) {
					aSB.append("\t\t\t\"items\": {\n");
					aSB.append("\t");
				}
				aSB.append("\t\t\t\"properties\": {\n");
				final List<List<String>> attributesAndRequiredLocal = this
						.getAttributesAndRequired(attrName, null, attrList,
								isArray ? 3 : 2);
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
		boolean insert = false;
		final List<String> required = new ArrayList<String>();
		for (int k = 0; k < attrList.getLength(); k++) {
			insert = false;
			final StringBuffer attrSB = new StringBuffer();
			// use="optional" type="xsd:string"
			final XSObject obj = attrList.item(k);
			if (obj instanceof XSAttributeUse) {
				final XSAttributeUse attributeUse = (XSAttributeUse) obj;
				final XSAttributeDeclaration attribute = attributeUse
						.getAttrDeclaration();
				final XSObjectList attrAnnotations = attribute.getAnnotations();
				final String attrName = attribute.getName();
				final Integer attrTier = this
						.getTier(attrName, attrAnnotations);
				final String attrDesc = this.getDescription(attrName,
						attrAnnotations);
				
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
				String attrType;
				if ((attributeUse.getActualVCType() == XSConstants.INTEGER_DT)
						|| (attributeUse.getActualVCType() == XSConstants.SHORT_DT)) {
					attrType = "integer";
				} else if ((attributeUse.getActualVCType() == XSConstants.FLOAT_DT)
						|| (attributeUse.getActualVCType() == XSConstants.DOUBLE_DT)) {
					attrType = "number";
				} else if (attributeUse.getActualVCType() == XSConstants.BOOLEAN_DT) {
					attrType = "boolean";
				} else {
					attrType = "string";
				}
				for (int i = 0; i < extraTabs; i++) {
					attrSB.append("\t");
				}
				attrSB.append("\t\t\"" + attrName + "\": {\n");
				for (int i = 0; i < extraTabs; i++) {
					attrSB.append("\t");
				}
				attrSB.append("\t\t\t\"type\":\"" + attrType + "\",\n");
				for (int i = 0; i < extraTabs; i++) {
					attrSB.append("\t");
				}
				attrSB.append("\t\t\t\"description\":\"" + attrDesc + "\",\n");
				for (int i = 0; i < extraTabs; i++) {
					attrSB.append("\t");
				}
				attrSB.append("\t\t\t\"tier\":" + attrTier + ",\n");
				for (int i = 0; i < extraTabs; i++) {
					attrSB.append("\t");
				}
				attrSB.append("\t\t\t\"category\":\"" + attrCategory + "\"");
				if (attrName.equals(XSD2JSONConverter.tier_attr)
						|| attrName.equals(XSD2JSONConverter.id_attr)) {
					attrSB.append(",\n");
					for (int i = 0; i < extraTabs; i++) {
						attrSB.append("\t");
					}
					attrSB.append("\t\t\t\"readonly\": true\n");
				} else {
					attrSB.append("\n");
				}
				for (int i = 0; i < extraTabs; i++) {
					attrSB.append("\t");
				}
				attrSB.append("\t\t}");
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
		final String category = "\"category\":";
		for (final String s : attributes) {
			if (s.contains(category)) {
				final int index1 = s.indexOf(category) + category.length();
				final int index2 = s.indexOf("\n", index1);
				String cat = s.substring(index1, index2);
				cat = cat.replace(",", "");
				cat = cat.replace("\"", "");
				if (cat.equals(XSD2JSONConverter.generic_cat_attr)) {
					hasGeneric = true;
				} else {
					if (!categoriesNeeded.contains(cat)
							&& !cat.equals(element.getName())) {
						categoriesNeeded.add(cat);
					}
				}
			}
		}
		int insertIndex = 0;
		if (hasGeneric) {
			subCategoriesOrder.add(XSD2JSONConverter.generic_cat_attr);
			insertIndex = 1;
		}
		XSTypeDefinition typeDef = element.getTypeDefinition();
		if (typeDef instanceof XSComplexTypeDefinition) {
			typeDef = typeDef.getBaseType();
			while ((typeDef instanceof XSComplexTypeDefinition)
					&& !typeDef.getName().equals("anyType")) {
				subCategoriesOrder.add(insertIndex, typeDef.getName());
				categoriesNeeded.remove(typeDef.getName());
				typeDef = typeDef.getBaseType();
			}
		}
		subCategoriesOrder.add(element.getName());
		subCategoriesOrder.addAll(categoriesNeeded);
		return subCategoriesOrder;
	}
	
	private List<String> writeComponentJSONFile(
			final XSElementDeclaration element, final Map<String, String> map)
			throws IOException {
		
		final XSObjectList annotations = element.getAnnotations();
		final XSTypeDefinition typeDef = element.getTypeDefinition();
		final String name = element.getName();
		if (typeDef instanceof XSComplexTypeDefinition) {
			final XSComplexTypeDefinition complTypeDef = (XSComplexTypeDefinition) typeDef;
			
			final List<XSParticle> particles = this
					.getChildrenParticleList(complTypeDef);
			
			this.errors.append(name);
			this.errors.append("\n");
			
			this.references.append(name);
			this.references.append("\n");
			
			final List<List<String>> childrenAttributesAndRequired = this
					.getChildrenAttributesAndRequired(particles, name,
							complTypeDef);
			final List<String> childrenAttributes = childrenAttributesAndRequired
					.get(0);
			final List<String> childrenRequired = childrenAttributesAndRequired
					.get(1);

			final XSObjectList attrList = complTypeDef.getAttributeUses();
			final List<List<String>> attributesAndRequired = this
					.getAttributesAndRequired(null, map, attrList, 0);
			final List<String> attributes = attributesAndRequired.get(0);
			final List<String> required = attributesAndRequired.get(1);
			attributes.addAll(childrenAttributes);
			
			final List<String> subCategoriesOrder = this.getSubCategoriesOrder(
					element, attributes);
			// System.out.println(element.getName() + " - " +
			// subCategoriesOrder);
			
			final String originalCategory = this.getCategory(name, annotations);
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

				final String image = newName + ".png";

				final StringBuffer sb = new StringBuffer();
				sb.append("{\n");
				sb.append("\t\"$schema\":\"http://json-schema.org/draft-07/schema\",\n");
				if (isSplit) {
					sb.append("\t\"ID\":\"" + newName + ".json\",\n");
				} else {
					sb.append("\t\"ID\":\"" + name + ".json\",\n");
				}
				sb.append("\t\"type\":\"object\",\n");
				sb.append("\t\"title\":\"" + name + "\",\n");
				sb.append("\t\"description\":\"" + desc + "\",\n");
				sb.append("\t\"category\":\"" + category + "\",\n");
				if (!category.equals(XSD2JSONConverter.subComponents_category)) {
					sb.append("\t\"image\":\"" + image + "\",\n");
				}
				// TODO is this neeeded for subComponent?
				sb.append("\t\"tier\":" + tier + ",\n");
				sb.append("\t\"subCategoriesOrder\": [\n");
				for (int i = 0; i < subCategoriesOrder.size(); i++) {
					sb.append("\t\t\"" + subCategoriesOrder.get(i) + "\"");
					if (i < (subCategoriesOrder.size() - 1)) {
						sb.append(",\n");
					} else {
						sb.append("\n");
					}
				}
				sb.append("\t],\n");
				// sb.append(references);
				// sb.append(",\n");
				sb.append("\t\"properties\": {\n");
				for (final String s : attributes) {
					sb.append(s);
					sb.append(",\n");
				}
				// TODO is this needed for subComponents?
				sb.append("\t\t\"Tier\": {\n");
				sb.append("\t\t\t\"type\":\"integer\",\n");
				sb.append("\t\t\t\"description\":\"The tier level of this component.\",\n");
				sb.append("\t\t\t\"tier\":" + tier + ",\n");
				sb.append("\t\t\t\"category\":\""
						+ XSD2JSONConverter.generic_cat_attr + "\",\n");
				sb.append("\t\t\t\"readonly\":true\n");
				sb.append("\t\t}\n");

				sb.append("\t}");
				required.addAll(childrenRequired);
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
					f = new File("./schemas/" + newName + ".json");
				} else {
					f = new File("./schemas/" + name + ".json");
				}
				final FileWriter fw = new FileWriter(f);
				final BufferedWriter bw = new BufferedWriter(fw);
				bw.write(sb.toString());
				bw.close();
				fw.close();
				comps.add(sb.toString());
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
	
	public void writeLogs() throws IOException {
		final File errorsFile = new File("./errors.json");
		FileWriter fw = new FileWriter(errorsFile);
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(this.errors.toString());
		bw.close();
		fw.close();
		final File referencesFile = new File("./references.json");
		fw = new FileWriter(referencesFile);
		bw = new BufferedWriter(fw);
		bw.write(this.references.toString());
		bw.close();
		fw.close();
	}
	
	public static void main(final String[] args) {
		final String fileURL = XSD2JSONConverter.githubPrefix
				+ (XSD2JSONConverter.useProgress
						? XSD2JSONConverter.versionType_Progress
						: XSD2JSONConverter.versionType_Stable)
				+ XSD2JSONConverter.inputFileVersion
				+ XSD2JSONConverter.fileName;
		final XSD2JSONConverter conv = new XSD2JSONConverter();
		try {
			final File f = new File(XSD2JSONConverter.outputFile);
			if (f.exists()) {
				f.delete();
			}
			conv.createTempSchemaFile(fileURL, XSD2JSONConverter.outputFile);
			conv.parseXSDFile();
			conv.retrieveElementList();
			final Map<XSElementDeclaration, Map<String, String>> map = conv
					.parseElements();
			conv.writeJSONFiles(map);
			conv.writeLogs();
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
