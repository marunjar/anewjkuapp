package org.voidsink.kussslib;


public enum CourseType {
	VL, VO, KV, UE, KS, SE, PR, UNDEFINED;
	
    public static CourseType parseCourseType(String text) {
        text = text.trim().toLowerCase();
        if (text.equals("VL")) {
            return VL;
        } else if (text.equals("VO")) {
            return VO;
        } else if (text.equals("KV")) {
            return KV;
        } else if (text.equals("UE")) {
            return UE;
        } else if (text.equals("KS")) {
            return KS;
        } else if (text.equals("SE")) {
            return SE;
        } else if (text.equals("PR")) {
            return PR;
        } else {
            return UNDEFINED;
        }
    }
}
