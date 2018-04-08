# SharedStreets Builder

The SharedStreets Builder application converts OpenStreetMap data to [SharedStreets protocol buffer tiles](https://github.com/sharedstreets/sharedstreets-ref-system).

SharedStreets uses this tool to generate and maintain a complete global tile set. Users can operate the tool directly on their OSM or GIS data to develop references for internally maintained data sets.

**Example use**

`java -jar ./sharedstreets-builder-0.1-preview.jar --input data/[osm_input_file].pbf --output ./[tile_output_directory]
`

**Notes**

The builder application is built on Apache Flink. If memory requirements exceed available space, Flink uses a disk-based cache for processing. Processing large OSM data sets may require several hundred gigabytes of free disk space. 

 

**Roadmap**

This library supports bulk processing from OSM PBF data. Support for GIS data processing and conflation has been moved to [client libraries and tools](https://github.com/sharedstreets/sharedstreets-conflator).

- [*v0.1:*](https://github.com/sharedstreets/sharedstreets-builder/releases/tag/0.1-preview) OSM support
- *v0.2:* Adding streetnames and improved OSM Tag filtering (changes pending in [PR #11](https://github.com/sharedstreets/sharedstreets-builder/pull/11))
- v0.3 Add hierarchical filtering for roadClass (per [sharedstreets-ref #20](https://github.com/sharedstreets/sharedstreets-ref-system/issues/20#issuecomment-378055341))
- v0.4 Add intersection turn restrictions 
- v0.5 Add bike ped infrastructure 

