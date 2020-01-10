package com.fueled.search.nearbyfood.util

import android.content.Context
import androidx.annotation.StringRes

/**
 * Created by Kiran.
 */


class ResourceProviderImpl(val context: Context) : ResourceProvider {
    override fun getString(@StringRes resourceIdentifier: Int, vararg arguments: Any): String {
        return if (arguments.isNotEmpty())
            context.resources.getString(resourceIdentifier, *arguments)
        else
            context.resources.getString(resourceIdentifier)
    }
}