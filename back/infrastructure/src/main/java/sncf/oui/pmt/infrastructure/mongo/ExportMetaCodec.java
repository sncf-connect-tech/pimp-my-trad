/*
 *
 *  * Copyright (C) 2018 VSCT
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package sncf.oui.pmt.infrastructure.mongo;

import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;
import sncf.oui.pmt.domain.export.ExportMetadata;
import sncf.oui.pmt.domain.export.KeySpec;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@Component
public class ExportMetaCodec implements Codec<ExportMetadata> {

    private static final String ID = "_id";
    private static final String EXPORT_ID = "exportId";
    private static final String TIMESTAMP = "timestamp";
    private static final String KEY_SPECS = "keySpecs";
    private static final String PROJECT_NAME = "projectName";
    private static final String KEYSET_ID = "keysetId";
    private static final String KEY_ID = "keyId";

    @Override
    public ExportMetadata decode(BsonReader bsonReader, DecoderContext decoderContext) {

        if (bsonReader.getCurrentBsonType() == BsonType.NULL) {
            bsonReader.readNull();
            return null;
        }

        String exportId = "";
        long timestamp = 0L;
        List<KeySpec> specs = new LinkedList<>();

        bsonReader.readStartDocument();
        while (bsonReader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            String name = bsonReader.readName();
            if (name.equals(EXPORT_ID)) {
                exportId = bsonReader.readString();
            } else if (name.equals(TIMESTAMP)) {
                timestamp = bsonReader.readInt64();
            } else if (name.equals(KEY_SPECS)) {
                bsonReader.readStartArray();
                while (bsonReader.readBsonType() != BsonType.END_OF_DOCUMENT) {
                    readSpec(bsonReader).ifPresent(specs::add);
                }
                bsonReader.readEndArray();
            } else {
                bsonReader.skipValue();
            }
        }
        bsonReader.readEndDocument();

        return new ExportMetadata(exportId, timestamp, specs);
    }

    private Optional<KeySpec> readSpec(BsonReader bsonReader) {

        if (bsonReader.getCurrentBsonType() == BsonType.NULL) {
            bsonReader.readNull();
            return Optional.empty();
        }

        String projectName = "";
        String keysetId = null;
        String keyId = "";

        bsonReader.readStartDocument();
        while (bsonReader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            String name = bsonReader.readName();
            if (name.equals(PROJECT_NAME)) {
                projectName = bsonReader.readString();
            } else if (name.equals(KEYSET_ID)) {
                keysetId = bsonReader.readString();
            } else if (name.equals(KEY_ID)) {
                keyId = bsonReader.readString();
            } else {
                bsonReader.skipValue();
            }
        }
        bsonReader.readEndDocument();
        return Optional.of(new KeySpec(projectName, keysetId, keyId));
    }

    @Override
    public void encode(BsonWriter bsonWriter, ExportMetadata exportMetadata, EncoderContext encoderContext) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeName(ID);
        bsonWriter.writeObjectId(new ObjectId());
        bsonWriter.writeName(EXPORT_ID);
        bsonWriter.writeString(exportMetadata.getExportId());
        bsonWriter.writeName(TIMESTAMP);
        bsonWriter.writeInt64(exportMetadata.getTimestamp());
        bsonWriter.writeName(KEY_SPECS);
        bsonWriter.writeStartArray();
        exportMetadata.streamSpecs()
                .forEach(spec -> writeSpec(bsonWriter, spec));
        bsonWriter.writeEndArray();
        bsonWriter.writeEndDocument();
    }

    private void writeSpec(BsonWriter bsonWriter, KeySpec spec) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeName(PROJECT_NAME);
        bsonWriter.writeString(spec.getProjectName());
        bsonWriter.writeName(KEYSET_ID);
        bsonWriter.writeString(spec.getKeysetId());
        bsonWriter.writeName(KEY_ID);
        bsonWriter.writeString(spec.getKeyId());
        bsonWriter.writeEndDocument();
    }

    @Override
    public Class<ExportMetadata> getEncoderClass() {
        return ExportMetadata.class;
    }
}
