package com.conversify.data.remote.aws

import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import com.conversify.ConversifyApp

object S3Utils {
    const val BUCKET_NAME = "conversifybucket"
    private const val IDENTITY_POOL_ID = "us-west-2:cd53aa78-1269-4138-874c-0f6e08cd23c0"
    private val REGION = Regions.US_WEST_2

    /**
     * Returns the URL to the key in the bucket given, using the client's scheme and endpoint.
     * Returns empty string if the given bucket and key cannot be converted to a URL.
     * */
    fun getResourceUrl(key: String): String {
        return S3_CLIENT.getResourceUrl(BUCKET_NAME, key) ?: ""
    }

    val COGNITO_CREDENTIAL_PROVIDER by lazy {
        CognitoCachingCredentialsProvider(
                ConversifyApp.getApplicationContext(),
                IDENTITY_POOL_ID,
                REGION)
    }

    val S3_CLIENT by lazy {
        AmazonS3Client(COGNITO_CREDENTIAL_PROVIDER).apply {
            setRegion(Region.getRegion(REGION))
        }
    }

    val TRANSFER_UTILITY by lazy {
        TransferUtility.builder()
                .s3Client(S3_CLIENT)
                .context(ConversifyApp.getApplicationContext())
                .defaultBucket(BUCKET_NAME)
                .build()
    }
}