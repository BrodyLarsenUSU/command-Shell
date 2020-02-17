import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.io.IOException;
import java.lang.Process;
import java.lang.ProcessBuilder;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.io.File;
import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.sun.tools.attach.VirtualMachine.list;

public class Assign3 {
    public static void main(String[] args) {
        boolean run = true;
        ArrayList<String> inputList = new ArrayList<>();
        while (run) {
            String command = getInput();
            inputList.add(command);
            if (command.equals("exit")) {
                run = false;
                stop();
            } else if (command.equals("ptime")) { //TODO this gives an invalid command
                ptime(command);
            } else if (command.equals("list")) {
                list();
            } else if (command.equals("history")) { //TODO this gives an invalid command at the end evan though it works
                getHistory(inputList, inputList.size());
            } else if (command.contains("cd")) {
                cd(command);
            } else if (command.contains("history last")) {
                String[] input = command.split(" ");
                int number = Integer.parseInt(input[2]);
                getNumberedHistory(inputList, number);
            } else if (command.contains(("history ^ "))) {
                String[] input = command.split(" ");
                int number = Integer.parseInt(input[2]);
                historyInput(inputList, number);
            } else if (command.contains("mdir")) {
                makeDir(command);
            } else if (command.contains("rdir")) {
                removeDir(command);
            } else if (command.contains("|")) {
                pipe(command);
            } else {
                System.out.println("Invalid Command: " + command);
            }
        }

    }

    private static void getHistory(ArrayList inputList, int lengthList) {
        System.out.println("--Command History--");
        int counter = 1;
        for (int i = 0; i < lengthList; i++) {
            System.out.println(counter + " : " + inputList.get(i));
            counter++;
        }
    }

    private static void getNumberedHistory(ArrayList inputList, int num) {
        ArrayList<Object> tempList = new ArrayList<>();
        //adding past num things to list
        int counter = 0;
        for (int i = 0; i < num; i++) {
            try {
                tempList.add(inputList.get(inputList.size() - (num - i)));
                counter++;
                if (counter == num) {
                    continue;
                }
            } catch (Exception ex) {
                continue;
            }
        }
        //printing out past num objects
        for (int i = 0; i < tempList.size(); i++) {
            System.out.println(tempList.get(i));
        }
    }

    private static void historyInput(ArrayList inputList, int num) {
        boolean loop = true;
        while (loop) {
            if(inputList.size() == 1){
                System.out.println("No previous commands have been given");
                break;
            }
            if (inputList.size() < num) {
                num = inputList.size();
                System.out.println("Number provided was bigger than history log. The first command in history log was given instead");
            }
            Object tempInput = inputList.get(inputList.size() - num);
            String newInput = tempInput.toString();
            try {
                if (newInput.equals("exit")) {
                    stop();
                } else if (newInput.equals("ptime")) {
                    ptime(newInput);
                } else if (newInput.equals("list")) {
                    list();
                } else if (newInput.equals("history")) {
                    getHistory(inputList, inputList.size());
                } else if (newInput.contains("cd")) {
                    cd(newInput);
                } else if (newInput.contains("history last")) {
                    String[] input = newInput.split(" ");
                    int number = Integer.parseInt(input[2]);
                    getNumberedHistory(inputList, number);
                } else if (newInput.contains(("history ^ "))) {
                    System.out.println("Error: The command found was also a 'history ^ #' command and an infinite loop would have occured");
                    System.out.println("Don't worry I stopped that from happening");
                } else if (newInput.contains("mdir")) {
                    makeDir(newInput);
                } else if (newInput.contains("rdir")) {
                    removeDir(newInput);
                } else if (newInput.contains("|")) {
                    pipe(newInput);
                } else {
                    System.out.println("Invalid Command: " + newInput);
                }
            } catch (Exception ex) {
                System.out.println("fail");
            }
            loop = false;
        }
    }


    private static void list() {
        String currentDir = System.getProperty("user.dir");
        File fileDir = new File(currentDir);
        String[] temp = fileDir.list();
        File[] temp2 = fileDir.listFiles();
        try {
            if (!temp2[0].exists()) {
                System.out.println("Empty File Directory1");
            } else {
                for (int i = 0; i < temp.length; i++) {
                    String dir = "-";
                    String read = "-";
                    String write = "-";
                    String execute = "-";
                    long fileSize = 0;
                    long lastMod = 0;
                    if (temp2[i].isDirectory()) {
                        dir = "d";
                    }
                    if (temp2[i].canRead()) {
                        read = "r";
                    }
                    if (temp2[i].canWrite()) {
                        write = "w";
                    }
                    if (temp2[i].canExecute()) {
                        execute = "x";
                    }
                    fileSize = temp2[i].length();
                    lastMod = temp2[i].lastModified();
                    SimpleDateFormat date = new SimpleDateFormat("MM/dd/yyyy HH:mm");
                    GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("US/Mountain"));
                    calendar.setTimeInMillis(lastMod);
                    Formatter fmt = new Formatter();
                    fmt.format("%10s", fileSize);
                    System.out.println(dir + read + write + execute + " " + fmt + " bytes " + date.format(calendar.getTime()) + " " + temp[i]);
                }

            }
        } catch (Exception ex) {
            System.out.println("Empty File Directory");
        }
    }

    //TODO this doesnt work right, figure out how pipelining workd outside of my command shell so i can replacate here
    private static void pipe(String command) {
        String[] commandList = splitCommand(command);
        for (int i = 0; i < commandList.length; i++) {
            System.out.println(commandList[i]);
        }
        String[] p1Cmd = {commandList[0]};
        String[] p2Cmd = {commandList[1]};

        ProcessBuilder pb1 = new ProcessBuilder(p1Cmd);
        ProcessBuilder pb2 = new ProcessBuilder(p2Cmd);

        pb1.redirectInput(ProcessBuilder.Redirect.INHERIT);
        //pb1.redirectOutput(ProcessBuilder.Redirect.PIPE);

        pb2.redirectOutput(ProcessBuilder.Redirect.INHERIT);

        try {
            Process p1 = pb1.start();
            Process p2 = pb2.start();

            java.io.InputStream in = p1.getInputStream();
            java.io.OutputStream out = p2.getOutputStream();

            int c;
            while ((c = in.read()) != -1) {
                out.write(c);
            }

            out.flush();
            out.close();

            p1.waitFor();
            p2.waitFor();
        } catch (Exception ex) {
        }
    }

    private static String cd(String command) {
        String currentDir = System.getProperty("user.dir");

        if (command.equals("cd")) {
            currentDir = System.getProperty("user.home");
            java.nio.file.Path proposed = java.nio.file.Paths.get(currentDir);
            currentDir = System.setProperty("user.dir", proposed.toString());


        } else if (command.equals("cd ..")) {
            File fileDir = new File(currentDir);
            currentDir = fileDir.getParent();
            java.nio.file.Path proposed = java.nio.file.Paths.get(currentDir);
            currentDir = System.setProperty("user.dir", proposed.toString());

        } else {
            if (command.contains("\"")) {
                makeDir(command);
                String[] commandList = splitCommand(command);
                java.nio.file.Path proposed = java.nio.file.Paths.get(currentDir, commandList[1]);
                currentDir = System.setProperty("user.dir", proposed.toString());

            } else {
                File fileDir = new File(currentDir);
                String[] inputList = command.split(" ");
                String[] temp = fileDir.list();
                File[] directoryList = fileDir.listFiles();
                boolean doesExist = false;
                for (int i = 0; i < temp.length; i++) {
                    if (directoryList[i].getName().equals(inputList[1])) {
                        java.nio.file.Path proposed = java.nio.file.Paths.get(currentDir, inputList[1]);
                        currentDir = System.setProperty("user.dir", proposed.toString());
                        doesExist = true;
                    }
                }
                if (doesExist == false) {
                    System.out.println("Directory does not exist");
                }

            }
        }
        return currentDir;
    }

    private static String[] splitCommand(String input) {
        java.util.List<String> matchList = new java.util.ArrayList<>();

        Pattern regex = Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'");
        Matcher regexMatcher = regex.matcher(input);
        while (regexMatcher.find()) {
            if (regexMatcher.group(1) != null) {
                // Add double-quoted string without the quotes
                matchList.add(regexMatcher.group(1));
            } else if (regexMatcher.group(2) != null) {
                // Add single-quoted string without the quotes
                matchList.add(regexMatcher.group(2));
            } else {
                // Add unquoted word
                matchList.add(regexMatcher.group());
            }
        }

        return matchList.toArray(new String[matchList.size()]);
    }

    private static void makeDir(String command) {
        String[] commandList = splitCommand(command);
        File newFile = new File(commandList[1]);
        boolean test = newFile.mkdirs();
        if (test) {
            System.out.println("New directory named \"" + commandList[1] + "\" created");
        } else {
            System.out.println("directory named \"" + commandList[1] + "\" already exists or things were mispelled");
        }
    }

    private static void removeDir(String command) {
        String[] commandList = splitCommand(command);
        File file = new File(commandList[1]);
        boolean test = file.delete();
        if (test) {
            System.out.println("file or directory named \"" + commandList[1] + "\" has been deleted");
        } else {
            System.out.println("There was an error during deletion: make sure file is closed or directory is empty");
        }

    }

    private static void ptime(String input) {
        try {

            String[] command = {input};
            ProcessBuilder pb = new ProcessBuilder(command);
            long start = System.currentTimeMillis();
            Process p = pb.start();

            System.out.println("Starting to wait");
            p.waitFor();
            long end = System.currentTimeMillis();
            System.out.printf("Program took %d milliseconds\n", end - start);
        } catch (IOException ex) {
            System.out.println("Illegal command im still not working");
        } catch (Exception ex) {
            System.out.println("Something else bad happened");
        }
    }

    private static String getInput() {
        Scanner myObject = new Scanner(System.in);
        String currentDirectory = System.getProperty("user.dir");
        System.out.print(currentDirectory + " $ ");
        String input2 = myObject.nextLine();
        return input2;

    }

    private static void stop() {
        System.exit(5);
    }
}