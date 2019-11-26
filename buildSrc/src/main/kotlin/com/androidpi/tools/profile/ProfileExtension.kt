package com.androidpi.tools.profile

/**
 * Created by jastrelax on 2018/7/15.
 */
open class ProfileExtension() {

    var threshold: Long = 0

    constructor(threshold: Long) : this() {
        this.threshold = threshold
    }
}