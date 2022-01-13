<img align="right" src="https://github.com/WU-BIMAC/MicroMetaApp.github.io/blob/master/images/Nature%20Methods_COVER.png">

This software is a **Micro-Meta App** dependency, which was developed as part of a **global community initiative** including the **[4D Nucleome Imaging Working Group](https://www.4dnucleome.org/)**, **[BINA Quality Control and Data Management Working Group](https://www.bioimagingna.org/qc-dm-wg)** and **[QUAREP-LiMi](https://quarep.org/)**. 

> **News!** The works of this **global community effort** resulted in multiple publications featured on a recent **Nature Methods FOCUS ISSUE** dedicated to **[Reporting and reproducibility in microscopy](https://www.nature.com/collections/djiciihhjh)**. 

> **Learn More!** For a thorought description of Micro-Meta App consult our recent **[Nature Methods](https://doi.org/10.1038/s41592-021-01315-z)** and **[BioRxiv.org](https://doi.org/10.1101/2021.05.31.446382)** publications!

**Background** If you want to learn more about the importannce of metadata and quality control to ensure full reproducibility, quality and scientific value in light microscopy, please take a look at our recent publications describing the development of **community-driven light microscopy metadata specifications** (**[Nature Methods](https://doi.org/10.1038/s41592-021-01327-9)** and **[BioRxiv.org](https://doi.org/10.1101/2021.04.25.441198)**) and our _overview manuscript_ entitled **"A perspective on Microscopy Metadata: data provenance and quality control"**, which is available on [ArXiv.org](https://arxiv.org/abs/1910.11370).

# 4DN Metadata Schema XSD to JSON Converter
This is a converter written in Java that translates an XSD microscopy metadata schema into JSON.

The main function of this Java-encoded component is to transform the XML Schema Definition (XSD) implementation of the [4DN-BINA-OME Data Model](https://zenodo.org/record/4710731) into a JSON-based schema, which is subsequently ingested by  [Micro-Meta App](https://wu-bimac.github.io/MicroMetaApp.github.io/) to automatically generate the software GUI and the associated data insertion forms.

The XSD to JSON Schema converter middleware utilizes the [Xerces2 Java XML Parser](http://xerces.apache.org/xerces2-j/), and the [W3C Java XML bindings libraries](https://www.w3.org/TR/2003/WD-DOM-Level-3-Core-20030609/java-binding.html) to navigate the XSD schema and produces two kinds of version-aware JSON files:

1. A comprehensive JSON file containing an array of the schemas for all necessary individual components that constitute the 4DN-BINA-OME Data Model (e.g., Objective, Filter, or Detector). This comprehensive JSON file is specifically designed to facilitate the remote loading of the schema by web-portal embedded React implementations of the Micro-Meta App. The comprehensive JSON schema is available as an individual file on GitHub and can be found [here](https://github.com/WU-BIMAC/4DNMetadataSchemaXSD2JSONConverter/blob/master/latest/fullSchema.json). 

2. A series of JSON files each containing the schema of individual components, which were designed to be employed by the Electron implementation of [Micro-Meta App](https://wu-bimac.github.io/MicroMetaApp.github.io/). These individual schema files are available within a subdirectory of the main repository on GitHub available [here](https://github.com/WU-BIMAC/4DNMetadataSchemaXSD2JSONConverter/tree/master/latest/schemas).

The middleware was specifically designed to maximize flexibility and extensibility. As such, the software allows the introduction of implementation-specific modifications of the resulting JSON schema so that it can be adapted for special purposes. For example, the introduction of a “Version” field allows the validation of whether or not the data being saved is compatible with the specific version of the schema being employed. As a further example, the introduction of the “Category” field allows the organization of different components in specific sub-menus across the sidebar. In order to facilitate the evolution of the model while ensuring back-compatibility, the GitHub repository supports versioning by storing all revisions of the output JSON schema.

# Background information

For more information please refer to our recent publications:

1. **A perspective on Microscopy Metadata: data provenance and quality control.**
Maximiliaan Huisman, Mathias Hammer, Alex Rigano, Ulrike Boehm, James J. Chambers, Nathalie Gaudreault, Alison J. North, Jaime A. Pimentel, Damir Sudar, Peter Bajcsy, Claire M. Brown, Alexander D. Corbett, Orestis Faklaris, Judith Lacoste, Alex Laude, Glyn Nelson, Roland Nitschke, David Grunwald, Caterina Strambio-De-Castillia, (2021). Available at: https://arxiv.org/abs/1910.11370

2. **Towards community-driven metadata standards for light microscopy: tiered specifications extending the OME model.**
Mathias Hammer, Maximiliaan Huisman, Alessandro Rigano, Ulrike Boehm, James J. Chambers, Nathalie Gaudreault, Alison J. North, Jaime A. Pimentel, Damir Sudar, Peter Bajcsy, Claire M. Brown, Alexander D. Corbett, Orestis Faklaris, Judith Lacoste, Alex Laude, Glyn Nelson, Roland Nitschke, Farzin Farzam, Carlas Smith, David Grunwald, Caterina Strambio-De-Castillia, (2021). Available at: https://www.biorxiv.org/content/10.1101/2021.04.25.441198v1. doi: https://doi.org/10.1101/2021.04.25.441198
