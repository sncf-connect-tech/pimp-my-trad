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

import sncf.oui.pmt.domain.keyset.KeysetMetadata;
import sncf.oui.pmt.domain.keyset.KeysetMetadataFactory;
import sncf.oui.pmt.domain.keyset.Language;
import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class KeysetMetaCodec implements Codec<KeysetMetadata> {

    private static final String FILES = "files";
    private static final String ID = "id";

    private KeysetMetadataFactory factory;

    @Autowired
    public KeysetMetaCodec(KeysetMetadataFactory factory) {
        this.factory = factory;
    }

    @Override
    public KeysetMetadata decode(BsonReader bsonReader, DecoderContext decoderContext) {

        if (bsonReader.getCurrentBsonType() == BsonType.NULL) {
            bsonReader.readNull();
            return null;
        }

        String id = null;
        Map<Language, String> files = new HashMap<>();

        bsonReader.readStartDocument();
        while (bsonReader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            String name = bsonReader.readName();
            if (name.equals(ID)) {
                id = bsonReader.readString();
            } else if (name.equals(FILES)) {
                bsonReader.readStartDocument();
                while (bsonReader.readBsonType() != BsonType.END_OF_DOCUMENT) {
                    files.put(Language.valueOf(bsonReader.readName()), bsonReader.readString());
                }
                bsonReader.readEndDocument();
            } else {
                bsonReader.skipValue();
            }
        }
        bsonReader.readEndDocument();

        return factory.create(id, files);
    }

    @Override
    public void encode(BsonWriter bsonWriter, KeysetMetadata keysetMetadata, EncoderContext encoderContext) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeName(ID);
        bsonWriter.writeString(keysetMetadata.getId());
        bsonWriter.writeName(FILES);
        bsonWriter.writeStartDocument();
        for (Map.Entry<Language, String> entry : keysetMetadata.getRelativeFiles().entrySet()) {
            bsonWriter.writeName(entry.getKey().toString());
            bsonWriter.writeString(entry.getValue());
        }
        bsonWriter.writeEndDocument();
        bsonWriter.writeEndDocument();
    }

    @Override
    public Class<KeysetMetadata> getEncoderClass() {
        return KeysetMetadata.class;
    }
}
