package main;

import appcontroller.AppController;
import comunication.AvisoCorreo;
import comunication.Mensajes;
import comunication.ValidarCorreo;
import persistence.Config;
import dataclass.InfoShipmentDataClass;
import dataclass.Menus;
import models.Admin;
import models.Driver;
import models.Shipment;
import models.User;
import persistence.PersistenceDisk;
import utils.Utils;

import java.time.LocalDateTime;
import java.util.*;

public class Main {

    public static final Scanner S = new Scanner(System.in);

    public static void main(String[] args) {
        AppController appController = new AppController();
        programaPrincipal(appController);
    }


    public static void programaPrincipal(AppController appController) {
        Object user = null;
        do {
            int option = startMenu();
            switch (option) {
                case 1: // Login (Inicio de sesión)
                    user = login(appController);
                    break;
                case 2: // Register (Registro)
                    user = registro(appController);
                    break;
                case 3:
                    if (Config.getUserInvited()) {
                        // Search for shipment with reference number (Búsqueda con número de referencia)
                        searchShipmentNoLogin(appController);
                        Utils.clickToContinue();
                        break;
                    } else {
                        System.out.println("Opción incorrecta, no existe");
                        break;
                    }
                default:
                    System.out.println("Opción incorrecta, no existe");
                    break;
            }

            if (user != null) {
                compruebaUsuarioMenus(user, appController);
                user = null; // Reset user to null after handling menus
            }
        } while (true);
    }

    public static void compruebaUsuarioMenus(Object user, AppController appController) {
        if (user instanceof User) userMenu(appController, (User) user);
        if (user instanceof Driver) driverMenu(appController, (Driver) user);
        if (user instanceof Admin) adminMenu(appController, (Admin) user);
    }


    public static void searchShipmentNoLogin(AppController controller) {
        int idShipment;
        try {
            System.out.print("Introduzca un número de seguimiento: ");
            idShipment = Integer.parseInt(S.nextLine());
            Shipment shipmentFind = controller.searchShipmentById(idShipment);
            if (shipmentFind == null) System.out.println("""
                    ┌──. ■ .─────────────────────────────┐
                      No se ha podido encontrar el envío
                    └─────────────────────────────. ■ .──┘""");
            else {
                System.out.println(shipmentFind.resumeToNoLogin());
            }
        } catch (NumberFormatException e) {
            System.out.println("""
                    ┌──. ■ .─────────────────────────────┐
                      Error. Debes introducir un número
                    └─────────────────────────────. ■ .──┘""");
        }

    }

    //PANTALLA DE LOGIN
    public static int startMenu() {
        int op = 0;
        try {
            if (Config.getUserInvited()) System.out.println(Menus.startMenuInvited());
            else System.out.println(Menus.startMenuNotInvited());
            op = Integer.parseInt(S.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Debes introducir una opción del menú");
            Utils.clickToContinue();
        }
        return op;
    }

    //MENÚ DE LOGIN PARA UN CLIENTE
    public static Object login(AppController appController) {
        Object user;
        System.out.print("Introduce tu email: ");
        String email = S.nextLine();
        System.out.print("Introduce tu contraseña: ");
        String pass = S.nextLine();

        /*Comprueba usuarios, conductores y administradores
        si existe meter el usuario en user, sino existe en ninguna de las clases devolver un sout
        indicando que ha sido incorrecto
         */
        user = appController.login(email, pass);
        if (user == null) System.out.println("Ese usuario no existe");
        else
            Utils.login();
        return user;
    }

    //MENÚ DE OPCIONES DEL USUARIO
    public static void userMenu(AppController controller, User user) {
        int op = 0;
        if (validateCount(user, controller)) {
            changeFirstLogin(user, controller);
            do {

                System.out.printf("""
                        ┌──. ■ .─────────────────────────────────────────────────────────────────┐
                        █ Bienvenido %-7s                                                     █
                        █ Tiene %-2d paquetes pendientes de entrega.                               █
                        █ Último inicio de sesión: %-21s                         █
                        █ ────────────────────────────────────────────────────────────────────── █
                        █ Menú de operaciones:                                                   █
                        █    1. Realizar un envío.                                               █
                        █    2. Muestra de información sobre los envíos que me han realizado.    █
                        █    3. Modificar mis datos de entrega para un envío.                    █
                        █    4. Muestra de información de los envíos que yo he realizado.        █
                        █    5. Ver mi perfil.                                                   █
                        █    6. Modificar mis datos.                                             █
                        █    7. Cerrar sesión.                                                   █
                        └─────────────────────────────────────────────────────────────────. ■ .──┘
                        Elija una opción:""", user.getName(), user.numDeliveriesPendingToDeliver(), ((controller.getLastLogin(user) == null) ? "No hay datos" : controller.getLastLogin(user)));
                try {
                    op = Integer.parseInt(S.nextLine());
                    switch (op) {
                        case 1:
                            makeShipment(controller, user);
                            Utils.clickToContinue();
                            break;
                        case 2:
                            drawInfoShipmentsUser(controller, user);
                            Utils.clickToContinue();
                            break;
                        case 3:
                            changeDeliveryData(controller, user);
                            Utils.clickToContinue();
                            break;
                        case 4:
                            drawShipmentISent(controller, user);
                            Utils.clickToContinue();
                            break;
                        case 5:
                            showProfileUser(user, controller);
                            Utils.clickToContinue();
                            break;
                        case 6:
                            modifyProfileUser(controller, user);
                            Utils.clickToContinue();
                            break;
                        case 7:
                            closeLogin(controller, user);
                            break;
                        default:
                            System.out.println("""
                                    ┌──. ■ .─────────────────────────────────┐
                                     Error. Debes elegir una opción del menú
                                    └─────────────────────────────────. ■ .──┘""");
                            break;
                    }
                } catch (NumberFormatException e) {
                    System.out.println("""
                            ┌──. ■ .─────────────────────────────┐
                              Error. Debes introducir un número
                            └─────────────────────────────. ■ .──┘""");
                }
            } while (op != 7);
        }
    }

    private static void showProfileUser(User user, AppController controller) {
        User userShow = controller.searchUserById(user.getId());
        System.out.println(userShow);
    }

    private static void changeFirstLogin(User user, AppController controller) {
        user.setFirst_login(true);
        controller.updateUser(user);
    }

    private static boolean validateCount(User user, AppController controller) {
        if (user.first_login && !user.isValidate()) {
            System.out.println("""
                    ┌──. ■ .──────────────────────────────────┐
                     Su cuenta actualmente no ha sido validada
                    └──────────────────────────────────. ■ .──┘""");
            System.out.print("Por favor, introduzca el código que se le proporcionó por email: ");
            try {
                if (user.validarToken(Integer.parseInt(S.nextLine()))) {
                    System.out.println("""
                            ┌──. ■ .────────────────────┐
                             Su cuenta ha sido validada
                            └────────────────────. ■ .──┘""");
                    user.setValidate(true);
                    controller.updateUser(user);
                    return true;
                } else {
                    System.out.println("""
                            ┌──. ■ .─────────────────┐
                             El código no es correcto
                            └─────────────────. ■ .──┘""");
                    return false;
                }
            } catch (NumberFormatException e) {
                System.out.println("""
                        ┌──. ■ .───────────────────────┐
                         Error. El código no es correcto
                        └───────────────────────. ■ .──┘""");
                return false;
            }
        } else if (!user.isFirst_login() && !user.isValidate()) {
            return true;
        } else if (!user.isFirst_login() && user.isValidate()) {
            return true;
        } else return user.isValidate() && user.isFirst_login();
    }

    private static void closeLogin(AppController controller, Object user) {
        if (user instanceof User) controller.closeLogin(user);
        if (user instanceof Driver) controller.closeLogin(user);
        if (user instanceof Admin) controller.closeLogin(user);
    }

    public static void modifyProfileUser(AppController controller, User user) {
        String op;
        do {
            System.out.print("""
                    ┌──. ■ .─────────────────────────────────────────────────────────┐
                    █              ¿Qué desea modificar de su perfil?                █
                    █ Menú de operaciones:                                           █
                    █    1. Dirección de entrega.                                    █
                    █    2. Número de telefono.                                      █
                    █    3. Nombre y apellidos.                                      █
                    █    4. Contraseña.                                              █
                    █    5. Activar o desactivar suscripción de notificaciones.      █
                    █    6. Email.                                                   █
                    █    7. Salir                                                    █
                    └─────────────────────────────────────────────────────────. ■ .──┘
                    Elija una opción:""");
            op = S.nextLine();
            switch (op) {
                case "1":
                    changeStreet(user, controller);
                    break;
                case "2":
                    changePhone(user, controller);
                    break;
                case "3":
                    changeName(user, controller);
                    changeSurname(user, controller);
                    break;
                case "4":
                    changePass(user, controller);
                    break;
                case "5":
                    changeNotifications(user, controller);
                    break;
                case "6":
                    changeEmail(user, controller);
                    break;
                case "7":
                    Utils.exitOption();
                    break;
                default:
                    System.out.println("""
                            ┌──. ■ .──────────────────────────────────────────────┐
                              Error. Debes elegir una opción mostrada en el menú
                            └──────────────────────────────────────────────. ■ .──┘""");
                    break;
            }
        } while (!op.equals("7"));
    }

    private static void changeStreet(User user, AppController controller) {
        System.out.print("Introduce la nueva calle/avd/paseo: ");
        user.setStreet(S.nextLine());
        System.out.print("Introduce el nuevo número de portal: ");
        try {
            user.setNum(Integer.parseInt(S.nextLine()));
            System.out.print("Introduce una ciudad: ");
            user.setCity(S.nextLine());
            System.out.print("Introduce una provincia: ");
            user.setState(S.nextLine());
            System.out.print("Introduce el código postal: ");
            user.setPostalCode(Integer.parseInt(S.nextLine()));

            if (controller.updateUser(user)) {
                System.out.println("""
                        ┌──. ■ .──────────────────────────┐
                          Dirección cambiada correctamente
                        └──────────────────────────. ■ .──┘""");
            } else System.out.println("""
                    ┌──. ■ .───────────────────────┐
                     Error al actualizar los datos
                    └───────────────────────. ■ .──┘""");
        } catch (NumberFormatException e) {
            System.out.println("""
                    ┌──. ■ .─────────────────────────────┐
                      Error. Debes introducir un número
                    └─────────────────────────────. ■ .──┘""");
        }
    }

    private static void changeNotifications(User user, AppController controller) {
        System.out.print("Introduce tu contraseña: ");
        if (user.checkPass(S.nextLine())) {
            if (user.isNotification()) {
                user.setNotification(false);
                if (controller.updateUser(user)) {
                    System.out.println("""
                            ┌──. ■ .───────────────────────────────────────┐
                                Notificaciones desactivadas correctamente
                            └───────────────────────────────────────. ■ .──┘""");
                } else System.out.println("""
                        ┌──. ■ .───────────────────────┐
                         Error al actualizar los datos
                        └───────────────────────. ■ .──┘""");
            } else {
                user.setNotification(true);
                if (controller.updateUser(user)) {
                    System.out.println("""
                            ┌──. ■ .─────────────────────────────────────┐
                                Notificaciones activadas correctamente
                            └─────────────────────────────────────. ■ .──┘""");
                } else System.out.println("""
                        ┌──. ■ .───────────────────────┐
                         Error al actualizar los datos
                        └───────────────────────. ■ .──┘""");
            }
        } else System.out.println("""
                ┌──. ■ .──────────────────────────┐
                    Error. Vuelve a intentarlo
                └──────────────────────────. ■ .──┘""");
    }

    private static void changePhone(User user, AppController controller) {
        System.out.print("Introduce el nuevo número de teléfono: ");
        try {
            user.setPhone(Integer.parseInt(S.nextLine()));
            if (controller.updateUser(user)) {
                System.out.println("""
                        ┌──. ■ .──────────────────────────────────┐
                         Número de teléfono cambiado correctamente
                        └──────────────────────────────────. ■ .──┘""");
            } else System.out.println("""
                    ┌──. ■ .───────────────────────┐
                     Error al actualizar los datos
                    └───────────────────────. ■ .──┘""");

        } catch (NumberFormatException e) {
            System.out.println("""
                    ┌──. ■ .─────────────────────────────┐
                      Error. Debes introducir un número
                    └─────────────────────────────. ■ .──┘""");
        }
    }

    private static void changeSurname(User user, AppController controller) {
        System.out.print("Introduce tus apellidos: ");
        user.setSurname(S.nextLine());
        if (controller.updateUser(user)) {
            System.out.println("""
                    ┌──. ■ .──────────────────────────┐
                     Apellidos cambiados correctamente
                    └──────────────────────────. ■ .──┘""");
        } else System.out.println("""
                ┌──. ■ .───────────────────────┐
                 Error al actualizar los datos
                └───────────────────────. ■ .──┘""");

    }

    public static void drawShipmentISent(AppController controller, User user) {
        ArrayList<InfoShipmentDataClass> results = controller.getShipmentSendByUser(user.getId());
        if (results.isEmpty()) System.out.printf("""
                ┌──. ■ .──────────────────────────┐
                  Este usuario a enviado %d envíos
                └──────────────────────────. ■ .──┘\n""", controller.getNumShipmentsMadeByUser(user.getId()));
        else {
            System.out.printf("""
                    ┌──. ■ .──────────────────────────┐
                      Este usuario a enviado %d envíos
                    └──────────────────────────. ■ .──┘\n""", controller.getNumShipmentsMadeByUser(user.getId()));
            System.out.println("""
                    ┌──. ■ .───────────────────────────────────────────────┐
                     Los envíos se muestran del más reciente al más antiguo
                    └───────────────────────────────────────────────. ■ .──┘""");
            for (InfoShipmentDataClass s :
                    results) {
                if (s != null) System.out.println(s.forReciever());
            }
        }
    }

    public static void changeDeliveryData(AppController controller, User user) {
        int cont = 0, opShipment, newPostalCode;
        try {
            ArrayList<InfoShipmentDataClass> shipmentPendings = controller.getShipmentPendingsToUser(user.getId());
            if (shipmentPendings.isEmpty()) System.out.println("""
                    ┌──. ■ .───────────────────────┐
                     No existen envíos para mostrar
                    └───────────────────────. ■ .──┘""");
            else {
                System.out.println("""
                                          
                        ┌──. ■ .─────────────────────────────────────┐
                         Los envíos se muestran desde el más reciente,
                                    al más antiguo.
                        └─────────────────────────────────────. ■ .──┘""");
                System.out.println("""
                        ┌──. ■ .──────────────────────┐
                         ¿Qué envío quieres modificar?
                        └──────────────────────. ■ .──┘""");
                for (InfoShipmentDataClass s :
                        shipmentPendings) {
                    if (s != null) {
                        cont++;
                        System.out.println("NÚMERO " + cont + " - " + "\n" + s.forTracking() + "\n");
                    }
                }
                try {
                    do {
                        System.out.println("Elije un envío según el número de referencia que aparece: ");
                        opShipment = Integer.parseInt(S.nextLine());
                    } while (!(opShipment > 0));
                    Shipment shipmentChange = user.searchDeliveryById(opShipment);
                    if (shipmentChange != null) {
                        System.out.print("Indica la nueva dirección de envío (incluye nombre de calle/avd/paseo y número de portal): ");
                        String newAddress = S.nextLine();
                        System.out.print("Indica la ciudad: ");
                        String newCity = S.nextLine();
                        do {
                            System.out.print("Indica un código postal: ");
                            newPostalCode = Integer.parseInt(S.nextLine());
                        } while (!(newPostalCode > 0));
                        if (controller.changeDeliveryData(shipmentChange.getId(), newAddress, newPostalCode, newCity))
                            System.out.println("""
                                    ┌──. ■ .─────────────────────────┐
                                      Envío modificado correctamente
                                    └─────────────────────────. ■ .──┘""");
                        else System.out.println("""
                                ┌──. ■ .─────────────────────┐
                                  Error. Vuelve a intentarlo
                                └─────────────────────. ■ .──┘""");
                    } else System.out.println("""
                            ┌──. ■ .──────────────────────────────┐
                              Error. No se ha encontrado el envío
                            └──────────────────────────────. ■ .──┘""");
                } catch (NumberFormatException e) {
                    System.out.println("""
                            ┌──. ■ .─────────────────────────────┐
                              Error. Debes introducir un número
                            └─────────────────────────────. ■ .──┘""");
                }
            }
        } catch (NullPointerException e) {
            System.out.println("""
                    ┌──. ■ .──────────────────────┐
                       No hay envíos para mostrar
                    └──────────────────────. ■ .──┘""");
        }
    }

    public static void drawInfoShipmentsUser(AppController controller, User user) {
        try {
            ArrayList<InfoShipmentDataClass> results = controller.getShipmentsFinishedUser(user.getId());
            ArrayList<InfoShipmentDataClass> shipmentPendings = controller.getShipmentPendingsToUser(user.getId());
            if (!shipmentPendings.isEmpty()) {
                System.out.println("""
                        ┌──. ■ .───────────────────────────────────────────────┐
                         Los envíos se muestran del más reciente al más antiguo
                        └───────────────────────────────────────────────. ■ .──┘""");
                System.out.println("""
                        ┌──. ■ .──────────────────────┐
                         Envíos todavía NO entregados
                        └──────────────────────. ■ .──┘""");
                for (InfoShipmentDataClass s :
                        shipmentPendings) {
                    if (s != null) {
                        System.out.println(s.forSender());
                    }
                }
            }
            if (!results.isEmpty()) {
                int cont = 0;
                for (InfoShipmentDataClass r :
                        results) {
                    if (r != null) {
                        cont++;
                        if (cont == 1) System.out.println("""
                                ┌──. ■ .─────────────┐
                                 Envíos ya entregados
                                └─────────────. ■ .──┘""");
                        System.out.println(r.forTracking());
                    }
                }
            }
            if (results.isEmpty() && shipmentPendings.isEmpty()) {
                System.out.println("""
                        ┌──. ■ .──────────────────────┐
                           No hay envíos para mostrar
                        └──────────────────────. ■ .──┘""");
            }
        } catch (NullPointerException e) {
            System.out.println("""
                    ┌──. ■ .──────────────────────┐
                       No hay envíos para mostrar
                    └──────────────────────. ■ .──┘""");
        }
    }

    public static void makeShipment(AppController controller, User user) {
        int op, postalCode;
        String name, address, email, city, nombreArchivo;
        Shipment shipment;
        boolean notification = true;
        System.out.print("Introduce el email del destinatario: ");
        email = S.nextLine();
        User userFind = controller.searchUserByEmail(email);
        if (userFind == null) {
            System.out.print("Introduce la dirección en la que se entregará el paquete: ");
            address = S.nextLine();
            System.out.print("Introduzca la ciudad en la que se entregará el paquete: ");
            city = S.nextLine();
            try {
                do {
                    System.out.print("Introduce un código postal: ");
                    postalCode = Integer.parseInt(S.nextLine());
                } while (!(postalCode > 0));
                System.out.print("Introduce el nombre del destinatario: ");
                name = S.nextLine();
                do {
                    System.out.print("""
                            ┌──. ■ .─────────────────────────────────────┐
                              ¿Deseas que al destinatario le mandemos
                                actualizaciones sobre su nuevo envío?
                            └─────────────────────────────────────. ■ .──┘
                               █ 1. Si
                               █ 2. No
                            Elija una opción:""");
                    op = Integer.parseInt(S.nextLine());
                } while (!(op > 0));
                switch (op) {
                    case 1:
                        notification = true;
                        break;
                    case 2:
                        notification = false;
                        break;
                    default:
                        System.out.print("""
                                ┌──. ■ .─────────────────────────────────────┐
                                   Al destinatario se le ha asignado que
                                   SI tendrá actualizaciones sobre su envío
                                └─────────────────────────────────────. ■ .──┘""");
                        break;
                }
                shipment = controller.addShipmentToNoRegisterUser("En oficina de origen", user.getId(), email, postalCode,
                        name, notification, address, city);
                nombreArchivo = controller.createPdf(shipment, user);
                controller.sendEmail(shipment, user, nombreArchivo, notification);
                System.out.print("""
                                         ┌──. ■ .──────────────────────┐
                                             Información del envío
                                         └──────────────────────. ■ .──┘\n""" + shipment.resume() + "\n");
                System.out.println("""
                        ┌──. ■ .─────────────────────────────┐
                          El envío ha sido creado con éxito
                        └─────────────────────────────. ■ .──┘""");
            } catch (NumberFormatException e) {
                System.out.println("""
                        ┌──. ■ .─────────────────────────────┐
                          Error. Debes introducir un número
                        └─────────────────────────────. ■ .──┘""");
            }
        } else {
            System.out.print("""
                    ┌──. ■ .───────────────────────────────────────────────────┐
                               El destinatario ha sido encontrado,
                      ¿quieres que le mandemos actualizaciones sobre el envío?
                    └───────────────────────────────────────────────────. ■ .──┘
                       █ 1. Si
                       █ 2. No
                       Elija una opción:""");
            switch (S.nextLine()) {
                case "1" -> notification = true;
                case "2" -> notification = false;
                default -> System.out.print("""
                        ┌──. ■ .─────────────────────────────────────┐
                           Al destinatario se le ha asignado que
                           SI tendrá actualizaciones sobre su envío
                        └─────────────────────────────────────. ■ .──┘""");
            }
            shipment = controller.addShipment(user.getId(), userFind.getId(), notification);
            nombreArchivo = controller.createPdf(shipment, user);
            controller.sendEmail(shipment, user, nombreArchivo, notification);

            System.out.print("""
                                     ┌──. ■ .──────────────────────┐
                                         Información del envío
                                     └──────────────────────. ■ .──┘\n""" + shipment.resume() + "\n");
            System.out.println("""
                    ┌──. ■ .─────────────────────────────┐
                      El envío ha sido creado con éxito
                    └─────────────────────────────. ■ .──┘""");
        }
    }

    //MENÚ PARA REGISTRAR UN USUARIO

    //Aquí solo se puede registrar un usuario
    public static Object registro(AppController controller) {
        int phone, num, postalCode = 0, token;
        System.out.print("Introduzca su email: ");
        String email = S.nextLine();
        if (controller.searchDriverByEmail(email) != null || controller.searchUserByEmail(email) != null)
            System.out.println("""
                    ┌──. ■ .─────────────────────────────────┐
                      El email introducido ya está registrado
                    └─────────────────────────────────. ■ .──┘""");
        else {
            try {
                boolean accept = false;
                Utils.checkEmail();
                token = ValidarCorreo.enviarToken(email);
                if (!controller.searchShipmentByEmail(email).isEmpty()) {
                    System.out.println("""
                    
                    ┌──. ■ .────────────────────────────────────────────────┐
                     Se ha encontrado información perteneciente a este email
                    └────────────────────────────────────────────────. ■ .──┘
                      ¿Desea utilizar esa información para crear su cuenta?
                              █ 1. Si
                              █ 2. No
                      Elije tu opción:""");
                    if (S.nextLine().equals("1")) accept = true;
                }
                if (accept) {
                    System.out.print("\nIntroduzca su clave: ");
                    String pass = S.nextLine();
                    phone = enterPhone();
                    System.out.print("\nIntroduzca su número de portal: ");
                    num = enterNum();
                    System.out.print("\nIntroduzca su provincia: ");
                    String state = S.nextLine();
                    User userUse = controller.createAccountShippingInfo(email, pass, phone, token, num, state);
                    if (userUse != null) {
                        System.out.println("""
                                
                                ┌──. ■ .───────────────────────┐
                                  Usuario registrado con éxito
                                └───────────────────────. ■ .──┘""");
                        validateCountRegister(userUse, controller);
                        return userUse;
                    } else System.out.println("""
                            ┌──. ■ .─────────────────────────────────────────┐
                              Ha ocurrido un error al registrarse el usuario
                            └─────────────────────────────────────────. ■ .──┘""");
                } else {
                    System.out.print("\nIntroduzca su clave: ");
                    String pass = S.nextLine();
                    System.out.print("Introduzca su nombre: ");
                    String name = S.nextLine();
                    System.out.print("Introduzca sus apellidos: ");
                    String surname = S.nextLine();
                    phone = enterPhone();
                    System.out.print("Introduzca su calle: ");
                    String street = S.nextLine();
                    System.out.print("Introduzca su ciudad: ");
                    String city = S.nextLine();
                    num = enterNum();
                    System.out.print("Introduzca su provincia: ");
                    String state = S.nextLine();
                    do {
                        try {
                            System.out.print("Introduzca su codigo postal: ");
                            postalCode = Integer.parseInt(S.nextLine());
                        } catch (NumberFormatException e) {
                            System.out.println("""
                                    ┌──. ■ .─────────────────────────────┐
                                      Error. Debes introducir un número
                                    └─────────────────────────────. ■ .──┘""");
                        }
                    } while (!(postalCode > 0));
                    if (controller.addUser(name, surname, email, phone, pass, street, num, city, state, postalCode, token)) {
                        System.out.println("""
                                ┌──. ■ .───────────────────────┐
                                  Usuario registrado con éxito
                                └───────────────────────. ■ .──┘""");
                        User userUse = controller.searchUserByEmail(email);
                        controller.findShipmentCreateUser(userUse);
                        validateCountRegister(userUse, controller);
                        if (userUse != null) controller.recordLogin(userUse, LocalDateTime.now());
                        return userUse;
                    } else System.out.println("""
                            ┌──. ■ .─────────────────────────────────────────┐
                              Ha ocurrido un error al registrarse el usuario
                            └─────────────────────────────────────────. ■ .──┘""");
                }

            } catch (RuntimeException e) {
                System.out.println("""
                        \n┌──. ■ .────────────────────────────────────┐
                           Por favor, introduzca un email correcto
                        └────────────────────────────────────. ■ .──┘""");
            }
        }
        return null;
    }

    private static int enterNum() {
        int num = -1;
        do {
            try {
                System.out.print("Introduzca su número de portal: ");
                num = Integer.parseInt(S.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("""
                                    ┌──. ■ .─────────────────────────────┐
                                      Error. Debes introducir un número
                                    └─────────────────────────────. ■ .──┘""");
            }
        } while (!(num > 0));
        return num;
    }

    private static int enterPhone() {
        int phone = -1;
        do {
            try {
                System.out.print("Introduzca su número de telefono: ");
                phone = Integer.parseInt(S.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("""
                                    ┌──. ■ .─────────────────────────────┐
                                      Error. Debes introducir un número
                                    └─────────────────────────────. ■ .──┘""");
            }
        } while (!(phone > 0));
        return phone;
    }

    private static void validateCountRegister(User userUse, AppController controller) {
        System.out.print("""
                                ┌──. ■ .──────────────────────────────────────────────────┐
                                 Le hemos enviado un email con un código de confirmación.
                                 ─────────────────────────────────────────────────────────
                                            ¿Desea verificar su cuenta ahora
                                            o en el próximo inicio de sesión?
                                └──────────────────────────────────────────────────. ■ .──┘
                                      █ 1. Verificar ahora.
                                      █ 2. Más tarde.
                                 Elija una opción:
                                 """);
        int op = Integer.parseInt(S.nextLine());
        switch (op) {
            case 1:
                System.out.print("Introduce el código que se muestra en el email: ");
                if (userUse.validarToken(Integer.parseInt(S.nextLine()))) {
                    System.out.println("""
                                            ┌──. ■ .─────────────────────────┐
                                              Cuenta validada correctamente
                                            └─────────────────────────. ■ .──┘""");
                    userUse.setValidate(true);
                    controller.updateUser(userUse);
                }
                break;
            default:
                System.out.println("""
                                        ┌──. ■ .────────────────────────────────────┐
                                          Tu cuenta no será validada ahora.
                                          
                                          Más tarde deberá hacerlo al iniciar sesión
                                          nuevamente
                                        └────────────────────────────────────. ■ .──┘""");
                break;
        }
    }

    //MENÚ PARA EL CONDUCTOR

    public static void driverMenu(AppController controller, Driver driver) {
        int op = 0;
        do {
            System.out.printf("""
                    ┌──. ■ .─────────────────────────────────────────────────────────────────┐
                    █ Bienvenido %-10s.                                                 █
                    █ Tiene %-2d paquetes pendientes de entrega.                               █
                    █ Último inicio de sesión: %-21s                         █
                    █ ────────────────────────────────────────────────────────────────────── █
                    █ Menú de operaciones:                                                   █
                    █    1. Ver la información de los envíos que debo entregar.              █
                    █    2. Cambia el estado de un envío.                                    █
                    █    3. Ver el histórico de mis paquetes entregados.                     █
                    █    4. Añade una zona de entrega a mi perfil.                           █
                    █    5. Ver mi perfil.                                                   █
                    █    6. Modificar mis datos.                                             █
                    █    7. Cerrar sesión.                                                   █
                    └─────────────────────────────────────────────────────────────────. ■ .──┘
                    Elija una opción:""", driver.getName(), driver.numShipmentsPendings(), ((controller.getLastLogin(driver) == null) ? "No hay datos" : controller.getLastLogin(driver)));
            try {
                op = Integer.parseInt(S.nextLine());
                switch (op) {
                    case 1:
                        drawInfoShipmentsDriver(controller, driver);
                        Utils.clickToContinue();
                        break;
                    case 2:
                        updateShipmentStatus(controller, driver);
                        Utils.clickToContinue();
                        break;
                    case 3:
                        getShipmentsDelivered(controller, driver);
                        Utils.clickToContinue();
                        break;
                    case 4:
                        addZoneDelivery(controller, driver);
                        Utils.clickToContinue();
                        break;
                    case 5:
                        showProfileDriver(controller, driver);
                        Utils.clickToContinue();
                        break;
                    case 6:
                        modifyProfileDriver(controller, driver);
                        Utils.clickToContinue();
                        break;
                    case 7:
                        closeLogin(controller, driver);
                        Utils.closeSesion();
                        break;
                    default:
                        //TODO: MODIFICAR
                        break;
                }
            } catch (NumberFormatException e) {
                System.out.println("""
                        ┌──. ■ .─────────────────────────────┐
                          Error. Debes introducir un número
                        └─────────────────────────────. ■ .──┘""");
            }
        } while (op != 7);
    }

    private static void showProfileDriver(AppController controller, Driver driver) {
        Driver d = controller.searchDriverById(driver.getId());
        System.out.println(d.showProfile());
    }

    public static void modifyProfileDriver(AppController controller, Driver driver) {
        int op = 0;
        do {
            System.out.print("""
                    ┌──. ■ .─────────────────────────────────────────────────────────┐
                    █              ¿Qué desea modificar de su perfil?                █
                    █ Menú de operaciones:                                           █
                    █    1. Nombre.                                                  █
                    █    2. Contraseña.                                              █
                    █    3. Email.                                                   █
                    █    4. Salir.                                                   █
                    └─────────────────────────────────────────────────────────. ■ .──┘
                    Elija una opción: """);
            try {
                op = Integer.parseInt(S.nextLine());
                switch (op) {
                    case 1:
                        changeName(driver, controller);
                        break;
                    case 2:
                        changePass(driver, controller);
                        break;
                    case 3:
                        changeEmail(driver, controller);
                        break;
                    case 4:
                        Utils.exitOption();
                        break;
                    default:
                        break;
                }
            } catch (NumberFormatException e) {
                System.out.println("""
                        ┌──. ■ .─────────────────────────────┐
                          Error. Debes introducir un número
                        └─────────────────────────────. ■ .──┘""");
            }

        } while (op != 4);
    }

    public static void addZoneDelivery(AppController controller, Driver driver) {
        System.out.print("Introduce el código postal que deseas añadir: ");
        try {
            int postalCode = Integer.parseInt(S.nextLine());
            if (driver.hasPostalCodeZone(postalCode)) System.out.println("""
                    ┌──. ■ .──────────────────────────────────────────────────────────┐
                       El codigo postal introducido ya está registrado en sus datos
                    └──────────────────────────────────────────────────────────. ■ .──┘""");
            else {
                if (controller.addZoneToDriver(driver.getId(), postalCode)) System.out.println("""
                        ┌──. ■ .─────────────────────────────────┐
                           Zona de entrega añadida correctamente
                        └─────────────────────────────────. ■ .──┘""");
                else System.out.println("""
                        ┌──. ■ .─────────────────────────────────────┐
                           Ha ocurrido un error. Vuelve a intentarlo
                        └─────────────────────────────────────. ■ .──┘""");
            }
        } catch (NumberFormatException e) {
            System.out.println("""
                    ┌──. ■ .─────────────────────────────┐
                       Error. Debes introducir un número
                    └─────────────────────────────. ■ .──┘""");
        }
    }

    public static void getShipmentsDelivered(AppController controller, Driver driver) {
        int cont = 0;
        try {
            ArrayList<InfoShipmentDataClass> shipmentDelivered = controller.getShipmentsFinishedDriver(driver.getId());
            if (!shipmentDelivered.isEmpty()) {
                System.out.println("""
                        ┌──. ■ .───────────────────────────────────────────────┐
                         Los envíos se muestran del más reciente al más antiguo
                        └───────────────────────────────────────────────. ■ .──┘""");
                for (InfoShipmentDataClass s :
                        shipmentDelivered) {
                    if (s != null) {
                        cont++;
                        System.out.println("NÚMERO " + cont + ": \n" + s.forDriverFinished());
                    }
                }
            } else System.out.println("""
                    ┌──. ■ .──────────────────────────┐
                       No existen envíos para mostrar
                    └──────────────────────────. ■ .──┘""");
        } catch (NullPointerException e) {
            System.out.println("""
                    ┌──. ■ .──────────────────────────┐
                       No existen envíos para mostrar
                    └──────────────────────────. ■ .──┘""");
        }
    }

    public static void updateShipmentStatus(AppController controller, Driver driver) {
        int cont = 0, op, opStatus;
        String newStatus = "";
        try {
            ArrayList<InfoShipmentDataClass> shipmentsDriver = controller.getShipmentsPendingsDriver(driver.getId());
            if (shipmentsDriver.isEmpty()) {
                System.out.println("""
                        ┌──. ■ .─────────────────────────┐
                           No hay envíos para mostrar
                        └─────────────────────────. ■ .──┘""");
            } else {
                System.out.println("""
                        ┌──. ■ .──────────────────────────┐
                           ¿Qué envío deseas actualizar?
                        └──────────────────────────. ■ .──┘""");
                for (int i = (shipmentsDriver.size() - 1); i >= 0; i--) {
                        cont++;
                        System.out.println("""
                                ┌──. ■ .───────────────────────────────────────────────┐
                                 Los envíos se muestran del más reciente al más antiguo
                                └───────────────────────────────────────────────. ■ .──┘""");
                        System.out.println("NÚMERO " + cont + ": \n" + shipmentsDriver.get(i).forDriverPending());
                }
                try {
                    do {
                        System.out.print("Indica el envío a actualizar según su número ID: ");
                        op = Integer.parseInt(S.nextLine());
                    } while (!(op > 0));
                    Shipment shipmentFind = controller.searchShipmentById(op);
                    if (shipmentFind == null) System.out.println("Lo sentimos, el envío que has marcado no existe");
                    else {
                        System.out.print("""
                                ┌──. ■ .──────────────────────────────┐
                                   ¿A qué estado deseas actualizarlo?
                                └──────────────────────────────. ■ .──┘
                                 █ 1. En oficina de origen.
                                 █ 2. En almacén.
                                 █ 3. En reparto.
                                 █ 4. Entregado.
                                 Elija una opción:""");
                        opStatus = Integer.parseInt(S.nextLine());
                        switch (opStatus) {
                            case 1 -> newStatus = "En oficina de origen";
                            case 2 -> newStatus = "En almacen";
                            case 3 -> newStatus = "En reparto";
                            case 4 -> newStatus = "Entregado";
                        }
                        driver.updateShipmentStatus(newStatus, shipmentFind.getId());
                        controller.changeDeliveryStatus(newStatus, shipmentFind.getId());
                        System.out.println("""
                                ┌──. ■ .─────────────────────────┐
                                  Paquete actualizado con éxito
                                └─────────────────────────. ■ .──┘""");
                    }
                    User userFind = controller.searchUserByEmail(shipmentFind.getEmailUserNoRegister());
                    if (userFind != null && userFind.isNotification()) {
                        Mensajes.enviarMensaje(userFind.getEmail(), "Actualización del envío", AvisoCorreo.generaPlantillaAviso(userFind.getName(),
                                shipmentFind.getId(), shipmentFind.getExpectDate(), shipmentFind.getStatus(), shipmentFind.getAlternativeAddress(), shipmentFind.getAlternativeCity()), null);
                    }
                } catch (NumberFormatException e) {
                    System.out.println("""
                            ┌──. ■ .─────────────────────────────┐
                              Error. Debes introducir un número
                            └─────────────────────────────. ■ .──┘""");
                }
            }
        } catch (NullPointerException e) {
            System.out.println("""
                    ┌──. ■ .─────────────────────┐
                      No hay envíos para mostrar
                    └─────────────────────. ■ .──┘""");
        }
    }

    public static void drawInfoShipmentsDriver(AppController controller, Driver driver) {
        if (driver.numShipmentsPendings() == 0) System.out.println("""
                ┌──. ■ .──────────────────────┐
                   No hay envíos para mostrar
                └──────────────────────. ■ .──┘""");
        else {
            ArrayList<InfoShipmentDataClass> results = controller.getShipmentsPendingsDriver(driver.getId());
            Collections.sort(results);
            System.out.println("""
                    ┌──. ■ .───────────────────────────────────────────────┐
                     Los envíos se muestran del más reciente al más antiguo
                    └───────────────────────────────────────────────. ■ .──┘""");
            for (InfoShipmentDataClass s : results) {
                if (s != null) {
                    System.out.println(s.forDriverPending());
                }
            }
        }
    }

    //MENÚ PARA EL ADMINISTRADOR

    //Hay que meter una opcion para añadir un conductor, ya que, solo lo puede añadir el admin.

    public static void adminMenu(AppController controller, Admin admin) {
        int op = 0;
        do {
            try {
                System.out.printf("""
                                Bienvenido: %s. Usted es administrador.
                                Último inicio de sesión: %s
                                ┌──. ■ .─────────────────────────────────────────────────────────────────┐
                                                        Estadísticas de la app
                                └─────────────────────────────────────────────────────────────────. ■ .──┘
                                █    Número de usuarios: %-2d                                             █
                                █    Número de conductores: %-2d                                          █
                                █    Número de envíos pendientes de entrega: %-2d                         █
                                █    Número de envíos sin conductor: %-2d                                 █
                                █    Número de envíos a usuarios no registrados: %-2d                     █
                                █    Promedio de días que tardamos en entregar un paquete: %.2f         █
                                               
                                ┌──. ■ .─────────────────────────────────────────────────────────────────┐
                                █ Menú de operaciones:                                                   █
                                █    1. Ver los envíos sin asignar.                                      █
                                █    2. Asignar un envío a un conductor.                                 █
                                █    3. Ver un resumen de los usuarios registrados.                      █
                                █    4. Ver un resumen de los conductores registrados.                   █
                                █    5. Ver mi perfil.                                                   █
                                █    6. Modificar mis datos.                                             █
                                █    7. Crear nueva cuenta de conductor.                                 █
                                █    8. Crear nueva cuenta de Administrador.                             █
                                █    9. Muestra la configuración de nuestro programa.                    █
                                █   10. Enviar listado de envíos por correo.                             █
                                █   11. Realizar copia de seguridad de la base de datos.                 █
                                █   12. Cerrar sesión.                                                   █
                                └─────────────────────────────────────────────────────────────────. ■ .──┘
                                Elija una opción: """, admin.getName(),
                        ((controller.getLastLogin(admin) == null) ? "No hay datos" : controller.getLastLogin(admin)), controller.numUsers(),
                        controller.numDrivers(), controller.numShipmentsPendings(),
                        controller.numShipmentsToAssing(), controller.numShipmentsToNoUserRegister(), controller.numDaysToDeliver());

                op = Integer.parseInt(S.nextLine());
                switch (op) {
                    case 1:
                        drawShippingsUnassigned(controller);
                        Utils.clickToContinue();
                        break;
                    case 2:
                        assignedShipmentADriver(controller);
                        Utils.clickToContinue();
                        break;
                    case 3:
                        resumeUserRegister(controller);
                        Utils.clickToContinue();
                        break;
                    case 4:
                        resumeDriversRegister(controller);
                        Utils.clickToContinue();
                        break;
                    case 5:
                        System.out.println(admin.showProfile());
                        Utils.clickToContinue();
                        break;
                    case 6:
                        modifyProfileAdmin(controller, admin);
                        break;
                    case 7:
                        addDriver(controller);
                        Utils.clickToContinue();
                        break;
                    case 8:
                        addAdmin(controller);
                        Utils.clickToContinue();
                        break;
                    case 9:
                        showConfigProgram(controller);
                        Utils.clickToContinue();
                        break;
                    case 10:
                        listShipmentEmail(controller, admin);
                        Utils.clickToContinue();
                        break;
                    case 11:
                        backupsBaseData(controller);
                        break;
                    case 12:
                        closeLogin(controller, admin);
                        Utils.closeSesion();
                        break;
                    default:
                        System.out.println("""
                                ┌──. ■ .─────────────────────────────────────────────────┐
                                  Error. Debes introducir una opción existente en el menú
                                └─────────────────────────────────────────────────. ■ .──┘""");
                        break;
                }
            } catch (NumberFormatException e) {
                System.out.println("""
                        ┌──. ■ .─────────────────────────────┐
                          Error. Debes introducir un número
                        └─────────────────────────────. ■ .──┘""");
            }
        } while (op != 11);
    }

    private static void backupsBaseData(AppController controller) {
        System.out.println("""
                ┌──. ■ .─────────────────────────────────────────────────────────────────┐
                                  Creación y restauración de datos
                └─────────────────────────────────────────────────────────────────. ■ .──┘
                """);
        System.out.println("""
                       █ 1. Guardar una copia de seguridad.
                       █ 2. Restaurar una copia de seguridad.
                       █ 3. Salir.
                       Elige una opción:""");
        int op = Integer.parseInt(S.nextLine());
        switch (op) {
            case 1:
                saveBackup(controller);
                break;
            case 2:
                restoreBackup(controller);
                break;
            case 3:
                Utils.closeSesion();
                break;
            default:
                System.out.println("Opción incorrecta");
                break;
        }
    }

    private static void restoreBackup(AppController controller) {
        String ruta;
        System.out.print("""
                ┌──. ■ .────────────────────────────────────────────────────┐
                  Para realizar la restauración de seguridad debe indicar
                         la ruta donde están guardados los datos
                   Ejemplo: \"\\Desktop\\backup\\FernanPaaq\"
                └────────────────────────────────────────────────────. ■ .──┘
                Introduce la ruta: """);
        ruta = S.nextLine();
        if (controller.restoreBackup(ruta)) System.out.println("""
                ┌──. ■ .───────────────────────────────────────┐
                   Copia de seguridad restaurada correctamente
                └───────────────────────────────────────. ■ .──┘ """);
        else System.out.println("""
                ┌──. ■ .───────────────────────────────────────┐
                   Error al restaurar la copia de seguridad
                └───────────────────────────────────────. ■ .──┘ """);
    }

    private static void saveBackup(AppController controller) {
        String ruta;
        System.out.print("""
                ┌──. ■ .────────────────────────────────────────────────────┐
                  Para realizar la copia de seguridad debe indicar una ruta 
                             donde quiera guardar los datos
                   Ejemplo: \"\\Desktop\\backup\\FernanPaaq\"
                └────────────────────────────────────────────────────. ■ .──┘
                Introduce la ruta: """);
        ruta = S.nextLine();
        if (controller.saveBackup(ruta)) System.out.printf("""
                ┌──. ■ .─────────────────────────────────────────────────────┐
                  Copia de seguridad guardada en: %s\\
                └─────────────────────────────────────────────────────. ■ .──┘\n""", ruta);
        else System.out.println("""
                ┌──. ■ .─────────────────────────────────┐
                  Error al guardar la copia de seguridad
                └─────────────────────────────────. ■ .──┘""");
    }

    private static void showConfigProgram(AppController controller) {
        String respuesta;
        System.out.println("""
                ┌──. ■ .─────────────────────────────────────────────────────────────────┐
                                  Configuración de FernanPaaq App
                └─────────────────────────────────────────────────────────────────. ■ .──┘
                """);
        ArrayList<String> info = controller.getInfoProperties();
        if (info != null) {
            for (String s :
                    info) {
                System.out.println("█ " + s);
            }
            System.out.println("───────────────────────────────────────────────────────────────────────────");
            System.out.print("""
                    ┌──. ■ .─────────────────────────────────────────────────────────────────┐
                       SI DESEA CAMBIAR EL PROGRAMA A \"MODO INVITADO\" TIENE DOS OPCIONES:
                    └─────────────────────────────────────────────────────────────────. ■ .──┘
                       █ 1. Modificar el fichero properties en la ruta  
                            src/main/java/config/config.properties
                       █ 2. Modificar desde aquí el fichero
                       █ 3. Salir.
                       Elige una opción: """);
            int op = Integer.parseInt(S.nextLine());
            switch (op) {
                case 1:
                    System.out.print("""
                            ┌──. ■ .───────────────────────────────────────────┐
                              Apague el programa y dirijase a la ruta indicada
                            └───────────────────────────────────────────. ■ .──┘
                            """);
                    break;
                case 2:
                    do {
                        System.out.print("""
                                ┌──. ■ .───────────────────────────────────────────┐
                                 Si desea activar el \"MODO INVITADO\" pulse \"s\",
                                 en cambio, si desea desactivarlo pulse \"n\"
                                └───────────────────────────────────────────. ■ .──┘
                                Escriba su respuesta:""");
                        respuesta = S.nextLine();
                    } while (!respuesta.equalsIgnoreCase("s") && !respuesta.equalsIgnoreCase("n"));
                    if (controller.changeInvitedMode(respuesta)) {
                        System.out.println("""
                                ┌──. ■ .─────────────────────────┐
                                 Modo invitado cambiado con éxito
                                └─────────────────────────. ■ .──┘""");
                        System.out.println("""
                                ┌──. ■ .───────────────────────────────────────────────────────┐
                                 Una vez modificado, para cambiar el modo deberá dirigirse a la
                                       ruta del archivo para cambiar nuevamente el modo
                                └───────────────────────────────────────────────────────. ■ .──┘""");
                    } else System.out.println("""
                            ┌──. ■ .─────────────────────────┐
                                Error al realizar el cambio
                            └─────────────────────────. ■ .──┘""");
                    break;
                case 3:
                    System.out.println("No se ha activado el modo invitado");
                    break;
            }
        } else System.out.println("█ NO HAY INFORMACIÓN PARA MOSTRAR █");
    }

    private static void listShipmentEmail(AppController controller, Admin admin) {
        if (controller.sendExcel(admin)) System.out.println("El listado ha sido enviado con éxito");
        else System.out.println("Fallo al enviar el listado");
    }

    private static void addAdmin(AppController controller) {
        System.out.print("Introduce el email del nuevo administrador: ");
        String emailAdmin = S.nextLine();
        if (controller.searchDriverByEmail(emailAdmin) != null && controller.searchUserByEmail(emailAdmin) != null &&
            controller.searchAdminByEmail(emailAdmin) != null)
            System.out.println("""
                    ┌──. ■ .─────────────────────────┐
                     El email introducido ya existe
                    └─────────────────────────. ■ .──┘""");
        else {
            System.out.print("Introduce el nombre del administrador: ");
            String nameAdmin = S.nextLine();
            System.out.print("Introduce la contraseña para el administrador: ");
            String pass = S.nextLine();
            if (controller.addAdmin(nameAdmin, emailAdmin, pass)) {
                System.out.println("""
                        ┌──. ■ .───────────────────────────────┐
                          Administrador agregado correctamente
                        └───────────────────────────────. ■ .──┘""");
            } else System.out.println("Ha ocurrido un error");
        }
    }

    /*private static void saveCopySecurity(AppController controller) {
        //He puesto una ruta predeterminada para que, en el caso de que no quiera escribir una ruta
        // tenga esta de preferencia
        String ruta = "\\src\\main\\java\\copy\\backup.copy";
        try {
            System.out.print("""
                    ┌──. ■ .────────────────────────────────────────────────────────────┐
                    █             Bienvenido a la opción de copias de seguridad         █
                    └────────────────────────────────────────────────────────────. ■ .──┘          
                            █ ¿En qué ruta desea guardar la copia de seguridad?
                                █ 1. Ruta predeterminada
                                █ 2. Escribir ruta preferida
                              ---------------------------------------------
                             (En caso de pulsar cualquier numero se elegirá
                              la primera opción automáticamente)
                              ---------------------------------------------
                            █ Elige una opción:""");
            switch (Integer.parseInt(S.nextLine())) {
                case 2:
                    System.out.println("""
                            ┌──. ■ .──────────────────────────────────────────────────────────────┐
                                   (Para escribir la ruta correctamente deberá seguir la
                                       sucesión de carpetas hasta llegar a la indicada)
                                       ***********************************************
                                              UTILICE SIEMPRE UNA RUTA ABSOLUTA
                                       ***********************************************
                              Ejemplo: C:\\java\\PracticaT6\\src\\main\\*NOMBRE DEL ARCHIVO*.copy)
                                       
                            └──────────────────────────────────────────────────────────────. ■ .──┘
                                █ Escriba aquí su ruta: """);
                    ruta = S.nextLine();
                    if (PersistenceDisk.backup(controller, ruta)) System.out.println("""
                            ┌──. ■ .────────────────────┐
                              Copia creada correctamente
                            └────────────────────. ■ .──┘""");
                    else System.out.println("""
                            ┌──. ■ .────────────────────────────────────────────────────┐
                                Error al crear la copia, compruebe la ruta de guardado
                            └────────────────────────────────────────────────────. ■ .──┘""");
                    break;
                default:
                    if (PersistenceDisk.backup(controller, ruta)) System.out.println("""
                            ┌──. ■ .────────────────────┐
                              Copia creada correctamente
                            └────────────────────. ■ .──┘""");
                    else System.out.println("""
                            ┌──. ■ .───────────────────┐
                               Error al crear la copia
                            └───────────────────. ■ .──┘""");
                    break;
            }
        } catch (NumberFormatException e) {
            System.out.println("""
                    ┌──. ■ .─────────────────────────────┐
                      Error. Debes introducir un número
                    └─────────────────────────────. ■ .──┘""");
        }

    }*/

    public static void modifyProfileAdmin(AppController controller, Admin admin) {
        int op = 0;
        do {
            System.out.print("""
                    ┌──. ■ .─────────────────────────────────────────────────────────┐
                    █              ¿Qué desea modificar de su perfil?                █
                    █ Menú de operaciones:                                           █
                    █    1. Nombre.                                                  █
                    █    2. Contraseña.                                              █
                    █    3. Email.                                                   █
                    █    4. Salir.                                                   █
                    └─────────────────────────────────────────────────────────. ■ .──┘
                    Elija una opción:""");
            try {
                op = Integer.parseInt(S.nextLine());
                switch (op) {
                    case 1:
                        changeName(admin, controller);
                        break;
                    case 2:
                        changePass(admin, controller);
                        break;
                    case 3:
                        changeEmail(admin, controller);
                        break;
                    case 4:
                        Utils.exitOption();
                        break;
                    default:
                        break;
                }
            } catch (NumberFormatException e) {
                System.out.println("""
                        ┌──. ■ .─────────────────────────────┐
                          Error. Debes introducir un número
                        └─────────────────────────────. ■ .──┘""");
            }

        } while (op != 4);
    }

    private static void changeEmail(Object user, AppController controller) {
        boolean made = false;
        System.out.print("Introduce tu actual contraseña: ");
        if (user instanceof User) {
            if (((User) user).checkPass(S.nextLine())) {
                System.out.print("Introduce tu nuevo email: ");
                ((User) user).setEmail(S.nextLine());
                made = controller.updateUser(user);
            }
        }
        if (user instanceof Driver) {
            if (((Driver) user).checkPass(S.nextLine())) {
                System.out.print("Introduce tu nuevo email: ");
                ((Driver) user).setEmail(S.nextLine());
                made = controller.updateUser(user);
            }
        }
        if (user instanceof Admin) {
            if (((Admin) user).checkPass(S.nextLine())) {
                System.out.print("Introduce tu nuevo email: ");
                ((Admin) user).setEmail(S.nextLine());
                made = controller.updateUser(user);
            }
        }
        if (made) {
            System.out.println("""
                    ┌──. ■ .──────────────────────┐
                      Email cambiado correctamente
                    └──────────────────────. ■ .──┘""");
        } else System.out.println("""
                ┌──. ■ .────────────────────┐
                  Error. Vuelve a intentarlo
                └────────────────────. ■ .──┘""");
    }

    private static void changePass(Object user, AppController controller) {
        boolean made = false;
        System.out.print("Introduce tu actual contraseña: ");
        if (user instanceof User) {
            if (((User) user).checkPass(S.nextLine())) {
                System.out.print("Introduce tu nueva contraseña: ");
                ((User) user).setPass(S.nextLine());
                made = controller.updateUser(user);
            }
        }
        if (user instanceof Driver) {
            if (((Driver) user).checkPass(S.nextLine())) {
                System.out.print("Introduce tu nueva contraseña: ");
                ((Driver) user).setPass(S.nextLine());
                made = controller.updateUser(user);
            }
        }
        if (user instanceof Admin) {
            if (((Admin) user).checkPass(S.nextLine())) {
                System.out.print("Introduce tu nueva contraseña: ");
                ((Admin) user).setPass(S.nextLine());
                made = controller.updateUser(user);
            }
        }
        if (made) {
            System.out.println("""
                    ┌──. ■ .──────────────────────────┐
                     Contraseña cambiada correctamente
                    └──────────────────────────. ■ .──┘""");
        } else System.out.println("""
                ┌──. ■ .────────────────────┐
                  Error. Vuelve a intentarlo
                └────────────────────. ■ .──┘""");

    }

    private static void changeName(Object user, AppController controller) {
        boolean made = false;
        System.out.print("Introduce tu nuevo nombre: ");
        if (user instanceof User) {
            ((User) user).setName(S.nextLine());
            made = controller.updateUser(user);
        }
        if (user instanceof Driver) {
            ((Driver) user).setName(S.nextLine());
            made = controller.updateUser(user);
        }
        if (user instanceof Admin) {
            ((Admin) user).setName(S.nextLine());
            made = controller.updateUser(user);
        }
        if (made) {
            System.out.println("""
                    ┌──. ■ .──────────────────────┐
                     Nombre cambiado correctamente
                    └──────────────────────. ■ .──┘""");
        } else System.out.println("""
                ┌──. ■ .───────────────────────┐
                 Error al actualizar los datos
                └───────────────────────. ■ .──┘""");
    }

    private static void resumeDriversRegister(AppController controller) {
        int cont = 0;
        ArrayList<Driver> drivers = controller.getDrivers();
        if (drivers.isEmpty()) System.out.println("""
                ┌──. ■ .────────────────────────┐
                  No hay conductores registrados
                └────────────────────────. ■ .──┘""");
        else {
            for (Driver d :
                    drivers) {
                if (d != null) {
                    cont++;
                    System.out.println("Conductor nº: " + cont + "\n" + d.resumeForAdmin());
                }
            }
        }
    }

    public static void resumeUserRegister(AppController controller) {
        int cont = 0, op = 0, opTwo;
        User userFind;
        ArrayList<User> users = controller.getUsers();
        if (users.isEmpty()) System.out.println("""
                ┌──. ■ .─────────────────────┐
                 No hay usuarios registrados
                └─────────────────────. ■ .──┘""");
        else {
            for (User u :
                    users) {
                if (u != null) {
                    cont++;
                    System.out.println("Usuario nº: " + cont + "\n" + u.resumeForAdmin());
                }
            }
        }
        do {
            try {
                do {
                    System.out.print("""
                            ┌──. ■ .──────────────────────────────┐
                             ¿Desea buscar un usuario en concreto?
                            └──────────────────────────────. ■ .──┘
                              █ 1. Si
                              █ 2. No
                            Elija una opción:""");
                    op = Integer.parseInt(S.nextLine());
                    if (op < 0) System.out.println("""                             
                            ┌──. ■ .───────────────────────────┐
                             Debes introducir una opción valida
                            └───────────────────────────. ■ .──┘""");
                } while (!(op > 0));
                switch (op) {
                    case 1:
                        do {
                            do {
                                System.out.print("""
                                        ┌──. ■ .────────────────────────┐
                                         ¿De qué manera desea buscarlo?
                                        └────────────────────────. ■ .──┘
                                          █ 1. Según ID de un envío.
                                          █ 2. Email
                                          █ 3. Número de teléfono
                                          █ 4. Salir.
                                        Elije una opción: """);
                                opTwo = Integer.parseInt(S.nextLine());
                            } while (!(opTwo > 0));
                            switch (opTwo) {
                                case 1:
                                    System.out.print("Introduce el número de referencia del envío: ");
                                    userFind = controller.searchUserByIdShipment(Integer.parseInt(S.nextLine()));
                                    if (userFind == null)
                                        System.out.println("""
                                                ┌──. ■ .───────────────────────────────────────────────────┐
                                                 No se ha encontrado ningún usuario que contenga este envío
                                                └───────────────────────────────────────────────────. ■ .──┘""");
                                    else System.out.println(userFind.resumeForAdmin());
                                    break;
                                case 2:
                                    System.out.print("Introduce un email: ");
                                    userFind = controller.searchUserByEmail(S.nextLine());
                                    if (userFind == null)
                                        System.out.println("""
                                                ┌──. ■ .────────────────────────────────────────────┐
                                                  No se ha encontrado ningún usuario con este email
                                                └────────────────────────────────────────────. ■ .──┘""");
                                    else System.out.println(userFind.resumeForAdmin());
                                    break;
                                case 3:
                                    System.out.print("Introduce un número de teléfono: ");
                                    userFind = controller.searchUserByPhone(Integer.parseInt(S.nextLine()));
                                    if (userFind == null)
                                        System.out.println("""
                                                ┌──. ■ .─────────────────────────────────────────────┐
                                                 No se ha encontrado ningún usuario con este teléfono
                                                └─────────────────────────────────────────────. ■ .──┘""");
                                    else System.out.println(userFind.resumeForAdmin());
                                    break;
                                default:
                                    break;
                            }
                        } while (opTwo != 4);
                        break;
                    default:
                        Utils.exitOption();
                        break;
                }
            } catch (NumberFormatException e) {
                System.out.println("""
                        ┌──. ■ .─────────────────────────────┐
                          Error. Debes introducir un número
                        └─────────────────────────────. ■ .──┘""");
            }
        } while (op != 2);
    }

    public static void assignedShipmentADriver(AppController controller) {

        ArrayList<InfoShipmentDataClass> shipmentUnassigned = controller.getShipmentsUnassigned();
        if (shipmentUnassigned.isEmpty()) System.out.println("""
                ┌──. ■ .─────────────────────┐
                  No hay envíos para mostrar
                └─────────────────────. ■ .──┘""");
        else {
            System.out.println("""
                    ┌──. ■ .─────────────────────────────────────┐
                     Los envíos se muestran desde el más antiguo,
                                 al más reciente.
                    └─────────────────────────────────────. ■ .──┘""");
            System.out.println("""
                    ┌──. ■ .─────────────────────┐
                      ¿Qué envío desea asignar?
                    └─────────────────────. ■ .──┘""");
            for (InfoShipmentDataClass s :
                    shipmentUnassigned) {
                if (s != null) System.out.println(s.forAdminUnassigned());
            }
            System.out.println("""
                    ┌──. ■ .───────────────────────────────────────────────┐
                     Se ruega que se asignen primero los envíos más antiguos
                    └───────────────────────────────────────────────. ■ .──┘
                        """);
            System.out.print("Introduce el número de referencia del envio a asignar: ");
            try {
                int idShipment = Integer.parseInt(S.nextLine());
                Shipment shipmentFind = controller.searchShipmentById(idShipment);
                if (shipmentFind == null) System.out.println("Error, no se ha encontrado el envío a asignar");
                else {
                    ArrayList<Driver> drivers = controller.getDrivers();
                    System.out.println("¿A qué conductor deseas asignarlo?");
                    if (drivers.isEmpty()) System.out.println("No hay conductores disponibles");
                    else {
                        for (Driver d :
                                drivers) {
                            if (d != null) System.out.println(d.resumeForAdmin());
                        }
                        System.out.print("Introduce el número de referencia del conductor a seleccionar: ");
                        int idDriver = Integer.parseInt(S.nextLine());
                        if (controller.addShipmentDriver(idShipment, idDriver))
                            System.out.println("""
                                    ┌──. ■ .────────────────────────┐
                                      Envío asignado correctamente
                                    └────────────────────────. ■ .──┘""");
                        else System.out.println("""
                                ┌──. ■ .──────────────────────┐
                                  Error. Vuelve a intentarlo
                                └──────────────────────. ■ .──┘""");
                    }
                }
            } catch (NumberFormatException e) {
                System.out.println("""
                        ┌──. ■ .─────────────────────────────┐
                          Error. Debes introducir un número
                        └─────────────────────────────. ■ .──┘""");
            }
        }

    }

    public static void drawShippingsUnassigned(AppController controller) {
        ArrayList<InfoShipmentDataClass> results = controller.getShipmentsUnassigned();
        if (results.isEmpty()) System.out.println("""
                ┌──. ■ .──────────────────────┐
                   No hay envíos para mostrar
                └──────────────────────. ■ .──┘""");
        else {
            Collections.sort(results);
            System.out.println("""
                                        
                    ┌──. ■ .─────────────────────────────────────┐
                     Los envíos se muestran desde el más antiguo,
                                al más nuevo creado.
                    └─────────────────────────────────────. ■ .──┘""");
            for (InfoShipmentDataClass s :
                    results) {
                if (s != null) {
                    System.out.println(s.forAdminUnassigned());
                }
            }
        }
    }

    public static void addDriver(AppController controller) {
        System.out.print("Introduce el email del conductor: ");
        String emailDriver = S.nextLine();
        if (controller.searchDriverByEmail(emailDriver) != null && controller.searchUserByEmail(emailDriver) != null)
            System.out.println("""
                    ┌──. ■ .─────────────────────────┐
                     El email introducido ya existe
                    └─────────────────────────. ■ .──┘""");
        else {
            System.out.print("Introduce el nombre del conductor: ");
            String nameDriver = S.nextLine();
            System.out.print("Introduce la contraseña para el conductor: ");
            String pass = S.nextLine();

            if (controller.addDriver(nameDriver, emailDriver, pass)) {
                System.out.println("""
                        ┌──. ■ .─────────────────────────┐
                         Conductor agregado correctamente
                        └─────────────────────────. ■ .──┘""");
                System.out.println("""
                        ┌──. ■ .───────────────────────────────────────────────────┐
                         Actualmente este conductor no dispone de zonas de entrega,
                               deberá iniciar sesión y añadirlas manualmente.
                        └───────────────────────────────────────────────────. ■ .──┘""");
            } else System.out.println("Ha ocurrido un error");
        }
    }
}
