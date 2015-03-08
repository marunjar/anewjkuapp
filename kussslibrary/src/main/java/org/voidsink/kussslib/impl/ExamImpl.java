package org.voidsink.kussslib.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.voidsink.kussslib.Exam;
import org.voidsink.kussslib.Term;
import org.voidsink.kussslib.Term.TermType;


public class ExamImpl implements Exam {

	private static final String TAG = Exam.class.getSimpleName();

	//TODO: move patterns to separate class
    private static final Pattern courseIdTermPattern = Pattern
            .compile(KusssHandlerImpl.PATTERN_LVA_NR_COMMA_TERM);
    private static final Pattern courseIdPattern = Pattern
            .compile(KusssHandlerImpl.PATTERN_LVA_NR);
    private static final Pattern termPattern = Pattern
            .compile(KusssHandlerImpl.PATTERN_TERM);
    private static final Pattern timePattern = Pattern
            .compile("\\d{2}\\:\\d{2}");
    
    
    private String courseId;
    private Term term;
    private Date dtStart;
    private Date dtEnd;
    private String location;
    private String title;
    private int cid;
    private String description;
    private String info;
    private boolean isRegistered;
    private int maxParticipants;
    private int participants;
    private Date registrationDtStart;
    private Date registrationDtEnd;
    private Date unregistrationDt;
    
    
    ExamImpl(String courseId, Term term, Date dtStart, Date dtEnd, String location,
    		String title, int cid, String description, String info, boolean isRegistered,
    		int maxParticipants, int participants, Date registrationDtStart,
    		Date registrationDtEnd, Date unregistrationDt) {
    	
        this.courseId = courseId;
        this.term = term;
        this.dtStart = dtStart;
        this.dtEnd = dtEnd;
        this.location = location;
        this.title = title;
        this.cid = cid;
        this.description = description;
        this.info = info;
        this.isRegistered = isRegistered;
        this.maxParticipants = maxParticipants;
        this.participants = participants;
        this.registrationDtStart = registrationDtStart;
        this.registrationDtEnd = registrationDtEnd;
        this.unregistrationDt = unregistrationDt;
    }
    
    
	public ExamImpl(Element row, boolean isNewExam) {
		
		this("", null, null, null, "", "", 0, "", "", false, 0, 0, null, null, null);
		
		final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
		Elements columns = row.getElementsByTag("td");
		
        if (isNewExam) {
        	
            // allow only exams that can be selected
            if (columns.size() >= 5 && columns.get(0).select("input").size() == 1) {

                	
                Matcher courseIdTermMatcher = courseIdTermPattern.
                		matcher(columns.get(1).text()); //(courseId,term)
                
                if (courseIdTermMatcher.find()) {
                	
                    String courseIdTerm = courseIdTermMatcher.group();
                    title = columns.get(1).text().substring(0, courseIdTermMatcher.start());

                    Matcher courseIdMatcher = courseIdPattern.matcher(courseIdTerm); //courseId
                    
                    if (courseIdMatcher.find()) {
                        courseId = courseIdMatcher.group();
                    }

                    Matcher termMatcher = termPattern.matcher(courseIdTerm); //term
                    
                    if (termMatcher.find(courseIdMatcher.end())) {
                        term = parseTerm(termMatcher.group());
                    }

                    
                    setDateTimeLocation(columns.get(2).text(), columns.get(3).text());

                    isRegistered = false;
                }
            }
            
        } else {
        	
            if (columns.size() >= 5 && columns.get(4).select("input").size() == 1) {

                	
                Matcher courseIdTermMatcher = courseIdTermPattern.
                		matcher(columns.get(0).text()); //(courseId, term)
                
                if (courseIdTermMatcher.find()) {
                	
                    String courseIdTerm = courseIdTermMatcher.group();
                    title = columns.get(0).text().substring(0, courseIdTermMatcher.start());

                    Matcher courseIdMatcher = courseIdPattern.matcher(courseIdTerm); //courseId
                    
                    if (courseIdMatcher.find()) {
                        courseId = courseIdMatcher.group();
                    }

                    Matcher termMatcher = termPattern.matcher(courseIdTerm); //term
                    
                    if (termMatcher.find(courseIdMatcher.end())) {
                        term = parseTerm(termMatcher.group());
                    }

                    setDateTimeLocation(columns.get(1).text(), columns.get(2).text());

                    isRegistered = (columns.get(0)
                            .getElementsByClass("assignment-inactive")
                            .size() == 0);
                }
            }
        }
	}
	
	
	//TODO: Diese Methode auslagern?
    private Term parseTerm(String termStr) {
    	
    	int year = Integer.parseInt(termStr.substring(0, 4));
    	TermType type = TermType.parseTermType(termStr.substring(4));
    	
    	return new Term (year, type);
    }
    
    
    private void setDateTimeLocation(String dateStr, String timeLocationStr) {
    	
        String[] splitted = timeLocationStr.split("\\/");
        final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyyHH:mm");

        String time = "";
        String startTime = "";
        String endTime = "";
        String location = "";

        try {
            if (splitted.length > 1) {
                time = extractTimeString(splitted[0]);
                location = splitted[1];
            } else {
                time = extractTimeString(splitted[0]);
            }
        } catch (Exception e) {
        	//TODO: Exception handling
            //Log.e(TAG, "cant parse string", e);
            //Analytics.sendException(c, e, false, timeLocation);
        }
        
        int mSign = time.indexOf("-");
        
        startTime = time.substring(0, mSign - 1);
        endTime = time.substring(mSign + 1);
        
        this.location = location;
        
        try {
	        this.dtStart = dateFormat.parse(dateStr + startTime);
	        this.dtEnd = dateFormat.parse(dateStr + endTime);
        }
        catch (ParseException e) {
        	//TODO: Exception handling
        	//Log.e(TAG, "Exam ctor", e);
	        //Analytics.sendException(e, false);
        }
    }

    
    private String extractTimeString(String time) {
        List<String> times = new ArrayList<>();
        // extract times
        Matcher timeMatcher = timePattern.matcher(time);
        while (timeMatcher.find()) {
            times.add(timeMatcher.group());
        }
        // remove duplicates
        int i = 0;
        while (i < times.size() - 1) {
            if (times.get(i).equals(times.get(i + 1))) {
                times.remove(i + 1);
            } else {
                i++;
            }
        }

        // create new time string
        String result = "";
        for (String s : times) {
            if (!result.isEmpty()) {
                result += " - ";
            }
            result += s;
        }

        return result;
    }
    
    
	public boolean isInitialized() {
		return !this.courseId.isEmpty() && this.term != null &&
				this.dtStart != null && this.dtEnd != null;
	}

	
	public void addAdditionalInfo(Element element) {
		// TODO Auto-generated method stub
		
	}

	
	public String getCourseId() {
		return courseId;
	}


	public Term getTerm() {
		return term;
	}


	public Date getDtStart() {
		return dtStart;
	}


	public Date getDtEnd() {
		return dtEnd;
	}


	public String getLocation() {
		return location;
	}


	public String getTitle() {
		return title;
	}


	public int getCid() {
		return cid;
	}


	public String getDescription() {
		return description;
	}


	public String getInfo() {
		return info;
	}


	public boolean isRegistered() {
		return isRegistered;
	}


	public int getMaxParticipants() {
		return maxParticipants;
	}


	public int getParticipants() {
		return participants;
	}


	public Date getRegistrationDtStart() {
		return registrationDtStart;
	}


	public Date getRegistrationDtEnd() {
		return registrationDtEnd;
	}


	public Date getUnRegistrationDt() {
		return unregistrationDt;
	}

}
