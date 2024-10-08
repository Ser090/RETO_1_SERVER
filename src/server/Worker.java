/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import utilidades.Mensaje;
import utilidades.Signable;
import utilidades.User;

/**
 *
 * @author Sergio
 */
public class Worker implements Runnable, Signable {

    private Socket clienteSocket;
    private Signable dao;

    public Worker(Socket clienteSocket, Signable dao) {
        this.clienteSocket = clienteSocket;
        this.dao = dao;
    }

    @Override
    public void run() {
        try (ObjectOutputStream outputStream = new ObjectOutputStream(clienteSocket.getOutputStream());
                ObjectInputStream inputStream = new ObjectInputStream(clienteSocket.getInputStream())) {

        } catch (IOException ex) {
            Logger.getLogger(Worker.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public Mensaje signIn(User user) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Mensaje signUp(User user) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
