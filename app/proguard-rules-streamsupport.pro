#  proguard-rules-streamsupport.pro
#

-target 1.6 #1.6 for api < 19, 1.7 for api < 24
-dontobfuscate

-dontwarn java8.**

-keepattributes *Annotation*,Signature,InnerClasses