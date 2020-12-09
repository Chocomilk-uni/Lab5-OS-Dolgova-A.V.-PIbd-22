import java.util.ArrayList;
import java.util.Random;

public class Core {
    //Таблица процессов
    private ArrayList<Process> processArray = new ArrayList<>();

    /*
    Копия таблицы процессов для передачи в ядро с другим способом планирования
    (чтобы сохранить те же рандомно сгенерированные параметры для получения корректных тестов времени выполнения)
     */
    private final ArrayList<Process> processArrayCopy;

    //Очередь процессов, ожидающих доступа к устройству ввода-вывода (для планирования с прерываниями)
    ArrayList<Process> busyProcessesArray = new ArrayList<>();

    Random random = new Random();
    private Driver driver;
    private final int NUMBER_OF_PROCESSES;
    private final int INPUT_OUTPUT_TIME = 25;
    private int totalTimeWithoutInterrupts;
    private int totalTimeWithInterrupts;

    public Core(int numberOfProcesses) {
        this.NUMBER_OF_PROCESSES = numberOfProcesses;
        driver = new Driver();
        processArrayCopy = new ArrayList<>();
    }

    /*
    Создание процессов:
    необходимое для выполнения процесса время и надобность обращения процесса
    к устройству ввода-вывода во время своей работы определяются случайным образом.
    */
    public void createProcesses() {
        System.out.println("\tCоздание процессов:\n");
        for (int i = 0; i < NUMBER_OF_PROCESSES; i++) {
            int necessaryWorkingTime = 5 + random.nextInt(200);
            boolean needsInputOutput = random.nextBoolean();
            processArray.add(new Process(i + 1, necessaryWorkingTime, needsInputOutput));
            processArrayCopy.add(new Process(i + 1, necessaryWorkingTime, needsInputOutput));
            System.out.println("Процесс " + processArray.get(i).getProcessID() + " создан\n\tДля его выполнения потребуется " +
                    processArray.get(i).getNecessaryWorkingTime() + " мc процессорного времени\n\tНеобходимость обращения к устройству ввода-вывода: " +
                    processArray.get(i).getNeedsIO());
            System.out.println();
        }
        System.out.println();
    }

    //Максимальное число билетов
    private int totalNumberOfTickets;

    /*
    За основу алгоритма планирования взят фрагмент кода 2-ой лабораторной (лотерейное планирование)
     */
    public void planningWithoutInterrupts() {
        System.out.println("\t\tПланирование процессов без прерываний\n");
        giveLotteryTickets(processArray);
        int bankOfTickets = totalNumberOfTickets;
        while (!processArray.isEmpty()) {
            int winningTicket = (int) (1 + Math.random() * (bankOfTickets));

            System.out.println("\tВыиграл билет с номером: " + winningTicket);

            for (int i = 0; i < processArray.size(); i++) {
                if (processArray.get(i).getNumberOfTickets() >= winningTicket) {
                    System.out.println("=> Лотерею выиграл процесс " + processArray.get(i).getProcessID() +
                            "\nПроцесс начал работу");

                    /*
                    Если процессу нужен доступ к устройству ввода-вывода,
                    работа планировщика процессов приостанавливается.
                    Ядро взаимодействует с драйвером устройства, который опрашивает устройство и ожидает
                    окончания операции ввода-вывода. После этого управление возвращается ОС и процесс
                    продолжает работу.
                     */
                    if (processArray.get(i).getNeedsIO()) {
                        System.out.println("Процесс запрашивает доступ к устройству ввода-вывода");
                        driver.setIOTime(INPUT_OUTPUT_TIME);
                        System.out.println("Драйвер опрашивает устройство...");
                        if (driver.isDeviceAvailable()) {
                            System.out.println("Устройство доступно");
                            driver.operateIO(INPUT_OUTPUT_TIME);
                        }
                        System.out.println("Операция ввода-вывода завершена. Процесс продолжил выполнение");
                        processArray.get(i).setNeedsIO(false);
                        totalTimeWithoutInterrupts += INPUT_OUTPUT_TIME;
                    }
                    totalTimeWithoutInterrupts += processArray.get(i).getNecessaryWorkingTime();
                    System.out.println("Процесс завершил выполнение\n");

                    bankOfTickets -= processArray.get(i).getNumberOfTickets();
                    processArray.remove(i);
                    break;
                }
                else if (processArray.get(i).getNumberOfTickets() == 1) {
                    System.out.println("Лотерею выиграл процесс " + processArray.get(i).getProcessID() +
                            "\nПроцесс начал работу");

                    if (processArray.get(i).getNeedsIO()) {
                        System.out.println("Процесс запрашивает доступ к устройству ввода-вывода");
                        int inputOutputTime = 5 + random.nextInt(30);
                        driver.setIOTime(inputOutputTime);
                        System.out.println("Драйвер опрашивает устройство...");
                        if (driver.isDeviceAvailable()) {
                            System.out.println("Устройство доступно");
                            driver.operateIO(inputOutputTime);
                        }
                        System.out.println("Операция ввода-вывода завершена. Процесс продолжил выполнение");
                        processArray.get(i).setNeedsIO(false);
                        totalTimeWithoutInterrupts += inputOutputTime;
                    }
                    totalTimeWithoutInterrupts += processArray.get(i).getNecessaryWorkingTime();
                    System.out.println("Процесс завершил выполнение\n");
                    processArray.remove(i);
                    totalNumberOfTickets--;
                    break;
                }
            }
        }
    }

    public void planningWithInterrupts() {
        System.out.println("\t\tПланирование процессов с прерываниями\n");
        giveLotteryTickets(processArrayCopy);
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
                    totalTimeWithInterrupts += processArray.get(i).getNecessaryWorkingTime();
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
                    totalTimeWithInterrupts += processArray.get(i).getNecessaryWorkingTime();
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
            totalTimeWithInterrupts += driver.getIOTime();
            driver.operateIO(driver.getIOTime());
            System.out.println("Операция ввода-вывода завершена. Процесс завершил выполнение\n");
            busyProcessesArray.remove(0);
            while (!busyProcessesArray.isEmpty()) {
                System.out.println("Процесс " + busyProcessesArray.get(0).getProcessID() + " запрашивает доступ к устройству ввода-вывода");
                totalTimeWithInterrupts += INPUT_OUTPUT_TIME;
                driver.operateIO(INPUT_OUTPUT_TIME);
                System.out.println("Операция ввода-вывода завершена. Процесс завершил выполнение\n");
                busyProcessesArray.remove(0);
            }
        }
    }

    public void giveLotteryTickets(ArrayList<Process> processArray) {
        totalNumberOfTickets = 50;
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
            }
            else if (currentNumberOfTickets > 0) {
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

    public int getTotalTimeWithoutInterrupts() {
        return totalTimeWithoutInterrupts;
    }

    public int getTotalTimeWithInterrupts() {
        return totalTimeWithInterrupts;
    }
}