Medical Consultation Platform (Java TCP/IP)
This project implements a multi-threaded client-server medical platform designed for reliable, real-time communication between doctors and patients using Java TCP/IP sockets. The system handles concurrent user sessions and facilitates a ticket-based consultation workflow.

Core Functionality

Dual-Role Architecture: The server manages two distinct client types: Doctors, who must register and log in with secure credentials, and Patients, who can access the platform anonymously to seek consultations.

Ticket Management System:


Direct Tickets: Patients can browse a live list of online doctors (optionally filtered by 14 different specialties) and submit tickets directly to a specific professional.


General Tickets: Patients may submit general inquiries that are broadcast to all registered doctors for a response.


Queue Logic: The server maintains a first-in, first-out (FIFO) pattern for ticket processing, with a strict limit of 5 pending tickets per doctor to ensure quality of service.


Real-Time Doctor Status: Doctors appear "online" to patients upon a successful login and are automatically removed from the active pool upon logging off.

Technical Implementation

Concurrency & Synchronization: The server leverages Java Multithreading to handle multiple simultaneous client connections. It incorporates synchronization mechanisms to resolve data contention and race conditions arising from concurrent access to shared ticket lists and doctor status registries.


Robust Communication: Utilizes the TCP protocol to ensure reliable, ordered delivery of medical consultations and responses.


State Persistence & Validation: The server-side logic includes a validation layer for doctor registrations and login attempts, matching credentials against registered data to maintain system integrity.

Known Technical Limitations

Wait-Time Logic: The current client-side implementation faces a challenge regarding the 5-minute timeout mechanism; specifically, it does not yet consistently prompt the patient to wait or withdraw if a specific doctor fails to respond within the designated window.
