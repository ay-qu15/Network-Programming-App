import java.io.*;
import java.util.*;

public class Doctor {

    public String username;
    public String name;
    public String password;
    public int experience;
    public String specialty;
    public boolean logged_in;
    public final ArrayList<Ticket> doctor_tickets = new ArrayList<>(); //Queue so i can return tickets in FIFO order
    public static final ArrayList<Ticket> generalTickets = new ArrayList<>();

    public static void ReadCredentials(Doctor doctor, ObjectInputStream din, ObjectOutputStream dout) {
        //either returns Accepted or Rejected when the doctor is registering in a string
        //note all the doctor's data is to be saved in the server 
        try {

            dout.writeUTF("name: ");
            dout.flush();
            doctor.name = din.readUTF(); //write his name

            dout.writeUTF("username: ");
            dout.flush();
            doctor.username = din.readUTF(); //write his username

            dout.writeUTF("password: ");
            dout.flush();
            doctor.password = din.readUTF(); //write his password


        } catch (Exception e) {
            System.err.println("Error in ReadCredentials() in Doctor class " + e);
        }

    }

    public static void ResumeCredentials(Doctor doctor, ObjectInputStream din, ObjectOutputStream dout) {
        //returns nothing and continues to read the doctor's credentials ...
        //note all the doctor's data is saved in the server 
        try {

            dout.writeUTF("Experience: ");
            dout.flush();
            doctor.experience = din.readInt();

            dout.writeUTF("Specialty: ");
            dout.flush();
            String specialty = din.readUTF();
            while (!Server.Specialties.contains(specialty)) {
                specialty = din.readUTF();
            }
            doctor.specialty = specialty;

        } catch (Exception e) {
            System.err.println("Error in ResumeCredentials() in Doctor class " + e);
        }

    }

    public static String CheckCredentials(ArrayList<Doctor> DoctorsList, Doctor doctor, ObjectInputStream din, ObjectOutputStream dout) {
        //either returns index of this doctor in the Doctors array or reason of refusal in the string

        try {
            dout.writeUTF("enter your username to log-in: ");
            dout.flush();
            doctor.username = din.readUTF(); //write his username

            dout.writeUTF("enter your password to log-in: ");
            dout.flush();
            doctor.password = din.readUTF(); //write his password

            boolean yes = false;
            String reason = "";
            int index = 0;
            for (int i = 0; i < DoctorsList.size(); i++) {
                if (Doctor.compareUser(DoctorsList.get(i), doctor)) {
                    if (Doctor.comparePassword(DoctorsList.get(i), doctor)) {
                        yes = true;
                        index = i;
                        break;
                    } else {
                        reason = "Wrong Password";
                        break;
                    }
                } else {
                    reason = "no user with the entered name";
                }
            }

            if (yes) {
                return Integer.toString(index);
            } else {
                return reason;
            }
        } catch (Exception e) {
            System.err.println("Error in CheckCredentials() in Doctor class " + e);
        }

        return null;
    }

    public static void Logged_in(ArrayList<Doctor> DoctorsList, int index, ObjectInputStream din, ObjectOutputStream dout) { //Doctor's main menu
        Doctor doctor = DoctorsList.get(index);
        doctor.logged_in = true;
        try {
            while (true) {

                String input = din.readUTF(); //his choice for 1.Browse my tickets2.Browse general tickets3.Next ticket4.Log-off"
                if (input.matches("1")) {
                    MyTickets(doctor, din, dout);
                    //  break;
                } else if (input.matches("2")) {
                    GeneralTickets(doctor, din, dout);
                    //   break;
                } else if (input.matches("3")) {
                    NextTicket(doctor, din, dout);
                } else if (input.matches("4")) {
                    Log_off(doctor);
                    break;
                }
            }
        } catch (Exception x) {
            System.out.println("error in Logged_in() in Doctor class");
        }

    }


    public static void MyTickets(Doctor doctor, ObjectInputStream din, ObjectOutputStream dout) { // My tickets menu
        try {
            //    dout.writeUTF(displayTicketsForDoctor(doctor));//to give it to the string tickets in client
            //!displayTicketsForDoctor(doctor).matches("No tickets for you at the moment") ' condition replaced with true'
            while (true) {
                dout.writeUTF(displayTicketsForDoctor(doctor));
                dout.flush();
                String num = din.readUTF();
                if (!num.matches("exit")) {
                    int num2 = Integer.parseInt(num) - 1;
                    String response = din.readUTF();
                    synchronized (doctor.doctor_tickets.get(num2)) {
                        setResponseForTicket(doctor.doctor_tickets.get(num2), response, doctor);
                        doctor.doctor_tickets.get(num2).notifyAll();
                    //    doctor.doctor_tickets.get(num2).notifyAll();
                    }
                    break;
                } else {
                    break;
                }
            }

        } catch (Exception e) {
            System.out.println("error in MyTickets() in Doctor class");
        }

    }

    public static void GeneralTickets(Doctor doctor, ObjectInputStream din, ObjectOutputStream dout) { //All tickets menu
        try {
            while (true) {
                dout.writeUTF(displayGeneralTickets());
                dout.flush();
                String num = din.readUTF();
                if (!num.matches("exit")) {
                    int num2 = Integer.parseInt(num) - 1;
                    String response = din.readUTF();
                    synchronized (doctor.doctor_tickets.get(num2)) {
                        setResponseForTicket(generalTickets.get(num2), response, doctor);
                        //here i must add logic to write this response to the patient client side
                        generalTickets.get(num2).notifyAll();

                    }
                    break;
                } else {
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println("error in GeneralTickets() in Doctor class" + e);
        }
    }

    public static void NextTicket(Doctor doctor, ObjectInputStream din, ObjectOutputStream dout) { //next ticket menu
        try {
            int x = doctor.doctor_tickets.size();
            dout.writeInt(x);
            dout.flush();
            for (int i = 0; i < doctor.doctor_tickets.size(); i++) {
                if (doctor.doctor_tickets.get(i).getResponder() == null) {
                    dout.writeUTF(doctor.doctor_tickets.get(i).toString());
                    dout.flush();
                    String response = din.readUTF();
                    synchronized (doctor.doctor_tickets.get(i)) {
                        setResponseForTicket(doctor.doctor_tickets.get(i), response, doctor);
                        //here i must add logic to write this response to the patient client side
                        doctor.doctor_tickets.get(i).notifyAll();
                    }
                    break;
                }
            }
        } catch (Exception x) {
            System.out.println("error in NextTicket() in Doctor" + x);
        }
    }

    public static void Log_off(Doctor doctor) { //log off from the Doctor's main menu
        doctor.logged_in = false;
    }

    public static synchronized ArrayList<Doctor> getOnlineDoctors() { //return online doctors only
        if (Server.Doctors.size() == 0) {
            return null;
        }

        ArrayList<Doctor> onlineDoctors = new ArrayList<>();
        for (int i = 0; i < Server.Doctors.size(); i++) {
            if (Server.Doctors.get(i).logged_in) {
                onlineDoctors.add(Server.Doctors.get(i));
            }
        }
        return onlineDoctors;
    }

    public static synchronized String displayTicketsForDoctor(Doctor doctor) {
        if (doctor.doctor_tickets.size() == 0) {
            return "No tickets for you at the moment";
        }

        String tickets = "";
        int i = 1;
        for (Ticket ticket : doctor.doctor_tickets) {
            tickets = tickets + "ticket number " + (i++) + ": " + ticket.toString();
        }
        return tickets;
    }

    public static synchronized String displayGeneralTickets() {
        if (generalTickets.size() == 0) {
            return "No general tickets at the moment";
        }

        String tickets = "";
        int i = 1;
        for (Ticket ticket : generalTickets) {
            tickets = tickets + "ticket number " + (i++) + ": " + ticket;
        }
        return tickets;
    }

    public static boolean compareUser(Doctor doctor1, Doctor doctor2) {
        return doctor1.username.matches(doctor2.username);
    }

    public static boolean comparePassword(Doctor doctor1, Doctor doctor2) {
        return doctor1.password.matches(doctor2.password);
    }

    public static void setResponseForTicket(Ticket ticket, String response, Doctor doctor) {
        synchronized (ticket) {
            ticket.setResponse(response, doctor);

        }
    }

}