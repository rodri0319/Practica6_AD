/* Practica 6 - Código de servidor
Alvarez Santiago Daniela
Martínez Vivaldo Uriel Giovanni
Pedroza Velarde Luis Rodrigo
*/
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetSocketAddress;

public class Server {
    public static void main(String[] args) {
        int port = 8888; // Puerto del servidor
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("El servidor está escuchando en el puerto " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Cliente conectado desde " + clientSocket.getInetAddress().getHostAddress());

                // Configura un nuevo subproceso para manejar la solicitud del cliente
                Thread clientHandler = new Thread(new ClientHandler(clientSocket));
                clientHandler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class ClientHandler implements Runnable {
    private Socket clientSocket;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try (
            DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream())
        ) {
            String request = dis.readUTF();

            if (request.equals("UPLOAD")) {
                // Cargar un archivo al servidor
                String fileName = dis.readUTF();
                long fileSize = dis.readLong();
                receiveFile(fileName, fileSize, dis);
            } else if (request.equals("DOWNLOAD")) {
                // Descargar un archivo desde el servidor
                String fileName = dis.readUTF();
                sendFile(fileName, dos);
            } else {
                System.out.println("Solicitud no válida: " + request);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void receiveFile(String fileName, long fileSize, DataInputStream dis) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(fileName)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            long bytesReceived = 0;
            while (bytesReceived < fileSize && (bytesRead = dis.read(buffer, 0, (int) Math.min(buffer.length, fileSize - bytesReceived))) != -1) {
                fos.write(buffer, 0, bytesRead);
                bytesReceived += bytesRead;
            }
            System.out.println("Archivo recibido: " + fileName);
        }
    }

    private void sendFile(String fileName, DataOutputStream dos) throws IOException {
        File file = new File(fileName);
        if (file.exists() && file.isFile()) {
            long fileSize = file.length();
            dos.writeLong(fileSize);

            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    dos.write(buffer, 0, bytesRead);
                }
                System.out.println("Archivo enviado: " + fileName);
            }
        } else {
            dos.writeLong(0); // Envía un tamaño de archivo 0 para indicar que el archivo no existe
            System.out.println("Archivo no encontrado: " + fileName);
        }
    }
}
