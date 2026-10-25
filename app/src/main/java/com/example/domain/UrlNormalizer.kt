package com.example.domain

import java.net.URI

sealed class UrlValidationResult {
    data class Valid(
        val rawUrl: String,
        val normalizedUrl: String,
        val isInstagramReel: Boolean
    ) : UrlValidationResult()

    data class Invalid(val errorMessage: String) : UrlValidationResult()
}

object UrlNormalizer {
    private val TRACKING_PARAMS = setOf(
        "igsh",
        "igshid",
        "utm_source",
        "utm_medium",
        "utm_campaign",
        "utm_term",
        "utm_content",
        "fbclid",
        "s",
        "hl"
    )

    /**
     * Extracts the first URL found inside a shared text block (e.g., when shared from Instagram
     * where the text might be "Check out this Reel by @creator https://www.instagram.com/reel/...").
     */
    fun extractUrlFromSharedText(sharedText: String): String {
        val trimmed = sharedText.trim()
        val urlRegex = Regex("""https?://[^\s"<>]+""", RegexOption.IGNORE_CASE)
        return urlRegex.find(trimmed)?.value ?: trimmed
    }

    fun validateAndNormalize(input: String): UrlValidationResult {
        val raw = extractUrlFromSharedText(input).trim()
        if (raw.isEmpty()) {
            return UrlValidationResult.Invalid("Please paste an Instagram Reel or video link.")
        }

        val withScheme = if (!raw.startsWith("http://", ignoreCase = true) &&
            !raw.startsWith("https://", ignoreCase = true)
        ) {
            if (raw.contains("instagram.com/", ignoreCase = true) ||
                raw.contains("instagr.am/", ignoreCase = true)
            ) {
                "https://$raw"
            } else {
                return UrlValidationResult.Invalid("URL must start with https:// or http:// (e.g., https://www.instagram.com/reel/...)")
            }
        } else {
            raw
        }

        val uri = try {
            URI(withScheme)
        } catch (e: Exception) {
            return UrlValidationResult.Invalid("That link has invalid characters or formatting. Check the URL and try again.")
        }

        val scheme = uri.scheme?.lowercase() ?: ""
        if (scheme != "http" && scheme != "https") {
            return UrlValidationResult.Invalid("Only http:// and https:// links are supported.")
        }

        val host = uri.host?.lowercase()?.removePrefix("www.")?.removePrefix("m.") ?: ""
        if (host.isBlank() || !host.contains(".")) {
            return UrlValidationResult.Invalid("Please enter a complete web domain (e.g., instagram.com/reel/xyz).")
        }

        val isInstagram = host == "instagram.com" || host == "instagr.am" || host.endsWith(".instagram.com")

        // Normalize path: collapse multiple slashes, canonicalize /reels/ -> /reel/, remove trailing slash
        var path = (uri.path ?: "")
            .replace(Regex("/+"), "/")
            .replace(Regex("^/reels/", RegexOption.IGNORE_CASE), "/reel/")
            .trimEnd('/')

        if (isInstagram && path.isEmpty()) {
            return UrlValidationResult.Invalid("This looks like the Instagram home page, not a specific Reel or post link.")
        }

        // Filter out tracking query parameters
        val cleanQuery = uri.rawQuery
            ?.split("&")
            ?.mapNotNull { pair ->
                val idx = pair.indexOf('=')
                val key = if (idx >= 0) pair.substring(0, idx) else pair
                if (key.lowercase() in TRACKING_PARAMS || key.isBlank()) null else pair
            }
            ?.sorted()
            ?.joinToString("&")
            ?.takeIf { it.isNotEmpty() }

        val canonicalHost = if (isInstagram) "instagram.com" else host
        val normalized = buildString {
            append("https://")
            append(canonicalHost)
            if (path.isNotEmpty()) {
                if (!path.startsWith("/")) append("/")
                append(path)
            }
            if (cleanQuery != null) {
                append("?")
                append(cleanQuery)
            }
        }

        return UrlValidationResult.Valid(
            rawUrl = withScheme,
            normalizedUrl = normalized,
            isInstagramReel = isInstagram
        )
    }
}
