
# 4DN Metadata Schema XSD to JSON Converter
This is a converter written in Java that translates an XSD microscopy metadata schema into JSON.

The main function of this Java-encoded component is to transform the XML Schema Definition (XSD) implementation of the 4DN-BINA-OME Data Model into a JSON-based schema, which is subsequently ingested by Micro-Meta App to automatically generate the software GUI and the associated data insertion forms. 

The XSD to JSON Schema converter middleware utilizes the [Xerces2 Java XML Parser](http://xerces.apache.org/xerces2-j/), and the [W3C Java XML bindings libraries](https://www.w3.org/TR/2003/WD-DOM-Level-3-Core-20030609/java-binding.html) to navigate the XSD schema and produces two kinds of version-aware JSON files:

1. A comprehensive JSON file containing an array of the schemas for all necessary individual components that constitute the 4DN-BINA-OME Data Model (e.g., Objective, Filter, or Detector). This comprehensive JSON file is made available on GitHub and is specifically designed to facilitate the remote loading of the schema by web-portal embedded React implementations of the Micro-Meta App. This comprehensive JSON schema is available as an individual file on GitHub and can be found [here](https://github.com/WU-BIMAC/4DNMetadataSchemaXSD2JSONConverter/blob/master/latest/fullSchema.json). 

2. A series of JSON files each containing the schema of individual components, which were designed to be employed by the Electron implementation of the App. These individual schema files are available within a subdirectory of the main repository on GitHub available [here](https://github.com/WU-BIMAC/4DNMetadataSchemaXSD2JSONConverter/tree/master/latest/schemas).

The middleware was specifically designed to maximize flexibility and extensibility. As such, the software allows the introduction of implementation-specific modifications of the resulting JSON schema so that it can be adapted for special purposes. For example, the introduction of a “Version” field allows the validation of whether or not the data being saved is compatible with the specific version of the schema being employed. As a further example, the introduction of the “Category” field allows the organization of different components in specific sub-menus across the sidebar. In order to facilitate the evolution of the model while ensuring back compatibility, the GitHub repository supports versioning by storing all revisions of the output JSON schema.

# Background information

For more information please refer to our recent publications:

1. **A perspective on Microscopy Metadata: data provenance and quality control.**
Maximiliaan Huisman, Mathias Hammer, Alex Rigano, Ulrike Boehm, James J. Chambers, Nathalie Gaudreault, Alison J. North, Jaime A. Pimentel, Damir Sudar, Peter Bajcsy, Claire M. Brown, Alexander D. Corbett, Orestis Faklaris, Judith Lacoste, Alex Laude, Glyn Nelson, Roland Nitschke, David Grunwald, Caterina Strambio-De-Castillia, (2021). Available at: https://arxiv.org/abs/1910.11370

2. **Towards community-driven metadata standards for light microscopy: tiered specifications extending the OME model.**
Mathias Hammer, Maximiliaan Huisman, Alessandro Rigano, Ulrike Boehm, James J. Chambers, Nathalie Gaudreault, Alison J. North, Jaime A. Pimentel, Damir Sudar, Peter Bajcsy, Claire M. Brown, Alexander D. Corbett, Orestis Faklaris, Judith Lacoste, Alex Laude, Glyn Nelson, Roland Nitschke, Farzin Farzam, Carlas Smith, David Grunwald, Caterina Strambio-De-Castillia, (2021). Available at: https://www.biorxiv.org/content/10.1101/2021.04.25.441198v1. doi: https://doi.org/10.1101/2021.04.25.441198
