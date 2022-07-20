package com.ribbit.data.local.models

data class PostModel(
        val id: String,
        val name: String
) {
    override fun toString(): String {
        return name
    }
}