package com.xnglo.font

data class LocalFontOption(val id: String, val displayName: String, val assetFileName: String)

object LocalFonts {
    val ALL: List<LocalFontOption> = listOf(
        // xi38asc
        LocalFontOption("xb38asc", "xNglobNgali", "xb38asc.ttf"),
        LocalFontOption("xe38asc", "xNgloiNgliS", "xe38asc.ttf"),
        LocalFontOption("xg38asc", "xNgloguzraji", "xg38asc.ttf"),
        LocalFontOption("xh38asc", "xNglovinqi", "xh38asc.ttf"),
        LocalFontOption("xj38asc", "xNglojelugu", "xj38asc.ttf"),
        LocalFontOption("xk38asc", "xNgloknRa", "xk38asc.ttf"),
        LocalFontOption("xm38asc", "xNglomlyalxm", "xm38asc.ttf"),
        LocalFontOption("xo38asc", "xNglooriya", "xo38asc.ttf"),
        LocalFontOption("xp38asc", "xNglopnzabi", "xp38asc.ttf"),
        LocalFontOption("xs38asc", "xNglosinvla", "xs38asc.ttf"),
        LocalFontOption("xt38asc", "xNglotmil", "xt38asc.ttf"),
        // xi52asc
        LocalFontOption("xb52asc", "xNglobNgali x52", "xb52asc.ttf"),
        LocalFontOption("xe52asc", "xNgloiNgliS x52", "xe52asc.ttf"),
        LocalFontOption("xg52asc", "xNgloguzraji x52", "xg52asc.ttf"),
        LocalFontOption("xh52asc", "xNglovinqi x52", "xh52asc.ttf"),
        LocalFontOption("xj52asc", "xNglojelugu x52", "xj52asc.ttf"),
        LocalFontOption("xk52asc", "xNgloknRa x52", "xk52asc.ttf"),
        LocalFontOption("xm52asc", "xNglomlyalxm x52", "xm52asc.ttf"),
        LocalFontOption("xo52asc", "xNglooriya x52", "xo52asc.ttf"),
        LocalFontOption("xp52asc", "xNglopnzabi x52", "xp52asc.ttf"),
        LocalFontOption("xs52asc", "xNglosinvla x52", "xs52asc.ttf"),
        LocalFontOption("xt52asc", "xNglotmil x52", "xt52asc.ttf"),
    )
    const val DEFAULT_FONT_ID = "xh38asc"
    fun byId(id: String): LocalFontOption? = ALL.find { it.id == id }
}
