#  proguard-rules-streamsupport.pro
#

-target 1.6 #1.6 for api < 19, 1.7 for api < 24
-dontobfuscate

-keep class java8.** { *; }
-keep interface java8.** { *; }
-dontwarn java8.**

-keepattributes *Annotation*,Signature,InnerClasses