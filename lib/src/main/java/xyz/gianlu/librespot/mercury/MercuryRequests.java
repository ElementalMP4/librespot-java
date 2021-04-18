/*
 * Copyright 2021 devgianlu
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

package xyz.gianlu.librespot.mercury;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.spotify.Mercury;
import com.spotify.context.ContextPageOuterClass;
import com.spotify.context.ContextTrackOuterClass;
import com.spotify.metadata.Metadata;
import com.spotify.playlist4.Playlist4ApiProto;
import com.spotify.playlist_annotate3.PlaylistAnnotate3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.gianlu.librespot.common.ProtoUtils;
import xyz.gianlu.librespot.metadata.*;

import java.util.List;

/**
 * @author Gianlu
 */
public final class MercuryRequests {
    private static final String KEYMASTER_CLIENT_ID = "65b708073fc0480ea92a077233ca87bd";

    private MercuryRequests() {
    }

    @NotNull
    public static ProtobufMercuryRequest<Playlist4ApiProto.SelectedListContent> getRootPlaylists(@NotNull String username) {
        return new ProtobufMercuryRequest<>(RawMercuryRequest.get(String.format("hm://playlist/user/%s/rootlist", username)),
                Playlist4ApiProto.SelectedListContent.parser());
    }

    @NotNull
    public static ProtobufMercuryRequest<PlaylistAnnotate3.PlaylistAnnotation> getPlaylistAnnotation(@NotNull PlaylistId id) {
        return new ProtobufMercuryRequest<>(RawMercuryRequest.get(id.toMercuryUri(true)),
                PlaylistAnnotate3.PlaylistAnnotation.parser());
    }

    @NotNull
    public static ProtobufMercuryRequest<Playlist4ApiProto.SelectedListContent> getPlaylist(@NotNull PlaylistId id) {
        return new ProtobufMercuryRequest<>(RawMercuryRequest.get(id.toMercuryUri(false)),
                Playlist4ApiProto.SelectedListContent.parser());
    }

    @NotNull
    public static ProtobufMercuryRequest<Metadata.Track> getTrack(@NotNull TrackId id) {
        return new ProtobufMercuryRequest<>(RawMercuryRequest.get(id.toMercuryUri()), Metadata.Track.parser());
    }

    @NotNull
    public static ProtobufMercuryRequest<Metadata.Artist> getArtist(@NotNull ArtistId id) {
        return new ProtobufMercuryRequest<>(RawMercuryRequest.get(id.toMercuryUri()), Metadata.Artist.parser());
    }

    @NotNull
    public static ProtobufMercuryRequest<Metadata.Album> getAlbum(@NotNull AlbumId id) {
        return new ProtobufMercuryRequest<>(RawMercuryRequest.get(id.toMercuryUri()), Metadata.Album.parser());
    }

    @NotNull
    public static ProtobufMercuryRequest<Metadata.Episode> getEpisode(@NotNull EpisodeId id) {
        return new ProtobufMercuryRequest<>(RawMercuryRequest.get(id.toMercuryUri()), Metadata.Episode.parser());
    }

    @NotNull
    public static ProtobufMercuryRequest<Metadata.Show> getShow(@NotNull ShowId id) {
        return new ProtobufMercuryRequest<>(RawMercuryRequest.get(id.toMercuryUri()), Metadata.Show.parser());
    }

    @NotNull
    public static ProtobufMercuryRequest<Mercury.MercuryMultiGetReply> multiGet(@NotNull String uri, Mercury.MercuryRequest... subs) {
        RawMercuryRequest.Builder request = RawMercuryRequest.newBuilder()
                .setContentType("vnd.spotify/mercury-mget-request")
                .setMethod("GET")
                .setUri(uri);

        Mercury.MercuryMultiGetRequest.Builder multi = Mercury.MercuryMultiGetRequest.newBuilder();
        for (Mercury.MercuryRequest sub : subs)
            multi.addRequest(sub);

        request.addProtobufPayload(multi.build());
        return new ProtobufMercuryRequest<>(request.build(), Mercury.MercuryMultiGetReply.parser());
    }

    @NotNull
    public static JsonMercuryRequest<StationsWrapper> getStationFor(@NotNull String context) {
        return new JsonMercuryRequest<>(RawMercuryRequest.get("hm://radio-apollo/v3/stations/" + context), StationsWrapper.class);
    }

    @NotNull
    public static RawMercuryRequest autoplayQuery(@NotNull String context) {
        return RawMercuryRequest.get("hm://autoplay-enabled/query?uri=" + context);
    }

    @NotNull
    public static JsonMercuryRequest<ResolvedContextWrapper> resolveContext(@NotNull String uri) {
        return new JsonMercuryRequest<>(RawMercuryRequest.get(String.format("hm://context-resolve/v1/%s", uri)), ResolvedContextWrapper.class);
    }

    @NotNull
    public static JsonMercuryRequest<GenericJson> requestToken(@NotNull String deviceId, @NotNull String scope) {
        return new JsonMercuryRequest<>(RawMercuryRequest.get(String.format("hm://keymaster/token/authenticated?scope=%s&client_id=%s&device_id=%s", scope, KEYMASTER_CLIENT_ID, deviceId)), GenericJson.class);
    }

    @NotNull
    public static JsonMercuryRequest<GenericJson> getGenericJson(@NotNull String uri) {
        return new JsonMercuryRequest<>(RawMercuryRequest.get(uri), GenericJson.class);
    }

    @NotNull
    private static String getAsString(@NotNull JsonObject obj, @NotNull String key) {
        JsonElement elm = obj.get(key);
        if (elm == null) throw new IllegalArgumentException("Unexpected null value for " + key);
        else return elm.getAsString();
    }

    public static final class StationsWrapper extends JsonWrapper {

        public StationsWrapper(@NotNull JsonObject obj) {
            super(obj);
        }

        @NotNull
        public String uri() {
            return getAsString(obj, "uri");
        }

        @NotNull
        public List<ContextTrackOuterClass.ContextTrack> tracks() {
            return ProtoUtils.jsonToContextTracks(obj.getAsJsonArray("tracks"));
        }
    }

    public static final class ResolvedContextWrapper extends JsonWrapper {

        public ResolvedContextWrapper(@NotNull JsonObject obj) {
            super(obj);
        }

        @NotNull
        public List<ContextPageOuterClass.ContextPage> pages() {
            return ProtoUtils.jsonToContextPages(obj.getAsJsonArray("pages"));
        }

        @Nullable
        public JsonObject metadata() {
            return obj.getAsJsonObject("metadata");
        }

        @NotNull
        public String uri() {
            return getAsString(obj, "uri");
        }

        @NotNull
        public String url() {
            return getAsString(obj, "url");
        }
    }

    public static final class KeymasterToken extends JsonWrapper {

        public KeymasterToken(@NotNull JsonObject obj) {
            super(obj);
        }
    }

    public static final class GenericJson extends JsonWrapper {

        public GenericJson(@NotNull JsonObject obj) {
            super(obj);
        }
    }
}
