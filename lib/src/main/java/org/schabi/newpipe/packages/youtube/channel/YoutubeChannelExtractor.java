/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.schabi.newpipe.packages.youtube.channel;

import java.io.IOException;

import com.github.kil1s.other.http.HttpDownloader;
import com.grack.nanojson.JsonObject;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.schabi.newpipe.extractor.StreamingService;

import org.schabi.newpipe.extractor.channel.ChannelExtractor;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.stream.StreamInfoItem;

/**
 *
 * @author Florian
 */
public class YoutubeChannelExtractor extends ChannelExtractor {

    // Constants of objects used multiples from channel responses
    private static final String IMAGE = "image";
    private static final String CONTENTS = "contents";
    private static final String CONTENT_PREVIEW_IMAGE_VIEW_MODEL = "contentPreviewImageViewModel";
    private static final String PAGE_HEADER_VIEW_MODEL = "pageHeaderViewModel";
    private static final String TAB_RENDERER = "tabRenderer";
    private static final String CONTENT = "content";
    private static final String METADATA = "metadata";
    private static final String AVATAR = "avatar";
    private static final String THUMBNAILS = "thumbnails";
    private static final String SOURCES = "sources";
    private static final String BANNER = "banner";

    private JsonObject jsonResponse;
    
    private YoutubeChannelDataFinder finder;
    private YoutubeChannelRequester requester;
    private YoutubeChannelValidator validator; 

    @Nullable
    private YoutubeChannelHeader channelHeader;

    private String channelId;

    /**
     * If a channel is age-restricted, its pages are only accessible to logged-in and
     * age-verified users, we get an {@code channelAgeGateRenderer} in this case, containing only
     * the following metadata: channel name and channel avatar.
     *
     * <p>
     * This restriction doesn't seem to apply to all countries.
     * </p>
     */
    @Nullable
    private JsonObject channelAgeGateRenderer;

    public YoutubeChannelExtractor(
            HttpDownloader downloader,
            YoutubeChannelDataFinder finder,
            YoutubeChannelRequester requester,
            YoutubeChannelValidator validator, 
            StreamingService service,
            ListLinkHandler urlIdHandler
    ) {
        super(downloader, service, urlIdHandler);
        this.finder = finder;
        this.requester = requester;
        this.validator = validator;
    }

    @Override
    public void onFetchPage(@Nonnull final HttpDownloader downloader)
            throws IOException, ExtractionException {
        final String channelPath = super.getId();
        final String id = requester.resolveChannelId(channelPath);
        // Fetch Videos tab
        final YoutubeChannelResponseData data = requester.getChannelResponse(
                id, "EgZ2aWRlb3PyBgQKAjoA",
                getExtractorLocalization(), getExtractorContentCountry()
        );

        jsonResponse = data.jsonResponse;
        channelHeader = finder.getChannelHeader(jsonResponse);
        channelId = data.channelId;
        channelAgeGateRenderer = finder.getChannelAgeGateRenderer(jsonResponse);
    }
}
