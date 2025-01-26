import java.net.*;
import java.io.*;
import java.util.*;

public class Client implements Runnable {

    @Override
    public void run(){
        try {

            Socket socket = new Socket(InetAddress.getLoopbackAddress(), 8000);
            System.out.println("we on");
            ObjectOutputStream dout = new ObjectOutputStream(socket.getOutputStream());
            dout.flush();
            ObjectInputStream din = new ObjectInputStream(socket.getInputStream());
            Scanner scanner = new Scanner(System.in);

            boolean finish = false;
            while (!finish) {
                System.out.println(din.readUTF()); //to show the first menu to the client to know if doctor or patient
                System.out.println();
                String input = scanner.nextLine();
                dout.writeUTF(input);
                dout.flush();

                if (input.matches("Doctor")) {
                    System.out.println(din.readUTF()); //to ask if he wishes to register or log in
                    String choice = scanner.nextLine();
                    dout.writeUTF(choice);
                    dout.flush();

                    if (choice.matches("1")) { //to show the ReadCredentials messages to the user
                        System.out.println(din.readUTF()); //enter your name
                        dout.writeUTF(scanner.nextLine());
                        dout.flush();

                        System.out.println(din.readUTF()); //enter your username
                        dout.writeUTF(scanner.nextLine());
                        dout.flush();

                        System.out.println (din.readUTF()); //enter your password
                        dout.writeUTF(scanner.nextLine());
                        dout.flush();

                        System.out.println("Waiting for Approval");
                        String answer = din.readUTF(); //to inform the doctor if he was rejected from registration
                        if (answer.matches("Rejected")) { //"Your Registration was "+answer
                            System.out.println("Your registration was " + answer);
                            finish = true; //currently unnecessary
                            socket.close();
                            break; //break the big while loop
                        } else if (!answer.matches("Rejected")) {
                            System.out.println("Your registration was " + answer);

                            System.out.println(din.readUTF()); //enter #of years of experience
                            dout.writeInt(scanner.nextInt());
                            dout.flush();

                            System.out.println(din.readUTF()); // "Enter your specialty"
                            scanner.nextLine();
                            String specialty = scanner.nextLine(); // Read the input
                            dout.writeUTF(specialty);
                            dout.flush();
                            while (!Server.Specialties.contains(specialty)) {
                                System.out.println("Enter only one of the available Specialties!\nSpecialty: ");
                                specialty = scanner.nextLine();
                                dout.writeUTF(specialty);
                                dout.flush();
                            }
                            continue; //to go back to the main menu after the registration was accepted
                            //and he entered the rest of his info

                        }
                    } else if (choice.matches("2")) {

                        System.out.println(din.readUTF()); //enter your username to log-in
                        dout.writeUTF(scanner.nextLine());
                        dout.flush();

                        System.out.println(din.readUTF()); //enter your password to log-in
                        dout.writeUTF(scanner.nextLine());
                        dout.flush();

                        String checkAnswer = din.readUTF(); //to find out if the doctor logged-in or failed to log-in
                        if (checkAnswer.matches("Wrong Password") || checkAnswer.matches("no user with the entered name")) {
                            System.out.println(din.readUTF()); //"The log-in failed due to "+checkAnswer
                            continue;
                        } else { //for when the doctor logs in correctly
                            boolean yes = false;
                            String input2 = "";
                            while (!yes) { //Doctor's main menu
                                System.out.println("Choose:\n1.Browse my tickets\n2.Browse general tickets\n3.Next ticket\n4.Log-off");
                                input2 = scanner.nextLine();
                                dout.writeUTF(input2);
                                dout.flush();
                                switch (input2) {
                                    case "1":
                                        while (true) {
                                            System.out.println(din.readUTF()); //display tickets for this doctor
                                            System.out.print("Choose ticket to respond to or type exit to return to the Doctors's main menu: ");
                                            String num = scanner.nextLine();
                                            dout.writeUTF(num);
                                            dout.flush();
                                            if (!num.matches("exit")) {
                                                System.out.print("set your response: ");
                                                String response = scanner.nextLine();
                                                dout.writeUTF(response);
                                                dout.flush();
                                                break;
                                            } else {
                                                break;
                                            }
                                        }
                                        break;
                                    case "2":
                                        String genticket = Doctor.displayGeneralTickets();
                                        while (true) {
                                            System.out.println(din.readUTF()); //display general tickets
                                            System.out.print("Choose ticket to respond to or type exit to return to the Doctors's main menu: ");
                                            String number = scanner.nextLine();
                                            dout.writeUTF(number);
                                            dout.flush();
                                            if (!number.matches("exit")) {
                                                System.out.print("set your response: ");
                                                String response = scanner.nextLine();
                                                dout.writeUTF(response);
                                                dout.flush();
                                                break;
                                            } else {
                                                break;
                                            }
                                        }

                                        break;
                                    case "3":

                                        int x = din.readInt();
                                        if (x != 0) {
                                            System.out.println(din.readUTF()); //read the ticket from the NextTicket() in Doctor
                                            System.out.print("enter your response: ");
                                            String response = scanner.nextLine();
                                            dout.writeUTF(response);
                                            dout.flush();
                                        } else
                                            System.out.println("You have no more tickets left");
                                        break;
                                    case "4":
                                        System.out.println("you are now logged-off");
                                        yes = true;
                                        break;
                                }
                            }
                        }
                    }

                } else if (input.matches("Patient")) {
                    while (true) {
                        System.out.println(din.readUTF()); //"Choose:\n1.Browse online doctors\n2.Submit a general ticket\n3.Exit" this is from the server
                        String answer = scanner.nextLine();
                        dout.writeUTF(answer);
                        dout.flush();
                        if (answer.matches("1")) {

                            System.out.println("Choose:\n1.show all online doctors\n2.show doctors of specific specialty");
                            String s = scanner.nextLine();
                            dout.writeUTF(s);
                            dout.flush();
                            switch (s) {
                                case "1":
                                    System.out.println("Displaying all online doctors: ");
                                    String onlineDoctors = din.readUTF();
                                    if(onlineDoctors.matches("no online doctors at the moment")){
                                        System.out.println("no online doctors at the moment");
                                        break;
                                    }

                                    System.out.println(onlineDoctors); //display all online doctors
                                    System.out.println("Choose number of doctor you want: ");
                                    dout.writeUTF(scanner.nextLine());
                                    dout.flush();

                                    readInfo(din, dout);
                                    //remaining logic to wait for answer from the specified doctor then display it to the patient is done in
                                    //the readInfo() method

                                    break;
                                case "2":
                                    System.out.println("Enter your wanted specialty: ");
                                    dout.writeUTF(scanner.nextLine()); //read his wanted specialty
                                    dout.flush();

                                    System.out.println("Displaying doctors of this specialty: ");
                                    String onlineDoctos = din.readUTF();
                                    if(onlineDoctos.matches("no online doctors at the moment")){
                                        System.out.println("no online doctors of this specialty at the moment");
                                        break;
                                    }

                                    System.out.println(onlineDoctos); //read the specialties with their corresponding online doctor
                                    System.out.println("enter number of doctor of the displayed you want: ");
                                    String num = scanner.nextLine();
                                    dout.writeUTF(num);
                                    dout.flush();

                                    readInfo(din, dout);
                                    //remaining logic to wait for answer from the doctor then display it to the patient is done in readInfo()
                                    break;

                            }

                        } else if (answer.matches("2")) { //submit a general ticket
                            readInfo(din, dout);
                            //remaining logic to wait for answer from any doctor then display it to the patient is done in readInfo()

                        } else if (answer.matches("3")) { //exit
                            finish = true;
                            break;
                        }
                    }

                } else {
                    System.out.println(din.readUTF());
                }
            }

        } catch (Exception e) {
            System.out.println("error in main method of Client class " + e);
        }
    }

    public static void main(String[] args) {
        Thread clientThread = new Thread(new Client());
        clientThread.start();

    }

    public static void readInfo(ObjectInputStream din, ObjectOutputStream dout) {
        Scanner scanner = new Scanner(System.in);
        try {
            System.out.println("your name: ");
            dout.writeUTF(scanner.nextLine()); //read the patient's name
            dout.flush();

            System.out.println("your age: ");
            dout.writeUTF(scanner.nextLine()); //read the patient's age
            dout.flush();

            System.out.println("Enter your Question: ");
            dout.writeUTF(scanner.nextLine()); //read the patient's question
            dout.flush();
            boolean waiting = true;
            int count = 0;
            while(waiting && count<2) {
                System.out.println("Waiting for an Answer");
                //logic to Wait for notification for 5 minutes not complete

                String response = din.readUTF();
                if (response != null) { // Check if a response is available
                    System.out.println("Response from doctor: " + response);
                    waiting = false; // Stop waiting
                } else {
                    boolean doctorLoggedIn = din.readBoolean(); // Check if doctor is still logged in
                    if (doctorLoggedIn) {
                        System.out.println("No response but Doctor is logged in, do you wish to wait? (Y/N)");
                        String answer = scanner.nextLine();
                        dout.writeUTF(answer);
                        dout.flush();
                        if (answer.matches("N")) {
                            System.out.println("Sorry you didn't get an answer, Bye");
                            waiting = false; // Stop waiting
                        }
                    } else {
                        System.out.println("Doctor logged off, Bye");
                        waiting = false; // Stop waiting
                    }
                }

            }
        } catch (Exception e) {
            System.out.println("error in readInfo() in Client" + e);
        }
    }

}