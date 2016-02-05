package simple;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.swing.DefaultListModel;

public class user {
    private static final int USER_THROTTLE = 200;
    private Socket socket;
    private boolean connected;
    private Inport inport;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private String username;
    
    private DefaultListModel listModel = new DefaultListModel();
    
    private class Inport extends Thread
    {
        public void run()
        {   
            SimpleMessengerUI.JlblStatus.setText("Connected");
            // Enter process loop
            while(true)
            {
                try {
                    //Read message from server
                    Message m = (Message)(in.readObject());
                    if (m.type.equals("message"))
                    {
                        //Show message in chat
                        SimpleMessengerUI.JtxtChat.append("["+m.time+"] "+m.sender + ":\n" + m.content + "\n");
                    }
                    else if (m.type.equals("connecting"))
                    {
                        SimpleMessengerUI.JtxtChat.append("["+m.time+"] "+m.sender + " has joined the chat \n");
                        //Add user to connected list
                        listModel.addElement(m.content);
                        SimpleMessengerUI.JList1.setModel(listModel);
                    }
                    else if (m.type.equals("disconnected"))
                    {
                        SimpleMessengerUI.JtxtChat.append("["+m.time+"] "+m.sender + " has disconnected from the chat \n");
                        //remove user from connected list
                        for (int i = 0; i < listModel.size(); i++) {
                            if (listModel.get(i).equals(m.content))
                            {
                                listModel.remove(i);
                            }
                        }
                        SimpleMessengerUI.JList1.setModel(listModel);
                    }
                } catch (Exception e) {
                    System.out.println("Couldn't read message " + e);
                    return;
                }

                //Sleep
                try
                {
                    Thread.sleep(USER_THROTTLE);
                }
                catch(Exception e)
                {
                    System.out.println(toString()+" has input interrupted.");
                }
            }
        }
    }
    
    public void sendMessage (String message)
    {
        //Send message to the server
        try {
            String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
            Message m = new Message("message", username, message,timeStamp);
            out.writeObject(m);
            out.flush();
            //Clear message box
            SimpleMessengerUI.JtxtMessage.setText("");
        } catch (Exception e) {
            System.out.println("Message couldn't be sent: " + e);
        }
    }
    
    /**
     * Creates a new Client User with the socket from the newly connected client.
     *
     * @param newSocket  The socket from the connected client.
     */
    public user(Socket newSocket, String username)
    {
        // Set properties
        socket = newSocket;
        connected = true;
        this.username = username;
        try
        {
            // Open the OutputStream
            out = new ObjectOutputStream(socket.getOutputStream());
            // Send username to server
            String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
            Message m = new Message("connecting", username, username,timeStamp);
            out.writeObject(m);
            out.flush();
        }
        catch(IOException e)
        {
            System.out.println("Could not get Output stream from "+toString());
            return;
        }
        
        //Open InputStream
        try {
            in = new ObjectInputStream(socket.getInputStream());
        } catch (Exception e) {
            System.out.println("Could not get Input stream from "+toString());
            return;
        } 
        
        // Get input
        inport = new Inport();
        inport.start();
    }
    /**
     * Gets the connection status of this user.
     *
     * @return  If this user is still connected.
     */
    public boolean isConnected()
    {
        return connected;
    }
    /**
     * Purges this user from connection.
     */
    public void purge()
    {
        // Close everything
        try
        {
                connected = false;
                socket.close();
                return;
        }
        catch(IOException e)
        {
                System.out.println("Could not purge "+socket+".");
        }
    }
    /**
     * Returns the String representation of this user.
     *
     * @return  A string representation.
     */
    public String toString()
    {
        return new String(socket.toString());
    }
}
