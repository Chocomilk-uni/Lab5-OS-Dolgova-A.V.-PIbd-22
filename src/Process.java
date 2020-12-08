public class Process {
    private final int processID;
    private boolean needsInputOutputDevice;
    private boolean isBlocked;
    private int necessaryWorkingTime;
    private int numberOfTickets;

    public Process(int processID, int necessaryWorkingTime, boolean needsIO) {
        this.processID = processID;
        this.necessaryWorkingTime = necessaryWorkingTime;
        this.needsInputOutputDevice = needsIO;

    }

    public int getProcessID() {
        return processID;
    }

    public boolean getNeedsIO() {
        return needsInputOutputDevice;
    }

    public void setNeedsIO(boolean needsInputOutputDevice) {
        this.needsInputOutputDevice = needsInputOutputDevice;
    }

    public int getNecessaryWorkingTime() {
        return necessaryWorkingTime;
    }

    public void setNumberOfTickets(int numberOfTickets) {
        this.numberOfTickets = numberOfTickets;
    }

    public int getNumberOfTickets() {
        return numberOfTickets;
    }

    public void setBlocked(boolean blocked) {
        isBlocked = blocked;
    }
}