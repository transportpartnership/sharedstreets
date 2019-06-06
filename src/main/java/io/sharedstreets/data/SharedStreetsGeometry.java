package io.sharedstreets.data;


import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.Polyline;
import com.google.protobuf.ByteString;
import com.jsoniter.annotation.JsonIgnore;
import io.sharedstreets.data.output.proto.SharedStreetsProto;
import io.sharedstreets.tools.builder.model.BaseSegment;
import io.sharedstreets.tools.builder.tiles.TilableData;
import io.sharedstreets.tools.builder.util.geo.Geography;
import io.sharedstreets.tools.builder.util.geo.TileId;
import io.sharedstreets.tools.builder.util.UniqueId;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Formatter;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;


public class SharedStreetsGeometry extends TilableData implements Serializable {

    private final static Geography GeoOp = new Geography();

    public UniqueId id;

    public UniqueId startIntersectionId;
    public UniqueId endIntersectionId;
    public UniqueId forwardReferenceId;
    public UniqueId backReferenceId;

    public double length;

    public Geometry geometry;

    @JsonIgnore
    public SharedStreetsOSMMetadata metadata;

    public SharedStreetsGeometry(BaseSegment segment) {

        this.geometry = segment.constructGeometry();
        this.length = GeoOp.length((Polyline)this.geometry);;

        this.id = SharedStreetsGeometry.generateId(this);


        this.metadata = new SharedStreetsOSMMetadata(this, segment);
    }

    public String getType() {
        return "geometry";
    }

    public byte[] toBinary() throws IOException {

        SharedStreetsProto.SharedStreetsGeometry.Builder geometryBuilder = SharedStreetsProto.SharedStreetsGeometry.newBuilder();
        geometryBuilder.setId(this.id.toString());

        geometryBuilder.setFromIntersectionId(this.startIntersectionId.toString());
        geometryBuilder.setToIntersectionId(this.endIntersectionId.toString());

        geometryBuilder.setForwardReferenceId(this.forwardReferenceId.toString());

        if(this.backReferenceId != null)
            geometryBuilder.setBackReferenceId(this.backReferenceId.toString());

        geometryBuilder.setRoadClass(SharedStreetsProto.RoadClass.forNumber(this.metadata.getRoadClass().getValue()));

        for(int i = 0; i < ((Polyline)geometry).getPointCount(); i++) {

            geometryBuilder.addLonlats(((Polyline)geometry).getPoint(i).getX()); // lon
            geometryBuilder.addLonlats(((Polyline)geometry).getPoint(i).getY()); // lat

        }

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        geometryBuilder.build().writeDelimitedTo(bytes);

        return bytes.toByteArray();
    }

    @Override
    @JsonIgnore
    public String getId() {

        if(id == null)
            this.id = generateId(this);

        return this.id.toString();
    }

    @Override
    @JsonIgnore
    public Set<TileId> getTileKeys(int zLevel) {

        HashSet<TileId> tileIdSet = new HashSet<>();

        for(int i = 0; i < ((Polyline)this.geometry).getPointCount(); i++) {

            tileIdSet.add(TileId.lonLatToTileId(zLevel, ((Polyline)this.geometry).getPoint(i).getX(), ((Polyline)this.geometry).getPoint(i).getY()));
        }

        return tileIdSet;
    }

    // generate a stable ref
    public static UniqueId generateId(SharedStreetsGeometry ssg) {
        StringBuilder hashString = new StringBuilder();
        Formatter formatter = new Formatter((Locale) null);

        hashString.append("Geometry");

        for(int i = 0; i < ((Polyline)ssg.geometry).getPointCount(); i++) {
            hashString.append(formatter.format(" %.5f %.5f", ((Polyline)ssg.geometry).getPoint(i).getX(), ((Polyline)ssg.geometry).getPoint(i).getY()));
        }

        return UniqueId.generateHash(hashString.toString());
    }

}
