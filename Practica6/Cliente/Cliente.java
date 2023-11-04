/* Practica 6 - Código de cliente
Alvarez Santiago Daniela
Martíne Vivaldo Uriel Giovanni
Pedroza Velarde Luis Rodrigo
*/
import java.io.*;
import java.net.Socket;

public class Cliente {
    public static void main(String[] args) {
        String serverAddress = "localhost"; // Cambia esto a la dirección del servidor si es diferente
        int serverPort = 8888; // Cambia esto al puerto del servidor si es diferente

        try {
            Socket clientSocket = new Socket(serverAddress, serverPort);
            DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());

            // Menú para elegir entre cargar o descargar
            System.out.println("1. Cargar Archivo");
            System.out.println("2. Descargar Archivo");

            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("Ingresa tu elección (1/2): ");
            String choice = reader.readLine();

            if (choice.equals("1")) {
                // Cargar archivo
                dos.writeUTF("UPLOAD");

                System.out.print("Ingresa la ruta del archivo que deseas cargar: ");
                String filePath = reader.readLine();
                File file = new File(filePath);

                if (file.exists() && file.isFile()) {
                    dos.writeUTF(file.getName());
                    dos.writeLong(file.length());

                    try (FileInputStream fis = new FileInputStream(file)) {
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = fis.read(buffer)) != -1) {
                            dos.write(buffer, 0, bytesRead);
                        }
                        System.out.println("Archivo cargado exitosamente");
                    }
                } else {
                    System.out.println("El archivo no existe o no es válido.");
                }
            } else if (choice.equals("2")) {
                // Descargar archivo
                dos.writeUTF("DOWNLOAD");

                System.out.print("Ingresa el nombre del archivo que deseas descargar: ");
                String fileName = reader.readLine();
                dos.writeUTF(fileName);

                long fileSize = dis.readLong();

                if (fileSize > 0) {
                    try (FileOutputStream fos = new FileOutputStream(fileName)) {
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        long bytesReceived = 0;
                        while (bytesReceived < fileSize && (bytesRead = dis.read(buffer)) != -1) {
                            fos.write(buffer, 0, bytesRead);
                            bytesReceived += bytesRead;
                        }
                        System.out.println("Archivo descargado exitosamente");
                    }
                } else {
                    System.out.println("El archivo no existe en el servidor.");
                }
            } else {
                System.out.println("Elección no válida.");
            }

            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
