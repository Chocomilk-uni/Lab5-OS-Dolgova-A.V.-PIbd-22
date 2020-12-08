import java.util.ArrayList;
import java.util.Random;

public class CoreWithoutInterrupts {
    //Таблица процессов
    private ArrayList<Process> processArray = new ArrayList<>();

    /*
    Копия таблицы процессов для передачи в ядро с другим способом планирования
    (чтобы сохранить те же рандомно сгенерированные параметры для получения корректных тестов времени выполнения)
     */
    private final ArrayList<Process> processArrayCopy;

    Random random = new Random();
    private Driver driver;
    private final int NUMBER_OF_PROCESSES;
    private final int INPUT_OUTPUT_TIME = 25;
    private int totalTime;

    public CoreWithoutInterrupts(int numberOfProcesses) {
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
    private int totalNumberOfTickets = 50;

    /*
    За основу алгоритма планирования взят фрагмент кода 2-ой лабораторной (лотерейное планирование)
     */
    public void planProcesses() {
        System.out.println("\t\tПланирование процессов без прерываний\n");
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
                        totalTime += INPUT_OUTPUT_TIME;
                    }
                    totalTime += processArray.get(i).getNecessaryWorkingTime();
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
                        totalTime += inputOutputTime;
                    }
                    totalTime += processArray.get(i).getNecessaryWorkingTime();
                    System.out.println("Процесс завершил выполнение\n");
                    processArray.remove(i);
                    totalNumberOfTickets--;
                    break;
                }
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

    public ArrayList<Process> getProcessArrayCopy() {
        return processArrayCopy;
    }

    public int getTotalTime() {
        return totalTime;
    }
}