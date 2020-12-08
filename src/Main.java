public class Main {

    public static void main(String[] args) {
		CoreWithoutInterrupts coreWithoutInterrupts = new CoreWithoutInterrupts(5);
		coreWithoutInterrupts.createProcesses();
		coreWithoutInterrupts.planProcesses();
		System.out.println();
		CoreWithInterrupts coreWithInterrupts = new CoreWithInterrupts(5);
		coreWithInterrupts.createProcesses(coreWithoutInterrupts.getProcessArrayCopy());
		coreWithInterrupts.planProcesses();

		System.out.println("Время, затраченное на планирование процессов без прерываний: " + coreWithoutInterrupts.getTotalTime() + " мс");
		System.out.println("Время, затраченное на планирование процессов с прерываниями: " + coreWithInterrupts.getTotalTime() + " мс");
    }
}
