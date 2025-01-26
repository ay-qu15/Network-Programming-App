import java.net.*;
import java.io.*;
import java.util.*;

public class Patient {

    public static void browseOnlineDoctors(ObjectInputStream oin, ObjectOutputStream oout) {
        try {
            String answer = oin.readUTF();
            if (answer.equals("1")) {
                String s = getOnlineDoctors();
                oout.writeUTF(s);
                oout.flush();
                if(s.matches("no online doctors at the moment")){
                    return;
                }
                String num = oin.readUTF();
                int doctorIndex = Integer.parseInt(num) - 1;

                //from here its the readInfo() method in Client thats writing this data
                String name = oin.readUTF();
                int age = oin.readInt();
                String question = oin.readUTF();
                final Ticket ticket = new Ticket(name, age, question);
                // Add ticket to the doctor's queue
                synchronized (Server.Doctors) {
                    Server.Doctors.get(doctorIndex).doctor_tickets.add(ticket);
                }

                // Wait for response
                waitForResponse(ticket, oin, oout, Server.Doctors.get(doctorIndex));
            } else if (answer.equals("2")) {
            //    oout.writeUTF("Enter your wanted specialty:");
            //    oout.flush();
                String specialty = oin.readUTF(); //read his wanted specialty
                String s = getOnlineDoctors_specific_specialty(specialty);
                oout.writeUTF(s);
                oout.flush();
                if(s.matches("no online doctors at the moment")) {
                    return;
                }
                String num = oin.readUTF();
                int doctorIndex = Integer.parseInt(num) - 1;

                ////from here its the readInfo() method in Client thats writing this data
                String name = oin.readUTF();
                int age = oin.readInt();
                String question = oin.readUTF();
                final Ticket ticket = new Ticket(name, age, question);

                // Add ticket to the doctor's queue
                synchronized (Server.Doctors) {
                    Server.Doctors.get(doctorIndex).doctor_tickets.add(ticket);
                }

                // Wait for response
                waitForResponse(ticket, oin, oout, Server.Doctors.get(doctorIndex));
            }
        } catch (Exception e) {
            System.err.println("Error in browseOnlineDoctors() in Patient: " + e);
        }
    }

    public static void submitGeneralTicket(ObjectInputStream oin, ObjectOutputStream oout) {
        try {
            //from here its the readInfo() method in Client thats writing this data
            String name = oin.readUTF();
            int age = oin.readInt();
            String question = oin.readUTF();
            Ticket ticket = new Ticket(name, age, question);

            // Add ticket to general tickets
            synchronized (Doctor.generalTickets) {
                Doctor.generalTickets.add(ticket);
            }

            // Wait for response
            waitForResponse(ticket, oin, oout, null);
        } catch (Exception e) {
            System.err.println("Error in submitGeneralTicket: " + e);
        }
    }

    public static String getOnlineDoctors() {
        if (Doctor.getOnlineDoctors() == null)
            return "no online doctors at the moment";

        String s = "";
        ArrayList<Doctor> onlineDoctors = Doctor.getOnlineDoctors();
        for (int i = 0; i < onlineDoctors.size(); i++) {
            s = s + "\n" + (i + 1) + ". " + onlineDoctors.get(i).name + "-" + onlineDoctors.get(i).specialty;
        }
        return s;
    }

    public static String getOnlineDoctors_specific_specialty(String specialty) {
        if (Doctor.getOnlineDoctors() == null)
            return "no online doctors at the moment";

        String s = "";
        ArrayList<Doctor> onlineDoctors = Doctor.getOnlineDoctors();
        for (int i = 0; i < onlineDoctors.size(); i++) {
            if (onlineDoctors.get(i).specialty.matches(specialty))
                s = s + "\n" + (i + 1) + ". " + onlineDoctors.get(i).name + "-" + onlineDoctors.get(i).specialty;
        }
        return s;
    }

    private static void waitForResponse(Ticket ticket, ObjectInputStream oin, ObjectOutputStream oout, Doctor doctor){
        long timeout = 5 * 60 * 1000; // 5 minutes
        boolean waiting = true;
        try {
            //oout.writeObject(ticket);
            int count = 0;
            while (waiting) {
                synchronized (ticket) {
                    ticket.wait(timeout);
                    oout.writeUTF(ticket.getResponse());
                    oout.flush();
                    if (ticket.getResponse() != null) { // Ensure this condition is checked
                        oout.writeUTF("Response from doctor: " + ticket.getResponse());
                        oout.flush();
                        if (doctor != null)
                            doctor.doctor_tickets.remove(ticket);
                        else if (Doctor.generalTickets.contains(ticket))
                            Doctor.generalTickets.remove(ticket);
                        waiting = false; // Exit the loop
                    } else {

                        oout.writeBoolean(doctor.logged_in);
                        if (doctor.logged_in) {
                            String answer = oin.readUTF(); // "Do you wish to wait? (Y/N)"
                            if (answer.matches("N")) {
                                waiting = false;
                            }
                        } else {
                            waiting = false; // Doctor logged off, stop waiting
                        }
                    }
                }
            }
        }
        catch (Exception x){
            System.out.println("Error in waitForResponse() in Patient " + x);
        }
    }
}