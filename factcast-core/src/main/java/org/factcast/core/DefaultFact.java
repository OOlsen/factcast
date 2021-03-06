/**
 * Copyright © 2018 Mercateo AG (http://www.mercateo.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.factcast.core;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.factcast.core.util.FactCastJson;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;

/**
 * Note: creating an instance involves deserializing the header from JS. This is
 * probably not optimal considering performance. If you extend FactCast,
 * consider creating a dedicated Fact Impl.
 * 
 * For caching purposes, this thing should be Externalizable.
 * 
 * @see PGFact
 * @author uwe.schaefer@mercateo.com
 *
 */
@EqualsAndHashCode(of = { "deserializedHeader" })
public class DefaultFact implements Fact, Externalizable {

    @Getter
    String jsonHeader;

    @Getter
    String jsonPayload;

    transient Header deserializedHeader;

    // needed for Externalizable – do not use !
    @Deprecated
    public DefaultFact() {
    }

    public static Fact of(@NonNull String jsonHeader, @NonNull String jsonPayload) {
        return new DefaultFact(jsonHeader, jsonPayload);
    }

    @SneakyThrows
    protected DefaultFact(String jsonHeader, String jsonPayload) {

        this.jsonHeader = jsonHeader;
        this.jsonPayload = jsonPayload;
        init(jsonHeader);

    }

    private void init(String jsonHeader) throws IOException {
        deserializedHeader = FactCastJson.readValue(Header.class, jsonHeader);
        if (deserializedHeader.id == null) {
            throw new IOException("id attribute missing from " + jsonHeader);
        }
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    @EqualsAndHashCode(of = { "id" })
    static class Header {

        @JsonProperty
        @NonNull
        UUID id;

        @JsonProperty
        @NonNull
        String ns;

        @JsonProperty
        String type;

        @JsonProperty
        Set<UUID> aggIds;

        @JsonProperty
        final Map<String, String> meta = new HashMap<>();
    }

    @Override
    public String meta(String key) {
        return deserializedHeader.meta.get(key);
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        // write only header & payload
        out.writeUTF(jsonHeader);
        out.writeUTF(jsonPayload);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        // read only header & payload
        jsonHeader = in.readUTF();
        jsonPayload = in.readUTF();
        // and recreate the header fields
        init(jsonHeader);
    }

    @Override
    public UUID id() {
        return deserializedHeader.id();
    }

    @Override
    public String ns() {
        return deserializedHeader.ns();
    }

    @Override
    public String type() {
        return deserializedHeader.type();
    }

    @Override
    public Set<UUID> aggIds() {
        return deserializedHeader.aggIds();
    }

}
