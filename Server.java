import java.net.*;
import java.io.*;
import java.util.*;

public class Server {

    public static List<String> Specialties = new ArrayList<>(List.of(
            "Dermatology", "Cardiology", "Obstetrics and gynecology", "Neurology",
            "Family medicine", "Gastroenterology", "Ophthalmology", "Pediatrics",
            "Orthopedic", "Nephrology", "Hematology", "General surgery",
            "Endocrinology", "Medical laboratory scientist"));
    public static ArrayList<Doctor> Doctors = new ArrayList<>();

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(8000, 50);
            System.out.println("Server is running and waiting for connections...");

            while (true) {
                Socket clientSocket = serverSocket.accept(); // Accept a client connection
                System.out.println("Client connected: " + clientSocket.getInetAddress());

                // Create a new thread for this client
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                Thread clientThread = new Thread(clientHandler);
                clientThread.start();
            }
        } catch (IOException e) {
            System.out.println("Error in Server: " + e);
        }
    }
}

class ClientHandler implements Runnable {
    private Socket clientSocket;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            ObjectOutputStream dout = new ObjectOutputStream(clientSocket.getOutputStream());
            dout.flush(); // Flush the stream after creation
            ObjectInputStream din = new ObjectInputStream(clientSocket.getInputStream());
            Scanner scanner = new Scanner(System.in);

            boolean finish = false;

            while (!finish) {
                dout.writeUTF("Are you a Doctor or a Patient?");
                dout.flush(); // Ensure data is sent
                String input = din.readUTF();

                if (input.matches("Doctor")) {
                    dout.writeUTF("Choose from the following:\n1.Register\n2.Log-in");
                    dout.flush(); // Ensure data is sent
                    String choice = din.readUTF();
                    Doctor doctor = new Doctor();

                    switch (choice) {
                        case "1":
                            Doctor.ReadCredentials(doctor, din, dout);
                            System.out.print("Accept (Y/N)? ");
                            String isApproved = scanner.nextLine();
                            if (isApproved.matches("N")) {
                                dout.writeUTF("Rejected");
                                dout.flush(); // Ensure data is sent
                                clientSocket.close();
                                finish = true;
                            } else if (isApproved.matches("Y")) {
                                dout.writeUTF("Accepted");
                                dout.flush(); // Ensure data is sent
                                Doctor.ResumeCredentials(doctor, din, dout);
                                synchronized (Server.Doctors) {
                                    Server.Doctors.add(doctor);
                                }
                                continue;
                            }
                            break;

                        case "2":
                            String checkAnswer = Doctor.CheckCredentials(Server.Doctors, new Doctor(), din, dout);
                            dout.writeUTF(checkAnswer);
                            dout.flush(); // Ensure data is sent
                            if (checkAnswer.matches("Wrong Password") || checkAnswer.matches("no user with the entered name")) {
                                dout.writeUTF("The log-in failed due to " + checkAnswer);
                                dout.flush(); // Ensure data is sent
                            } else {
                                int index = Integer.parseInt(checkAnswer);
                                Doctor.Logged_in(Server.Doctors, index, din, dout);
                            }
                            break;

                        default:
                            break;
                    }
                } else if (input.matches("Patient")) {
                    while (true) {
                        dout.writeUTF("Choose:\n1.Browse online doctors\n2.Submit a general ticket\n3.Exit");
                        dout.flush(); // Ensure data is sent
                        String answer = din.readUTF();
                        if (answer.matches("1")) {
                            Patient.browseOnlineDoctors(din, dout);
                        } else if (answer.matches("2")) {
                            Patient.submitGeneralTicket(din, dout);
                        } else if (answer.matches("3")) {
                            return;
                        }
                    }
                } else {
                    dout.writeUTF("Only Type Doctor or Patient");
                    dout.flush(); // Ensure data is sent
                }
            }
        } catch (IOException e) {
            System.out.println("Error in ClientHandler: " + e);
        } finally {
            try {
                clientSocket.close();
                System.out.println("Client disconnected.");
            } catch (IOException e) {
                System.out.println("Error closing client socket: " + e);
            }
        }
    }
}
