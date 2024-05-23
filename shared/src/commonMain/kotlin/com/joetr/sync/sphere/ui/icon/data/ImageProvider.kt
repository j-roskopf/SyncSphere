package com.joetr.sync.sphere.ui.icon.data

import org.jetbrains.compose.resources.DrawableResource
import syncsphere.shared.generated.resources.Res
import syncsphere.shared.generated.resources.cat1
import syncsphere.shared.generated.resources.cat10
import syncsphere.shared.generated.resources.cat2
import syncsphere.shared.generated.resources.cat3
import syncsphere.shared.generated.resources.cat4
import syncsphere.shared.generated.resources.cat5
import syncsphere.shared.generated.resources.cat6
import syncsphere.shared.generated.resources.cat7
import syncsphere.shared.generated.resources.cat8
import syncsphere.shared.generated.resources.cat9
import syncsphere.shared.generated.resources.dog1
import syncsphere.shared.generated.resources.dog2
import syncsphere.shared.generated.resources.dog3
import syncsphere.shared.generated.resources.dog4
import syncsphere.shared.generated.resources.dog5
import syncsphere.shared.generated.resources.dog6
import syncsphere.shared.generated.resources.dog7
import syncsphere.shared.generated.resources.dog8
import syncsphere.shared.generated.resources.dog9

object ImageProvider {
    fun images() = listOf(
        Res.drawable.dog1,
        Res.drawable.dog2,
        Res.drawable.dog3,
        Res.drawable.dog4,
        Res.drawable.dog5,
        Res.drawable.dog6,
        Res.drawable.dog7,
        Res.drawable.dog8,
        Res.drawable.dog9,
        Res.drawable.dog1,
        Res.drawable.cat1,
        Res.drawable.cat2,
        Res.drawable.cat3,
        Res.drawable.cat4,
        Res.drawable.cat5,
        Res.drawable.cat6,
        Res.drawable.cat7,
        Res.drawable.cat8,
        Res.drawable.cat9,
        Res.drawable.cat10,
    )
}

internal fun String?.toDrawableRes(): DrawableResource? {
    if (this == null) return null
    return ImageProvider.images().firstOrNull {
        it.toString().contains(this)
    }
}

internal fun DrawableResource.toStringId(): String {
    return this.toString()
}
