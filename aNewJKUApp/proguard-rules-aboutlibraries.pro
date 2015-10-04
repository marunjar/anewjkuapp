#  proguard-rules-aboutlibraries.pro
#

# Exclude R from ProGuard to enable the library auto detection
-keep class .R
-keep class **.R$* {
    <fields>;
}