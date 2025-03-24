/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.schabi.newpipe.packages.youtube.channel;

import com.grack.nanojson.JsonObject;
import java.io.Serializable;
import javax.annotation.Nonnull;

/**
 * A channel header response.
 *
 * <p>
 * This class allows the distinction between a classic header and a carousel
 * one, used for auto-generated ones like the gaming or music topic channels and
 * for big events such as the Coachella music festival, which have a different
 * data structure and do not return the same properties.
 * </p>
 */
public class YoutubeChannelHeader implements Serializable {

    /**
     * Types of supported YouTube channel headers.
     */
    public enum HeaderType {

        /**
         * A {@code c4TabbedHeaderRenderer} channel header type.
         *
         * <p>
         * This header is returned on the majority of channels and contains the
         * channel's name, its banner and its avatar and its subscriber count in
         * most cases.
         * </p>
         */
        C4_TABBED,
        /**
         * An {@code interactiveTabbedHeaderRenderer} channel header type.
         *
         * <p>
         * This header is returned for gaming topic channels, and only contains
         * the channel's name, its banner and a poster as its "avatar".
         * </p>
         */
        INTERACTIVE_TABBED,
        /**
         * A {@code carouselHeaderRenderer} channel header type.
         *
         * <p>
         * This header returns only the channel's name, its avatar and its
         * subscriber count.
         * </p>
         */
        CAROUSEL,
        /**
         * A {@code pageHeaderRenderer} channel header type.
         *
         * <p>
         * This header returns only the channel's name and its avatar for system
         * channels.
         * </p>
         */
        PAGE
    }

    /**
     * The channel header JSON response.
     */
    @Nonnull
    public final JsonObject json;

    /**
     * The type of the channel header.
     *
     * <p>
     * See the documentation of the {@link HeaderType} class for more details.
     * </p>
     */
    public final HeaderType headerType;

    public YoutubeChannelHeader(@Nonnull final JsonObject json, final HeaderType headerType) {
        this.json = json;
        this.headerType = headerType;
    }
}
