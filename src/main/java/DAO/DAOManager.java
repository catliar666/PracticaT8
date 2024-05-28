package DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DAOManager {
    private Connection conn;
    private final String URL;
    private final String USER;
    private final String PASS;
    private static DAOManager singlenton;

    public DAOManager() { //Constructor privado para que no se pueda llamas las veces que se quiera
        this.conn = null;
        this.URL = "jdbc:mysql://127.0.0.1:3306/PracticaT8"; //Enlazo la dirección del servidor de la base de datos a usar
        this.USER = "root"; //Usuario de la bbdd
        this.PASS = "root"; //Contraseña de la bbdd
    }

    public static DAOManager getSinglentonInstance(){ //Método que devuelve el DAO,
        // si el atributo estático ya ha sido inicializado no devuelve nada
        if (singlenton == null) singlenton = new DAOManager();
        return singlenton;
    }

    public Connection getConn(){
        return conn;
    }
    public void open() throws Exception {
        try{
            Class.forName("com.mysql.cj.jdbc.Driver"); //Cargo el driver de conexión jdbc
            conn = DriverManager.getConnection(URL, USER, PASS); //Uso la clase DriverManager para crear la conexión
        } catch (Exception e) {
            throw e;
        }
    }

    public void close() throws SQLException {
        try {
            if (this.conn != null) {
                this.conn.close();
            }
        } catch (Exception e) {
            throw e;
        }
    }
}
