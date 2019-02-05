package com.conversify.data.remote.models.profile

/**
 * Created by Manish Bhargav
 */
data class CreateEditProfileRequest(var userName: String? = null,
                                    var fullName: String? = null,
                                    var bio: String? = null,
                                    var website: String? = null,
                                    var email: String? = null,
                                    var gender: String? = null,
                                    var designation: String? = null,
                                    var company: String? = null,
                                    var imageOriginal: String? = null,
                                    var imageThumbnail: String? = null)