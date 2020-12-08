public class Driver {
    private int IOTime;
    private boolean isDeviceAvailable = true;

    public boolean isDeviceAvailable() {
        return isDeviceAvailable;
    }

    public void setIOTime(int IOTime) {
        this.IOTime = IOTime;
    }

    public int getIOTime() {
        return IOTime;
    }

    public void operateIO(int time) {
        isDeviceAvailable = false;
        IOTime -= time;
        if (IOTime <= 0) {
            isDeviceAvailable = true;
        }
    }
}