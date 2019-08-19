package com.checkIt.data.remote.aws

import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import com.checkIt.CheckItApp

object S3Utils {
    private const val BUCKET_NAME = "pulses3bucket"
    private const val IDENTITY_POOL_ID = "us-west-2:d669ec93-b551-4c89-8ffc-5855a10cff2d"
    private val REGION = Regions.US_WEST_2

    /**
     * Returns the URL to the key in the bucket given, using the client's scheme and endpoint.
     * Returns empty string if the given bucket and key cannot be converted to a URL.
     * */
    fun getResourceUrl(key: String): String {
        return S3_CLIENT.getResourceUrl(BUCKET_NAME, key) ?: ""
    }

    private val COGNITO_CREDENTIAL_PROVIDER by lazy {
        CognitoCachingCredentialsProvider(
                CheckItApp.getApplicationContext(),
                IDENTITY_POOL_ID,
                REGION)
    }

    private val S3_CLIENT by lazy {
        AmazonS3Client(COGNITO_CREDENTIAL_PROVIDER).apply {
            setRegion(Region.getRegion(REGION))
        }
    }

    val TRANSFER_UTILITY: TransferUtility by lazy {
        TransferUtility.builder()
                .s3Client(S3_CLIENT)
                .context(CheckItApp.getApplicationContext())
                .defaultBucket(BUCKET_NAME)
                .build()
    }
}