// Small-project variant (architecture guide §4): one module, layers as packages.
// Split into domain/data/presentation modules once the Car API adapter and Room queue land.
plugins {
    alias(libs.plugins.convention.cmp.feature)
}
