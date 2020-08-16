package com.dbxprts.comedores;

import gnu.io.*;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class TwoWaySerialComm {
    ExecutorService executor = Executors.newFixedThreadPool(2);
    PublishSubject<String> mObservable = PublishSubject.create();
    CommPort commPort;
    Disposable disposable;

    public TwoWaySerialComm() {
        super();
    }

    void connect(String portName) {
        try {
            CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);

            if (portIdentifier.isCurrentlyOwned()) {
                commPort.close();
            }
            commPort = portIdentifier.open(this.getClass().getName(), 2000);

            if (commPort instanceof SerialPort) {
                System.out.println("Port is ready");
                SerialPort serialPort = (SerialPort) commPort;
                serialPort.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort
                        .PARITY_NONE);

                InputStream in = serialPort.getInputStream();
                OutputStream out = serialPort.getOutputStream();

                executor.execute(new Thread(new SerialReader(in, mObservable)));
                executor.execute(new Thread(new SerialWriter(out)));

                disposable = mObservable.subscribe(s -> {
                    try {
                        in.close();
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    disposable.dispose();
                });
            } else {
                System.out.println("Error: Only serial ports are handled by this example.");
            }
        } catch (UnsupportedCommOperationException | IOException | NoSuchPortException | PortInUseException | AWTException e) {
            e.printStackTrace();
        }
    }

    public static class SerialReader implements Runnable {
        InputStream in;
        PublishSubject<String> mObservable;
        Robot robot;

        public SerialReader(InputStream in, PublishSubject<String> mObservable) throws AWTException {
            this.in = in;
            this.mObservable = mObservable;
            robot = new Robot();
        }

        public void run() {
            byte[] buffer = new byte[10];
            int len = -1;
            try {
                while ((len = this.in.read(buffer)) > -1) {
                    String result = new String(buffer, 0, len, "UTF-8");

                    if(isLong(result)) {
                        typeNumber(result);
                    }
                    Thread.sleep(500);
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }

        public boolean isLong(String input) {
            try{
                Integer.parseInt(input);
                return true;
            } catch(NumberFormatException e) {
                return false;
            }
        }

        public void typeNumber(String number){
            StringSelection stringSelection = new StringSelection(number);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, stringSelection);

            robot.keyPress(KeyEvent.VK_CONTROL);
            robot.keyPress(KeyEvent.VK_V);
            robot.keyRelease(KeyEvent.VK_V);
            robot.keyRelease(KeyEvent.VK_CONTROL);
        }
    }

    public static class SerialWriter implements Runnable {
        OutputStream out;

        public SerialWriter(OutputStream out) {
            this.out = out;
        }

        public void run() {
            try {
                byte[] c = "P".getBytes();
                this.out.write(c);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        try {
            //listPorts();
            (new TwoWaySerialComm()).connect("COM7");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public String listPorts()
    {
        StringBuilder ports = new StringBuilder();
        java.util.Enumeration<CommPortIdentifier> portEnum = CommPortIdentifier.getPortIdentifiers();
        while ( portEnum.hasMoreElements() )
        {
            CommPortIdentifier portIdentifier = portEnum.nextElement();
            System.out.println(portIdentifier.getName()  +  " - " +  getPortTypeName(portIdentifier.getPortType()) );
            ports.append(" || ").append(portIdentifier.getName()).append(" - ").append(getPortTypeName(portIdentifier.getPortType()));
        }

        return ports.toString();
    }

    static String getPortTypeName ( int portType )
    {
        switch ( portType )
        {
            case CommPortIdentifier.PORT_I2C:
                return "I2C";
            case CommPortIdentifier.PORT_PARALLEL:
                return "Parallel";
            case CommPortIdentifier.PORT_RAW:
                return "Raw";
            case CommPortIdentifier.PORT_RS485:
                return "RS485";
            case CommPortIdentifier.PORT_SERIAL:
                return "Serial";
            default:
                return "unknown type";
        }
    }

}