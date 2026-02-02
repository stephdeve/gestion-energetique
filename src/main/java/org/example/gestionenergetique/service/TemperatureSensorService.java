package org.example.gestionenergetique.service;

import com.fazecast.jSerialComm.SerialPort;

import java.util.Scanner;
import java.util.function.Consumer;

public class TemperatureSensorService {
    private SerialPort port;
    private volatile boolean listening = false;
    private Thread listenThread;
    private double lastTemperature = 0;

    public TemperatureSensorService(){
        // Connexion différée: on laissera le contrôleur choisir et se connecter à un port.
    }

    public String[] listAvailablePorts(){
        SerialPort[] ports = SerialPort.getCommPorts();
        String[] names = new String[ports.length];
        for (int i = 0; i < ports.length; i++){
            // Afficher nom système (COM3, etc.) + description
            names[i] = ports[i].getSystemPortName() + " - " + ports[i].getDescriptivePortName();
        }
        return names;
    }

    public boolean connectBySystemName(String systemPortName, int baudRate){
        disconnect();
        SerialPort candidate = null;
        for (SerialPort p : SerialPort.getCommPorts()){
            if (p.getSystemPortName().equalsIgnoreCase(systemPortName)){
                candidate = p;
                break;
            }
        }
        if (candidate == null) return false;
        candidate.setBaudRate(baudRate);
        if (candidate.openPort()){
            this.port = candidate;
            return true;
        }
        return false;
    }

    public void disconnect(){
        stopListening();
        if (port != null && port.isOpen()){
            try { port.closePort(); } catch (Exception ignored) {}
        }
        port = null;
    }

    public boolean isConnected(){
        return port != null && port.isOpen();
    }

    public void startListening(Consumer<Double> onTemperatureRead) {
        if (port == null || !port.isOpen() || listening) return;
        listening = true;
        listenThread = new Thread(() -> {
            try (Scanner scanner = new Scanner(port.getInputStream())){
                while (listening && scanner.hasNextLine()){
                    String line = scanner.nextLine().trim();
                    if (line.startsWith("TEMP:")){
                        try {
                            double temp = Double.parseDouble(line.replace("TEMP:", "").trim());
                            lastTemperature = temp;
                            onTemperatureRead.accept(temp);
                        } catch (NumberFormatException ignored) {}
                    }
                }
            } catch (Exception e){
                // Sort proprement en cas d'erreur
                listening = false;
            }
        }, "TempSensorListenThread");
        listenThread.setDaemon(true);
        listenThread.start();
    }

    public void stopListening(){
        listening = false;
        if (listenThread != null){
            try { listenThread.interrupt(); } catch (Exception ignored) {}
            listenThread = null;
        }
    }

    public double getLastTemperature(){
        return lastTemperature;
    }
}
