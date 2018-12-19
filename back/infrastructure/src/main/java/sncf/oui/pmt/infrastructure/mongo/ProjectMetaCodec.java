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

import sncf.oui.pmt.domain.project.ProjectMetadataFactory;
import sncf.oui.pmt.domain.project.ProjectMetadata;
import sncf.oui.pmt.domain.keyset.KeysetMetadata;
import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@Component
public class ProjectMetaCodec implements Codec<ProjectMetadata> {

    private Codec<KeysetMetadata> keysetMetadataCodec;
    private ProjectMetadataFactory factory;

    @Autowired
    public ProjectMetaCodec(Codec<KeysetMetadata> keysetMetadataCodec, ProjectMetadataFactory factory) {
        this.keysetMetadataCodec = keysetMetadataCodec;
        this.factory = factory;
    }

    @Override
    public ProjectMetadata decode(BsonReader bsonReader, DecoderContext decoderContext) {

        if (bsonReader.getCurrentBsonType() == BsonType.NULL) {
            bsonReader.readNull();
            return null;
        }

        List<KeysetMetadata> keysetList = new LinkedList<>();
        String id = "";
        String origin = "";
        String projectName = "";
        String projectDir = "";

        bsonReader.readStartDocument();
        while (bsonReader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            String name = bsonReader.readName();
            switch (name) {
                case "keysets":
                    bsonReader.readStartArray();
                    while (bsonReader.readBsonType() != BsonType.END_OF_DOCUMENT) {
                        keysetList.add(keysetMetadataCodec.decode(bsonReader, decoderContext));
                    }
                    bsonReader.readEndArray();
                    break;
                case "origin":
                    origin = bsonReader.readString();
                    break;
                case "projectName":
                    projectName = bsonReader.readString();
                    break;
                case "projectDir":
                    projectDir = bsonReader.readString();
                    break;
                case "_id":
                    id = bsonReader.readObjectId().toString();
                    break;
                default:
                    bsonReader.skipValue();
                    break;
            }
        }
        bsonReader.readEndDocument();

        ProjectMetadata p = factory.create(id, origin, projectName)
                .setProjectDir(projectDir)
                .setKeysets(keysetList);
        keysetList.forEach(m -> m.setProjectRoot(p.getProjectDir()));
        return p;
    }

    @Override
    public void encode(BsonWriter bsonWriter, ProjectMetadata projectMetadata, EncoderContext encoderContext) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeName("_id");
        Optional<String> id = projectMetadata.getId();
        if (id.isPresent()) {
            bsonWriter.writeObjectId(new ObjectId(id.get()));
        } else {
            bsonWriter.writeObjectId(new ObjectId());
        }
        bsonWriter.writeName("origin");
        bsonWriter.writeString(projectMetadata.getOrigin());
        bsonWriter.writeName("projectName");
        bsonWriter.writeString(projectMetadata.getProjectName());
        bsonWriter.writeName("projectDir");
        bsonWriter.writeString(projectMetadata.getProjectDir());
        bsonWriter.writeName("keysets");
        bsonWriter.writeStartArray();
        for (KeysetMetadata meta : projectMetadata.getKeysets()) {
            keysetMetadataCodec.encode(bsonWriter, meta, encoderContext);
        }
        bsonWriter.writeEndArray();
        bsonWriter.writeEndDocument();
    }

    @Override
    public Class<ProjectMetadata> getEncoderClass() {
        return ProjectMetadata.class;
    }
}
