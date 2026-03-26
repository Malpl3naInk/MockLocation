package ink.moling.mocklocation.data.models

import com.google.gson.annotations.SerializedName

data class OssLicense(
    val project: String,
    val description: String? = null,
    val version: String? = null,
    val developers: List<String> = emptyList(),
    val url: String? = null,
    val year: String? = null,
    val licenses: List<LicenseInfo> = emptyList(),
    val dependency: String? = null
)

data class LicenseInfo(
    @SerializedName("license")
    val license: String,
    @SerializedName("license_url")
    val licenseUrl: String? = null
)
