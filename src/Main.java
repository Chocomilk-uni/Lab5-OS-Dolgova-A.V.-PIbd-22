public class Main {

    public static void main(String[] args) {
		Core core = new Core(5);
		core.createProcesses();
		core.planningWithoutInterrupts();
		System.out.println();
		core.planningWithInterrupts();

		System.out.println("Время, затраченное на планирование процессов без прерываний: " + core.getTotalTimeWithoutInterrupts() + " мс");
		System.out.println("Время, затраченное на планирование процессов с прерываниями: " +  core.getTotalTimeWithInterrupts() + " мс");
    }
}
