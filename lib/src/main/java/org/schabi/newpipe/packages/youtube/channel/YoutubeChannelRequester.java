/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.schabi.newpipe.packages.youtube.channel;

import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import javax.annotation.Nonnull;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.localization.ContentCountry;
import org.schabi.newpipe.extractor.localization.Localization;
import org.schabi.newpipe.packages.youtube.YoutubeNextRequester;

/**
 *
 * @author Florian
 */
public class YoutubeChannelRequester extends YoutubeNextRequester {
    private static final String BROWSE_ENDPOINT = "browseEndpoint";
    private static final String BROWSE_ID = "browseId";
    
    private YoutubeChannelValidator validator;
    
    /**
     *
     * @param validator
     */
    public YoutubeChannelRequester(YoutubeChannelValidator validator) {
        this.validator = validator;
    }
    
    /**
     * Take a YouTube channel ID or URL path, resolve it if necessary and return a channel ID.
     *
     * @param idOrPath a YouTube channel ID or URL path
     * @return a YouTube channel ID
     * @throws IOException if a channel resolve request failed
     * @throws ExtractionException if a channel resolve request response could not be parsed or is
     * invalid
     */
    @Nonnull
    public String resolveChannelId(@Nonnull final String idOrPath)
            throws ExtractionException, IOException {
        final String[] channelId = idOrPath.split("/");

        if (channelId[0].startsWith("UC")) {
            return channelId[0];
        }

        // If the URL is not a /channel URL, we need to use the navigation/resolve_url endpoint of
        // the InnerTube API to get the channel id. If this fails or if the URL is not a /channel
        // URL, then no information about the channel associated with this URL was found,
        // so the unresolved url will be returned.
        if (!channelId[0].equals("channel")) {
            final byte[] body = JsonWriter.string(
                    prepareDesktopJsonBuilder(Localization.DEFAULT, ContentCountry.DEFAULT)
                            .value("url", "https://www.youtube.com/" + idOrPath)
                            .done())
                    .getBytes(StandardCharsets.UTF_8);

            final JsonObject jsonResponse = getJsonPostResponse(
                    "navigation/resolve_url", body, Localization.DEFAULT);

            validator.checkIfChannelResponseIsValid(jsonResponse);

            final JsonObject endpoint = jsonResponse.getObject("endpoint");

            final String webPageType = endpoint.getObject("commandMetadata")
                    .getObject("webCommandMetadata")
                    .getString("webPageType", "");

            final JsonObject browseEndpoint = endpoint.getObject(BROWSE_ENDPOINT);
            final String browseId = browseEndpoint.getString(BROWSE_ID, "");

            if (webPageType.equalsIgnoreCase("WEB_PAGE_TYPE_BROWSE")
                    || webPageType.equalsIgnoreCase("WEB_PAGE_TYPE_CHANNEL")
                    && !browseId.isEmpty()) {
                if (!browseId.startsWith("UC")) {
                    throw new ExtractionException("Redirected id is not pointing to a channel");
                }

                return browseId;
            }
        }

        // return the unresolved URL
        return channelId[1];
    }
    
    /**
     * Fetch a YouTube channel tab response, using the given channel ID and tab parameters.
     *
     * <p>
     * Redirections to other channels are supported to up to 3 redirects, which could happen for
     * instance for localized channels or for auto-generated ones. For instance, there are three IDs
     * of the auto-generated "Movies and Shows" channel, i.e. {@code UCuJcl0Ju-gPDoksRjK1ya-w},
     * {@code UChBfWrfBXL9wS6tQtgjt_OQ} and {@code UCok7UTQQEP1Rsctxiv3gwSQ}, and they all redirect
     * to the {@code UClgRkhTL3_hImCAmdLfDE4g} one.
     * </p>
     *
     * @param channelId    a valid YouTube channel ID
     * @param parameters   the parameters to specify the YouTube channel tab; if invalid ones are
     *                     specified, YouTube should return the {@code Home} tab
     * @param localization the {@link Localization} to use
     * @param country      the {@link ContentCountry} to use
     * @return a {@link ChannelResponseData channel response data}
     * @throws IOException if a channel request failed
     * @throws ExtractionException if a channel request response could not be parsed or is invalid
     */
    @Nonnull
    public YoutubeChannelResponseData getChannelResponse(@Nonnull final String channelId,
                                                         @Nonnull final String parameters,
                                                         @Nonnull final Localization localization,
                                                         @Nonnull final ContentCountry country)
            throws ExtractionException, IOException {
        String id = channelId;
        JsonObject ajaxJson = null;

        int level = 0;
        while (level < 3) {
            final byte[] body = JsonWriter.string(prepareDesktopJsonBuilder(
                                    localization, country)
                            .value(BROWSE_ID, id)
                            .value("params", parameters)
                            .done())
                    .getBytes(StandardCharsets.UTF_8);

            final JsonObject jsonResponse = getJsonPostResponse(
                    "browse", body, localization);

            validator.checkIfChannelResponseIsValid(jsonResponse);

            final JsonObject endpoint = jsonResponse.getArray("onResponseReceivedActions")
                    .getObject(0)
                    .getObject("navigateAction")
                    .getObject("endpoint");

            final String webPageType = endpoint.getObject("commandMetadata")
                    .getObject("webCommandMetadata")
                    .getString("webPageType", "");

            final String browseId = endpoint.getObject(BROWSE_ENDPOINT)
                    .getString(BROWSE_ID, "");

            if (webPageType.equalsIgnoreCase("WEB_PAGE_TYPE_BROWSE")
                    || webPageType.equalsIgnoreCase("WEB_PAGE_TYPE_CHANNEL")
                    && !browseId.isEmpty()) {
                if (!browseId.startsWith("UC")) {
                    throw new ExtractionException("Redirected id is not pointing to a channel");
                }

                id = browseId;
                level++;
            } else {
                ajaxJson = jsonResponse;
                break;
            }
        }

        if (ajaxJson == null) {
            throw new ExtractionException("Got no channel response after 3 redirects");
        }

        defaultAlertsCheck(ajaxJson);

        return new YoutubeChannelResponseData(ajaxJson, id);
    }
}
