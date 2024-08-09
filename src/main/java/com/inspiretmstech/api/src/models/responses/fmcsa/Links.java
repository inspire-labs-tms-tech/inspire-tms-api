package com.inspiretmstech.api.src.models.responses.fmcsa;

import com.google.gson.annotations.SerializedName;

// Links class
public record Links(
        Link basics,
        @SerializedName("cargo carried") Link cargoCarried,
        @SerializedName("operation classification") Link operationClassification,
        @SerializedName("docket numbers") Link docketNumbers,
        @SerializedName("carrier active-For-hire authority") Link carrierActiveForHireAuthority
) {}
