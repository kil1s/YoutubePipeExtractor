/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.schabi.newpipe.packages.youtube.channel;

import com.grack.nanojson.JsonObject;
import javax.annotation.Nonnull;

/**
 * Response data object for {@link #getChannelResponse(String, String, Localization,
 * ContentCountry)}, after any redirection in the allowed redirects count
 * ({@code 3}).
 */
public class YoutubeChannelResponseData {

    /**
     * The channel response as a JSON object, after all redirects.
     */
    @Nonnull
    public final JsonObject jsonResponse;

    /**
     * The channel ID after all redirects.
     */
    @Nonnull
    public final String channelId;

    public YoutubeChannelResponseData(
            JsonObject jsonResponse,
            @Nonnull final String channelId
    ) {
        this.jsonResponse = jsonResponse;
        this.channelId = channelId;
    }
}
