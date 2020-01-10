package com.fueled.search.nearbyfood.util

/**
 * Created by Kiran.
 */


interface ResourceProvider {

    fun getString(resourceIdentifier: Int, vararg arguments: Any = arrayOf()): String
}