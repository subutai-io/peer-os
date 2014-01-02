/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *  
 *    http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License. 
 *  
 */
package org.safehaus.Core;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * ...
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$
 */
public class Timestamp{
    private int year;
    private int month;
    private int day;
    private int hour;
    private int minute;
    private int second;
    private int millisecond=-1;

    public Timestamp(String timestamp)
    {
        if(timestamp.contains("Z"))
            parseTimeStamp(timestamp);
        else
        {
            millisecond = -1;
            parseTimeStampInSystemMode(timestamp);
        }
    }

    private void parseTimeStamp(String timestamp)
    {
        year = Integer.parseInt(timestamp.substring(0,timestamp.indexOf("-")));
        timestamp = timestamp.substring(timestamp.indexOf("-")+1);

        month = Integer.parseInt(timestamp.substring(0,timestamp.indexOf("-")));
        timestamp = timestamp.substring(timestamp.indexOf("-")+1);

        day = Integer.parseInt(timestamp.substring(0,timestamp.indexOf("T")));
        timestamp = timestamp.substring(timestamp.indexOf("T")+1);

        hour = Integer.parseInt(timestamp.substring(0,timestamp.indexOf(":")));
        timestamp = timestamp.substring(timestamp.indexOf(":")+1);

        minute = Integer.parseInt(timestamp.substring(0,timestamp.indexOf(":")));
        timestamp = timestamp.substring(timestamp.indexOf(":")+1);

        second = Integer.parseInt(timestamp.substring(0,timestamp.indexOf(".")));
        timestamp = timestamp.substring(timestamp.indexOf(".")+1);

        millisecond = Integer.parseInt(timestamp.substring(0,timestamp.indexOf("Z")));
    }
    public static Timestamp getCurrentTimestamp()
    {
        //Get current time in date format
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        String current = dateFormat.format(date);
        current = current.replaceAll(" ","T");
//        System.out.println(current);

        //Calculate current time in England
        //The reason is in our lxc's which send the data, the local time is set to England
        //which will be changed.
        //TO-DO
        Timestamp currentTimestamp = new Timestamp(current);
        currentTimestamp.setHour(currentTimestamp.getHour()-2);
        return currentTimestamp;

    }
    public static Timestamp getHoursEarlier(Timestamp timestamp, int hour)
    {
        Timestamp lastHour;
        lastHour = new Timestamp(timestamp.toString());
        if(lastHour.getHour() < hour)
        {
            lastHour.setDay(lastHour.getDay()-(int)(Math.ceil(hour/24.0)));
            if((hour%24) > lastHour.getHour())
                lastHour.setHour(lastHour.getHour()+24-(hour%24));
            else
                lastHour.setHour(lastHour.getHour()-(hour%24));
        }
        else
        {
            lastHour.setHour(lastHour.getHour()-hour);
        }
        return lastHour;


    }
    private void parseTimeStampInSystemMode(String timestamp)
    {
        year = Integer.parseInt(timestamp.substring(0,timestamp.indexOf("-")));
        timestamp = timestamp.substring(timestamp.indexOf("-")+1);

        month = Integer.parseInt(timestamp.substring(0,timestamp.indexOf("-")));
        timestamp = timestamp.substring(timestamp.indexOf("-")+1);

        day = Integer.parseInt(timestamp.substring(0,timestamp.indexOf("T")));
        timestamp = timestamp.substring(timestamp.indexOf("T")+1);

        hour = Integer.parseInt(timestamp.substring(0,timestamp.indexOf(":")));
        timestamp = timestamp.substring(timestamp.indexOf(":")+1);

        minute = Integer.parseInt(timestamp.substring(0,timestamp.indexOf(":")));
        timestamp = timestamp.substring(timestamp.indexOf(":")+1);

        second = Integer.parseInt(timestamp);

    }

    @Override
    public String toString() {
        return year +
                "-" + month +
                "-" + day +
                "T" + hour +
                ":" + minute +
                ":" + second
                + (millisecond==-1 ? "" : ":" + millisecond +"Z");
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public int getSecond() {
        return second;
    }

    public void setSecond(int second) {
        this.second = second;
    }

    public int getMillisecond() {
        return millisecond;
    }

    public void setMillisecond(int millisecond) {
        this.millisecond = millisecond;
    }

}