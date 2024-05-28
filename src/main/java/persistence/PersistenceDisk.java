package persistence;

import appcontroller.AppController;
import models.Admin;
import models.Driver;
import models.Shipment;
import models.User;
import utils.Utils;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class PersistenceDisk {



    public static void recordLogin(Object user, LocalDateTime date) {
        String tipo = "", nombre = "";
        int id = -1;
        if (user instanceof User) {
            tipo = "usuario";
            id = ((User) user).getId();
            nombre = ((User) user).getName();
        }
        if (user instanceof Driver) {
            tipo = "conductor";
            id = ((Driver) user).getId();
            nombre = ((Driver) user).getName();
        }
        if (user instanceof Admin) {
            tipo = "administrador";
            id = ((Admin) user).getId();
            nombre = ((Admin) user).getName();
        }
        try {
            FileWriter fw = new FileWriter(Config.getPathRegisterLogin(), true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write("\"Inicio de sesión\";" + nombre
                     + ";" + tipo + ";" + Utils.fechaAString(date) + "\n");
            bw.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    public static void recordUpdateShipment(Shipment s, LocalDateTime date) {
        FileWriter fw;
        try {
            fw = new FileWriter(Config.getPathRegisterLogin(), true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write("\"Actualización del envío\";" + s.getId() + ";" + s.getStatus() + ";" + Utils.fechaAString(date) + "\n");
            bw.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void closeRegister(String nombre, String tipo, LocalDateTime date) {
        try {
            FileWriter fw = new FileWriter(Config.getPathRegisterLogin(), true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write("\"Cierre de sesión\";"+ nombre + ";"
                     + tipo + ";" + Utils.fechaAString(date) + "\n");
            bw.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static void recordShipment(int idRecieved, int idSender, LocalDateTime date) {
        try {
            FileWriter fw = new FileWriter(Config.getPathRegisterLogin(), true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write("\"Nuevo envío\";" + ((idRecieved == -1) ? "No registrado" : idRecieved) + ";" + idSender +
                     ";" + Utils.fechaAString(date) + "\n");
            bw.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void saveUser(User user) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(Config.getPathUsers() + "/" + user.getId() + ".dat");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(user);
            oos.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void saveDriver(Driver driver) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(Config.getPathDrivers() + "/" + driver.getId() + ".dat");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(driver);
            oos.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void saveAdmin(Admin admin) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(Config.getPathAdmins() + "/" + admin.getId() + ".dat");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(admin);
            oos.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void savePackageUnassigned(Shipment s) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(Config.getPathPackageUnassigned() + "/" + s.getId() + ".dat");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(s);
            oos.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static void savePackageNoRegisterUser(Shipment s) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(Config.getPathPackageNoRegisterUser() + "/" + s.getId() + ".dat");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(s);
            oos.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean backup(AppController appController, String route) {
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(route);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(appController);
            oos.close();
        } catch (IOException e) {
            return false;
        }
        return true;
    }


    public static AppController restore(String route) {
        FileInputStream fis = null;
        AppController appController = null;
        try {
            fis = new FileInputStream(route);
            ObjectInputStream ois = new ObjectInputStream(fis);
            appController = (AppController) ois.readObject();
            ois.close();
            return appController;
        } catch (ClassNotFoundException | IOException e) {
            return null;
        }
    }

    public static boolean existsData() {
        File f = new File(Config.getPathData());
        return f.list().length > 0;
    }

    public static ArrayList<User> readUsersDisk() {
        File f = new File(Config.getPathUsers());
        if (f.list().length == 0) return new ArrayList<>();
        ArrayList<User> users = new ArrayList<>();
        String[] ficheros = f.list();
        for (int i = 0; i < ficheros.length; i++) {
            FileInputStream fis;
            try {
                fis = new FileInputStream(Config.getPathUsers() + "/" + ficheros[i]);
                ObjectInputStream ois = new ObjectInputStream(fis);
                User temp = (User) ois.readObject();
                users.add(temp);
                ois.close();
            } catch (IOException | ClassNotFoundException e) {
                return null;
            }

        }
        return users;
    }

    public static ArrayList<Shipment> readPackageUnassignedDisk() {
        File f = new File(Config.getPathPackageUnassigned());
        if (f.list().length == 0) return new ArrayList<>();
        ArrayList<Shipment> shipments = new ArrayList<>();
        String[] ficheros = f.list();
        for (int i = 0; i < ficheros.length; i++) {
            FileInputStream fis;
            try {
                fis = new FileInputStream(Config.getPathPackageUnassigned() + "/" + ficheros[i]);
                ObjectInputStream ois = new ObjectInputStream(fis);
                Shipment temp = (Shipment) ois.readObject();
                shipments.add(temp);
                ois.close();
            } catch (IOException | ClassNotFoundException e) {
                return null;
            }

        }
        return shipments;
    }

    public static ArrayList<Shipment> readPackageNoRegisterUserDisk() {
        File f = new File(Config.getPathPackageNoRegisterUser());
        if (f.list().length == 0) return new ArrayList<>();
        ArrayList<Shipment> shipments = new ArrayList<>();
        String[] ficheros = f.list();
        for (int i = 0; i < ficheros.length; i++) {
            FileInputStream fis;
            try {
                fis = new FileInputStream(Config.getPathPackageNoRegisterUser() + "/" + ficheros[i]);
                ObjectInputStream ois = new ObjectInputStream(fis);
                Shipment temp = (Shipment) ois.readObject();
                shipments.add(temp);
                ois.close();
            } catch (IOException | ClassNotFoundException e) {
                return null;
            }

        }
        return shipments;
    }

    public static ArrayList<Driver> readDriversDisk() {
        File f = new File(Config.getPathDrivers());
        if (f.list().length == 0) return new ArrayList<>();
        ArrayList<Driver> drivers = new ArrayList<>();
        String[] ficheros = f.list();
        for (int i = 0; i < ficheros.length; i++) {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(Config.getPathDrivers() + "/" + ficheros[i]);
                ObjectInputStream ois = new ObjectInputStream(fis);
                Driver temp = (Driver) ois.readObject();
                drivers.add(temp);
                ois.close();
            } catch (IOException | ClassNotFoundException e) {
                return null;
            }

        }
        return drivers;
    }

    public static ArrayList<Admin> readAdminsDisk() {
        File f = new File(Config.getPathAdmins());
        if (f.list().length == 0) return new ArrayList<>();
        ArrayList<Admin> admins = new ArrayList<>();
        String[] ficheros = f.list();
        for (int i = 0; i < ficheros.length; i++) {
            FileInputStream fis;
            try {
                fis = new FileInputStream(Config.getPathAdmins() + "/" + ficheros[i]);
                ObjectInputStream ois = new ObjectInputStream(fis);
                Admin temp = (Admin) ois.readObject();
                admins.add(temp);
                ois.close();
            } catch (IOException | ClassNotFoundException e) {
                return null;
            }

        }
        return admins;
    }

    public static void deletePackageUnassigned(int idShipment) {
        File f = new File(Config.getPathPackageUnassigned());
        String[] ficheros = f.list();
        for (int i = 0; i < ficheros.length; i++) {
            File delete = new File(Config.getPathPackageUnassigned() + ficheros[i]);
            if (delete.equals(idShipment + ".dat")) delete.delete();
        }
    }

    public static void deletePackageToNoRegisterUser(int id) {
        File f = new File(Config.getPathPackageNoRegisterUser() + "/" + id + ".dat");
        if (f.exists()) {
            f.delete();
        }
    }
}
