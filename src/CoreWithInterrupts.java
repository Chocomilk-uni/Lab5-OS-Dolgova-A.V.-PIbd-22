import java.util.ArrayList;
import java.util.Random;

public class CoreWithInterrupts {
    //Таблица процессов
    private ArrayList<Process> processArray;

    //Очередь процессов, ожидающих доступа к устройству ввода-вывода
    ArrayList<Process> busyProcessesArray = new ArrayList<>();

    private final int INPUT_OUTPUT_TIME = 25;

    private Driver driver;
    private final int NUMBER_OF_PROCESSES;
    private int totalTime;

    public CoreWithInterrupts(int numberOfProcesses) {
        this.NUMBER_OF_PROCESSES = numberOfProcesses;
        driver = new Driver();
        processArray = new ArrayList<>();
    }

    /*
    Создание процессов:
    Т.к. и в первом, и во втором случае планирования параметры процессов должны быть идентичными,
    инициализируем таблицу процессов копией таблицы из CoreWithoutInterrupts
    */
    public void createProcesses(ArrayList<Process> processArrayCopy) {
        processArray.addAll(0, processArrayCopy);
    }

    //Максимальное число билетов
    private int totalNumberOfTickets = 50;

    /*
    За основу алгоритма планирования взят фрагмент кода 2-ой лабораторной (лотерейное планирование)
     */
    public void planProcesses() {
        System.out.println("\t\tПланирование процессов с прерываниями\n");
        giveLotteryTickets();
        int bankOfTickets = totalNumberOfTickets;
        while (!processArray.isEmpty()) {
            int winningTicket = (int) (1 + Math.random() * (bankOfTickets));

            System.out.println("\tВыиграл билет с номером: " + winningTicket);

            for (int i = 0; i < processArray.size(); i++) {
                if (processArray.get(i).getNumberOfTickets() >= winningTicket) {
                    System.out.println("=> Лотерею выиграл процесс " + processArray.get(i).getProcessID() +
                            "\nПроцесс начал работу");

                    /*
                    Если процессу нужен доступ к устройству ввода-вывода, ядро взаимодействует с драйвером устройства, который
                    опрашивает устройство и инициализирует его работу. Процесс блокируется, вместо него выполняются другие процессы.
                    После окончания операции ввода-вывода процесс продолжает работу.
                     */

                    //Если устройство занято, а процесс запрашивает к нему доступ, добавляем его в очередь
                    if (processArray.get(i).getNeedsIO() && !driver.isDeviceAvailable()) {
                        System.out.println("Процесс запрашивает доступ к устройству ввода-вывода");
                        processArray.get(i).setBlocked(true);
                        busyProcessesArray.add(processArray.get(i));
                        bankOfTickets -= processArray.get(i).getNumberOfTickets();
                        processArray.remove(i);
                        driver.setIOTime(INPUT_OUTPUT_TIME);
                        break;
                    } else if (processArray.get(i).getNeedsIO()) {
                        System.out.println("Процесс " + processArray.get(i).getProcessID() + " начал выполнение.");
                        System.out.println("Процесс запрашивает доступ к устройству ввода-вывода");
                        processArray.get(i).setBlocked(true);
                        driver.operateIO(processArray.get(i).getNecessaryWorkingTime());
                        processArray.get(i).setBlocked(false);
                    }
                    totalTime += processArray.get(i).getNecessaryWorkingTime();
                    System.out.println("Процесс завершил выполнение\n");
                    bankOfTickets -= processArray.get(i).getNumberOfTickets();
                    processArray.remove(i);
                    break;
                } else if (processArray.get(i).getNumberOfTickets() == 1) {
                    System.out.println("=> Лотерею выиграл процесс " + processArray.get(i).getProcessID());

                    if (processArray.get(i).getNeedsIO() && !driver.isDeviceAvailable()) {
                        System.out.println("Процесс запрашивает доступ к устройству ввода-вывода");
                        processArray.get(i).setBlocked(true);
                        busyProcessesArray.add(processArray.get(i));
                        processArray.remove(i);
                        driver.setIOTime(INPUT_OUTPUT_TIME);
                        break;
                    } else if (processArray.get(i).getNeedsIO()) {
                        System.out.println("Процесс " + processArray.get(i).getProcessID() + " начал выполнение.");
                        System.out.println("Процесс запрашивает доступ к устройству ввода-вывода");
                        processArray.get(i).setBlocked(true);
                        driver.operateIO(processArray.get(i).getNecessaryWorkingTime());
                        processArray.get(i).setBlocked(false);
                    }
                    totalTime += processArray.get(i).getNecessaryWorkingTime();
                    System.out.println("Процесс завершил выполнение\n");
                    processArray.remove(i);
                    totalNumberOfTickets--;
                    break;
                }
                else {
                    if (driver.isDeviceAvailable() && !busyProcessesArray.isEmpty()) {
                        System.out.println("Процесс " + busyProcessesArray.get(0).getProcessID() + " завершил выполнение\n");
                        busyProcessesArray.remove(0);
                    }
                }
            }
        }
        //Если в очереди процессов, ожидающих доступ к устройству, ещё остались процессы, они выполняются
        if (!busyProcessesArray.isEmpty()) {
            System.out.println("Процесс " + busyProcessesArray.get(0).getProcessID() + " запрашивает доступ к устройству ввода-вывода");
            totalTime += driver.getIOTime();
            driver.operateIO(driver.getIOTime());
            System.out.println("Операция ввода-вывода завершена. Процесс завершил выполнение\n");
            busyProcessesArray.remove(0);
            while (!busyProcessesArray.isEmpty()) {
                System.out.println("Процесс " + busyProcessesArray.get(0).getProcessID() + " запрашивает доступ к устройству ввода-вывода");
                totalTime += INPUT_OUTPUT_TIME;
                driver.operateIO(INPUT_OUTPUT_TIME);
                System.out.println("Операция ввода-вывода завершена. Процесс завершил выполнение\n");
                busyProcessesArray.remove(0);
            }
        }
    }

    public void giveLotteryTickets() {
        System.out.println("\tРаздача билетов");
        int currentNumberOfTickets = totalNumberOfTickets;

        System.out.println("Текущее количество билетов: " + currentNumberOfTickets);

        for (int i = 0; i < NUMBER_OF_PROCESSES; i++) {
            int numberOfTicketsToGive = (int) (2 + Math.random() * (currentNumberOfTickets + 1));
            if (currentNumberOfTickets - numberOfTicketsToGive >= 0) {
                processArray.get(i).setNumberOfTickets(numberOfTicketsToGive);
                currentNumberOfTickets -= numberOfTicketsToGive;

                System.out.println("Процесс " + processArray.get(i).getProcessID() + " получил билетов: " + numberOfTicketsToGive);
                System.out.println("Осталось билетов: " + currentNumberOfTickets);
            } else if (currentNumberOfTickets > 0) {
                numberOfTicketsToGive = currentNumberOfTickets;
                processArray.get(i).setNumberOfTickets(numberOfTicketsToGive);
                currentNumberOfTickets = 0;
                System.out.println("Процесс " + processArray.get(i).getProcessID() + " получил билетов: " + numberOfTicketsToGive);
                System.out.println("Осталось билетов: " + currentNumberOfTickets);
            }
            //Если билеты кончились, по умолчанию даём оставшимся процессам по 1 билету.
            else {
                processArray.get(i).setNumberOfTickets(1);
                System.out.println("Билеты закончились. Процесс " + processArray.get(i).getProcessID() + " получил 1 билет по умолчанию.");
                totalNumberOfTickets++;
            }
        }
        System.out.println("_____________________________\n");
    }

    public int getTotalTime() {
        return totalTime;
    }
}
