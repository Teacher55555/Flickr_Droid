package com.armageddon.android.flickrdroid.model

class PhotoExif (
    val camera: String,
    val lensModel: String,
    val fNumber: String,
    val focalLength: String,
    val exposureTime: String,
    val iso: String
) {
    val isDataEmpty =
        lensModel.isBlank() &&
        fNumber.isBlank() &&
        focalLength.isBlank() &&
        exposureTime.isBlank() &&
        iso.isBlank()
}