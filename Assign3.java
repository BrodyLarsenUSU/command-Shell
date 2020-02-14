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

public class Assign3 {
    public static void main(String[] args) {
        boolean run = true;
        ArrayList<String> inputList = new ArrayList<>();
        while (run) {
            String command = getInput();
            inputList.add(command);
            switch (command) {
                case "exit":
                    run = false;
                    stop();
                case "ptime":
                    ptime();
                    continue;
                case "list":
                    list();
                    continue;
                case "history":
                    getHistory(inputList, inputList.size());


            }
            if (command.contains("cd")){
                cd(command);
            }
            else if (command.contains("history ^ ")){
                String[] input = command.split(" ");
                int number = Integer.parseInt(input[2]);
                getNumberedHistory(inputList, number);
            }
        }
    }

    private static void getHistory(ArrayList inputList, int lengthList) {
        for (int i = 0; i < lengthList; i++) {
            System.out.println(inputList.get(i));
        }
    }

    private static void getNumberedHistory(ArrayList inputList, int num){
        ArrayList<Object> tempList = new ArrayList<>();
        //adding past num things to list
        int counter = 0;
        for(int i = 0; i < num; i++) {
            try {
                tempList.add(inputList.get(inputList.size() - (num - i)));
                counter++;
                if(counter == num){
                    continue;
                }
            } catch(Exception ex){
                continue;
                }
            }
        //printing out past num objects
        for(int i = 0; i < tempList.size(); i++){
            System.out.println(tempList.get(i));
        }
        }

//TODO format the print string better
    private static void list() {
        String currentDir = System.getProperty("user.dir");
        File fileDir = new File(currentDir);
        String[] temp = fileDir.list();
        File[] temp2 = fileDir.listFiles();

        for (int i = 0; i < fileDir.list().length; i++){
            String dir = "-";
            String read = "-";
            String write = "-";
            String execute = "-";
            long fileSize = 0;
            long lastMod = 0;
            if (temp2[i].isDirectory()){
                dir = "d";
            }
            if(temp2[i].canRead()){
                read = "r";
            }
            if(temp2[i].canWrite()){
                write = "w";
            }
            if(temp2[i].canExecute()){
                execute = "x";
            }
            fileSize = temp2[i].length();
            lastMod = temp2[i].lastModified();
            SimpleDateFormat date = new SimpleDateFormat("MM/dd/yyyy HH:mm");
            GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("US/Mountain"));
            calendar.setTimeInMillis(lastMod);
            System.out.print((dir + read + write + execute + " " + fileSize +" bytes " + date.format(calendar.getTime()) + " " + temp[i]));
        }
    }




//TODO finish cd
    private static String cd(String command) {
        String currentDir = System.getProperty("user.dir");
        File fileDir = new File(currentDir);
        String[] test = splitCommand(command);

        if (command.equals("cd ../")) {
            currentDir = fileDir.getParent();
            java.nio.file.Path proposed = java.nio.file.Paths.get(currentDir);
            currentDir = System.setProperty("user.dir", proposed.toString());

        } else {
            String temp = command;
            String[] templist = temp.split(" ");
            java.nio.file.Path proposed = java.nio.file.Paths.get(currentDir, templist[1]); //TODO check if templist[1] is in directory, if not throw error.
            currentDir = System.setProperty("user.dir", proposed.toString());
            temp = " ";
            templist = null;
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

    private static void ptime() {
        try {
            String[] command = {"nano", "ProcessExample.java"};
            ProcessBuilder pb = new ProcessBuilder(command);
            long start = System.currentTimeMillis();
            Process p = pb.start();

            System.out.println("Starting to wait");
            p.waitFor();
            long end = System.currentTimeMillis();
            System.out.printf("Program took %d milliseconds\n", end - start);
        } catch (IOException ex) {
            System.out.println("Illegal command");
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