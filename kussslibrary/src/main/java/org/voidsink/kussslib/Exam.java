package org.voidsink.kussslib;

import java.util.Date;


public interface Exam {

	
    /**
     * @return courseID
     */
    public String getCourseId();
    
    
    /**
     * @return term
     */
    public Term getTerm();
    
    
    public Date getDtStart();
    public Date getDtEnd();
    public String getLocation();
    public String getTitle();
    public int getCid();
    public String getDescription();
    public String getInfo();
    public boolean isRegistered();
    
    
    /**
     * @return max number of participants, < 0 if no limit 
     */
    public int getMaxParticipants();
    public int getParticipants();
    
    public Date getRegistrationDtStart();
    public Date getRegistrationDtEnd();
    
    public Date getUnRegistrationDt();
    
}
