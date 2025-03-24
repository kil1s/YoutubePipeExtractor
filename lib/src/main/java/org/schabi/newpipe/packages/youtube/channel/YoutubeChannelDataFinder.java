/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.schabi.newpipe.packages.youtube.channel;

import com.grack.nanojson.JsonObject;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.packages.youtube.YoutubeNextDataFinder;

/**
 *
 * @author Florian
 */
public class YoutubeChannelDataFinder extends YoutubeNextDataFinder {
    // Constants of objects used multiples from channel responses
    private static final String CAROUSEL_HEADER_RENDERER = "carouselHeaderRenderer";
    private static final String C4_TABBED_HEADER_RENDERER = "c4TabbedHeaderRenderer";
    private static final String CONTENT = "content";
    private static final String CONTENTS = "contents";
    private static final String HEADER = "header";
    private static final String PAGE_HEADER_VIEW_MODEL = "pageHeaderViewModel";
    private static final String TAB_RENDERER = "tabRenderer";
    private static final String TITLE = "title";
    private static final String TOPIC_CHANNEL_DETAILS_RENDERER = "topicChannelDetailsRenderer";

    
    @Nullable
    public JsonObject getChannelAgeGateRenderer(@Nonnull final JsonObject jsonResponse) {
        return jsonResponse.getObject(CONTENTS)
                .getObject("twoColumnBrowseResultsRenderer")
                .getArray("tabs")
                .stream()
                .filter(JsonObject.class::isInstance)
                .map(JsonObject.class::cast)
                .flatMap(tab -> tab.getObject(TAB_RENDERER)
                        .getObject(CONTENT)
                        .getObject("sectionListRenderer")
                        .getArray(CONTENTS)
                        .stream()
                        .filter(JsonObject.class::isInstance)
                        .map(JsonObject.class::cast))
                .filter(content -> content.has("channelAgeGateRenderer"))
                .map(content -> content.getObject("channelAgeGateRenderer"))
                .findFirst()
                .orElse(null);
    }
    
    @Nonnull
    public String getChannelName(@Nullable final YoutubeChannelHeader channelHeader,
                                        @Nullable final JsonObject channelAgeGateRenderer,
                                        @Nonnull final JsonObject jsonResponse)
            throws ParsingException {
        if (channelAgeGateRenderer != null) {
            final String title = channelAgeGateRenderer.getString("channelTitle");
            if (isNullOrEmpty(title)) {
                throw new ParsingException("Could not get channel name");
            }
            return title;
        }

        final String metadataRendererTitle = jsonResponse.getObject("metadata")
                .getObject("channelMetadataRenderer")
                .getString(TITLE);
        if (!isNullOrEmpty(metadataRendererTitle)) {
            return metadataRendererTitle;
        }

        return Optional.ofNullable(channelHeader)
                .map(header -> {
                    final JsonObject channelJson = header.json;
                    switch (header.headerType) {
                        case PAGE:
                            return channelJson.getObject(CONTENT)
                                    .getObject(PAGE_HEADER_VIEW_MODEL)
                                    .getObject(TITLE)
                                    .getObject("dynamicTextViewModel")
                                    .getObject("text")
                                    .getString(CONTENT, channelJson.getString("pageTitle"));
                        case CAROUSEL:
                        case INTERACTIVE_TABBED:
                            return getTextFromObject(channelJson.getObject(TITLE));
                        case C4_TABBED:
                        default:
                            return channelJson.getString(TITLE);
                    }
                })
                // The channel name from a microformatDataRenderer may be different from the one
                // displayed, especially for auto-generated channels, depending on the language
                // requested for the interface (hl parameter of InnerTube requests' payload)
                .or(() -> Optional.ofNullable(jsonResponse.getObject("microformat")
                        .getObject("microformatDataRenderer")
                        .getString(TITLE)))
                .orElseThrow(() -> new ParsingException("Could not get channel name"));
    }
    
    
    /**
     * Get a channel header it if exists.
     *
     * @param channelResponse a full channel JSON response
     * @return a {@link ChannelHeader} or {@code null} if no supported header has been found
     */
    @Nullable
    public YoutubeChannelHeader getChannelHeader(
            @Nonnull final JsonObject channelResponse) {
        final JsonObject header = channelResponse.getObject(HEADER);

        if (header.has(C4_TABBED_HEADER_RENDERER)) {
            return Optional.of(header.getObject(C4_TABBED_HEADER_RENDERER))
                    .map(json -> new ChannelHeader(json, ChannelHeader.HeaderType.C4_TABBED))
                    .orElse(null);
        } else if (header.has(CAROUSEL_HEADER_RENDERER)) {
            return header.getObject(CAROUSEL_HEADER_RENDERER)
                    .getArray(CONTENTS)
                    .stream()
                    .filter(JsonObject.class::isInstance)
                    .map(JsonObject.class::cast)
                    .filter(item -> item.has(TOPIC_CHANNEL_DETAILS_RENDERER))
                    .findFirst()
                    .map(item -> item.getObject(TOPIC_CHANNEL_DETAILS_RENDERER))
                    .map(json -> new YoutubeChannelHeader(json, YoutubeChannelHeader.HeaderType.CAROUSEL))
                    .orElse(null);
        } else if (header.has("pageHeaderRenderer")) {
            return Optional.of(header.getObject("pageHeaderRenderer"))
                    .map(json -> new YoutubeChannelHeader(json, YoutubeChannelHeader.HeaderType.PAGE))
                    .orElse(null);
        } else if (header.has("interactiveTabbedHeaderRenderer")) {
            return Optional.of(header.getObject("interactiveTabbedHeaderRenderer"))
                    .map(json -> new YoutubeChannelHeader(json,
                            YoutubeChannelHeader.HeaderType.INTERACTIVE_TABBED))
                    .orElse(null);
        }

        return null;
    }
    
    
    @Nullable
    public JsonObject getChannelAgeGateRenderer(@Nonnull final JsonObject jsonResponse) {
        return jsonResponse.getObject(CONTENTS)
                .getObject("twoColumnBrowseResultsRenderer")
                .getArray("tabs")
                .stream()
                .filter(JsonObject.class::isInstance)
                .map(JsonObject.class::cast)
                .flatMap(tab -> tab.getObject(TAB_RENDERER)
                        .getObject(CONTENT)
                        .getObject("sectionListRenderer")
                        .getArray(CONTENTS)
                        .stream()
                        .filter(JsonObject.class::isInstance)
                        .map(JsonObject.class::cast))
                .filter(content -> content.has("channelAgeGateRenderer"))
                .map(content -> content.getObject("channelAgeGateRenderer"))
                .findFirst()
                .orElse(null);
    }
}
