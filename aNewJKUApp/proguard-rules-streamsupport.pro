#  proguard-rules-streamsupport.pro
#

-keep class java8.** { *; }
-keep interface java8.** { *; }
-dontwarn java8.**

-dontobfuscate

#-keepattributes *Annotation*,Signature,InnerClasses
