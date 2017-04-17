package io.subutai.hub.share.dto;


import java.util.Date;


public class TestDto
{
    private String status1 = "THIS IS TEST STATUS #1, 17.04.2017";
    private String status2 = "THIS IS TEST STATUS #2, 17.04.2017";

    public TestDto()
    {
    }

    public String getStatus1() {
        return status1;
    }

    public void setStatus1(String status1) {
        this.status1 = status1;
    }

    public String getStatus2() {
        return status2;
    }

    public void setStatus2(String status2) {
        this.status2 = status2;
    }
}
