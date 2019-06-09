package io.sharedstreets.tools.builder;

import io.sharedstreets.tools.builder.osm.model.Way;
import io.sharedstreets.tools.builder.tiles.ProtoTileOutputFormat;
import io.sharedstreets.tools.builder.tiles.TilableData;
import io.sharedstreets.tools.builder.transforms.Intersections;
import io.sharedstreets.tools.builder.osm.OSMDataStream;
import io.sharedstreets.tools.builder.transforms.BaseSegments;
import io.sharedstreets.tools.builder.transforms.SharedStreetData;
import io.sharedstreets.tools.builder.util.geo.TileId;
import org.apache.commons.cli.*;
import org.apache.flink.api.java.ExecutionEnvironment;

import org.apache.flink.api.java.tuple.Tuple2;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;

public class ProcessPBF {

    static Logger LOG = LoggerFactory.getLogger(ProcessPBF.class);

    public static boolean DEBUG_OUTPUT = true;

    public static void main(String[] args) throws Exception {

        // create the command line parser
        CommandLineParser parser = new DefaultParser();

        // create the Options
        Options options = new Options();

        options.addOption( Option.builder().longOpt( "input" )
                .desc( "path to input OSM PBF file" )
                .hasArg()
                .required()
                .argName("INPUT-FILE")
                .build() );

        options.addOption( Option.builder().longOpt( "output" )
                .desc( "path to output directory (will be created)" )
                .hasArg()
                .argName("OUTPUT-DIR")
                .build() );

        options.addOption( Option.builder().longOpt( "zlevel" )
                .desc( "tile z-level (default 12)" )
                .hasArg()
                .argName("Z-LEVEL")
                .build() );

        options.addOption( Option.builder().longOpt( "roadClasses" )
                .desc( "road classes (default '6,4,2,1,0')" )
                .hasArg()
                .argName("ROAD-CLASSES")
                .build() );

        String inputFile = "";

        String outputPath = "";

        Integer zLevel = 12 ;

        String roadClasses = "6,4,2,1,0";
        ArrayList<Way.ROAD_CLASS> filteredClasses = new ArrayList<>();

        try {
            // parse the command line arguments
            CommandLine line = parser.parse( options, args );

            if( line.hasOption( "input" ) ) {
                // print the value of block-size
                inputFile = line.getOptionValue( "input" );
            }

            if( line.hasOption( "output" ) ) {
                // print the value of block-size
                outputPath = line.getOptionValue( "output" );
            }

            if(line.hasOption("zlevel")){
                zLevel = Integer.parseInt(line.getOptionValue("zlevel"));
            }

            if(line.hasOption("roadClasses")){
                roadClasses = line.getOptionValue("roadClasses");
            }

            // list of way classes for export tiles (will be sorted to be in sequential order from least to most filtered)
            for (String roadClass : roadClasses.split(",")) {
                int roadClassOrdinal = Integer.parseInt(roadClass);
                filteredClasses.add(Way.ROAD_CLASS.values()[roadClassOrdinal]);
            }
            filteredClasses.sort(new Comparator<Way.ROAD_CLASS>() {
                @Override
                public int compare(Way.ROAD_CLASS o1, Way.ROAD_CLASS o2) {
                    return o2.compareTo(o1);
                }
            });
        }
        catch( Exception exp ) {
            System.out.println( "Unexpected exception:" + exp.getMessage() );
            return;
        }

        File file = new File(inputFile);
        if(!file.exists()) {
            System.out.println( "Input file not found: "  + inputFile);
            return;
        }

        if(!file.getName().endsWith(".pbf")) {
            System.out.println( "Input file must end with .pbf: "  + inputFile);
            return;
        }

        File directory = new File(outputPath);

        if(directory.exists()) {
            System.out.println("Output directory already exists: " + outputPath);
            return;
        }

        final ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();

        // load osm data from PBF input
        OSMDataStream dataStream = new OSMDataStream(inputFile, env);


        for(Way.ROAD_CLASS filteredClass : filteredClasses) {

            OSMDataStream.FilteredWays filteredWays = dataStream.getFilteredWays(filteredClass);

            // create OSM intersections
            Intersections intersections = new Intersections(filteredWays);

            // build internal model for street network
            BaseSegments segments = new BaseSegments(filteredWays, intersections);

            // build SharedStreets references, geometries, intersections and metadata
            SharedStreetData streets = new SharedStreetData(segments);

            ProtoTileOutputFormat outputFormat = new ProtoTileOutputFormat<Tuple2<TileId, TilableData>>(outputPath, filteredClass);

            streets.mergedData(zLevel).output(outputFormat);
        }

        env.execute();

    }

}
