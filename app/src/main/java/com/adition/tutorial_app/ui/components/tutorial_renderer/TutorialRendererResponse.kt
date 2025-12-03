package com.adition.tutorial_app.ui.components.tutorial_renderer

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TutorialRendererResponse(val body: Body) {
    val bannerImage: String
        get() = body.ext.adData.bannerImage

    val framingWidth: Double
        get() = body.ext.adData.framingWidth

    val isBlackFraming: Boolean
        get() = body.ext.adData.isBlackFraming

    @Serializable
    data class Body(val ext: Ext)

    @Serializable
    data class Ext(val adData: AdData)

    @Serializable
    data class AdData(
        @SerialName("banner_image")
        val bannerImage: String,
        @SerialName("framing_width")
        val framingWidth: Double,
        @SerialName("is_black_framing")
        val isBlackFraming: Boolean
    )
}
