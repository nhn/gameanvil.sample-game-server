package stress_test;

import com.nhnent.tardis.connector.tcp.AsyncConnectorUser;

public class SampleUserClass extends AsyncConnectorUser {

    private int sendCount = 0;

    public int getSendCount() {
        return sendCount;
    }
    public void incSendCount(){
        ++sendCount;
    }
    public void setSendCount(int sendCount) {
        this.sendCount = sendCount;
    }

}
