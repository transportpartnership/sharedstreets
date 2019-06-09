# SharedStreets Builder

The SharedStreets Builder application converts OpenStreetMap data to [SharedStreets protocol buffer tiles](https://github.com/sharedstreets/sharedstreets-ref-system).

SharedStreets uses this tool to generate and maintain a complete global OSM-dervied tile set. Users can operate the tool directly on their OSM or use a pregenerated global tileset provided by SharedStreets.

Support for non-OSM data sources has been moved to the [sharedstreets-conflator](https://github.com/sharedstreets/sharedstreets-conflator) tool.

**Example use**

`java -jar build/libs/sharedstreets-builder-0.3.1.jar --input data/[osm_input_file].pbf --output ./[tile_output_directory]
`

**Filtered Road Classes**

Since v0.3 this tool generates tiles with hierarchically filtered road classes. Tiles with e.g. suffix `.2.pbf only contain road classes Motorway (0), Trunk (1) and Primary (2).
Per default, tiles up to road class Unclassified (6) are generated. To create tiles up to road class Other (8), you may specify an optional `roadClasses` argument, providing the comma separated road classes tiles should be generated for:

`java -jar build/libs/sharedstreets-builder-0.3.1.jar --input data/[osm_input_file].pbf --output ./[tile_output_directory] --roadClasses 0,1,2,4,6,8
`

**Notes**

The builder application is built on Apache Flink. If memory requirements exceed available space, Flink uses a disk-based cache for processing. Processing large OSM data sets may require several hundred gigabytes of free disk space. 
 

**Roadmap**

- [*v0.1:*](https://github.com/sharedstreets/sharedstreets-builder/releases/tag/0.1-preview) OSM support
- *v0.2:* Add OSM metadata support for support ways per [#9](https://github.com/sharedstreets/sharedstreets-builder/issues/9)
- [*v0.3:*](https://github.com/sharedstreets/sharedstreets-builder/releases/tag/0.3) add hierarchical filtering for roadClass per [sharedstreets-ref-system/#20](https://github.com/sharedstreets/sharedstreets-ref-system/issues/20#issuecomment-381010861)
