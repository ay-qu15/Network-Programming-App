public class Ticket{

    private static int idCounter = 1;
    private final int id;
    private final String patientName;
    private final int patientAge;
    private final String question;//to hold the content of a ticket submitted
    private String response = null;
    private Doctor responder; //to hold the name or username of a specific doctor to this ticket


    public synchronized  String getResponse() {
        return response;
    }

    public synchronized  Doctor getResponder() {
        return responder;
    }

    public Ticket(String patientName, int patientAge, String question) {
        this.id = idCounter++;
        this.patientName = patientName;
        this.patientAge = patientAge;
        this.question = question;
    }

    public int getId() {
        return id;
    }

    public synchronized void setResponse(String response, Doctor responder) {
        this.response = response;
        this.responder = responder;
        notify();
    }

    @Override
    public String toString() {
        return "Ticket ID: " + id + ", Patient: " + patientName + ", Age: " + patientAge + ", Question: " + question ;
    }
}

