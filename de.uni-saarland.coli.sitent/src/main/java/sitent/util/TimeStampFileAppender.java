package sitent.util;

/**
 * Code from http://stackoverflow.com/questions/4629772/filename-with-date-in-log4j
 */

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.FileAppender;

public class TimeStampFileAppender extends FileAppender {

	@Override
	public void setFile(String fileName) {
		if (fileName.indexOf("%timestamp") >= 0) {
			Date d = new Date();
			SimpleDateFormat format = new SimpleDateFormat("yyyy:MM:dd-HH:mm:ss");
			fileName = fileName.replaceAll("%timestamp", format.format(d));
		}
		super.setFile(fileName);
	}

}
