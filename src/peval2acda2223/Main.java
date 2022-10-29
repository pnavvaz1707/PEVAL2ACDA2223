package peval2acda2223;

import java.io.*;
import java.sql.*;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Main {
    private static final String[] MENU_OPCIONES = new String[]{
            "Actualizar la tabla jugadores mediante el fichero \"fichajes.txt\"",
            "Insertar un partido mediante teclado",
            "Mostrar nombre, altura, peso, posición y equipo de todos los jugadores de los equipos de " + "una ciudad introducida por teclado",
            "Visualizar el número de partidos jugados en cada temporada por un jugador, diferenciando entre local y visitante",
            "Actualizar la posición de PIVOT a PIVOTE de los jugadores de la división Pacífica de la Conferencia Oeste",
            "Eliminar todos los datos de un equipo introduciendo el nombre del equipo por teclado",
            "Salir"}; // Opciones del menú

    private static final Scanner teclado = new Scanner(System.in); // Escáner para poder introducir datos por teclado durante el programa entero

    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.jdbc.Driver");

        BaseDeDatos bd = new BaseDeDatos("jdbc:mysql://localhost:3306/" + Datos.DB_NAME, Datos.DB_USER, Datos.DB_PASSWORD);

        boolean sigue = true;
        while (sigue) {
            crearMenu();
            System.out.println("Introduce un número");
            String respuesta = teclado.next();
            teclado.nextLine(); // Esta instrucción es para evitar que se salte alguna línea al leer por teclado en las opciones

            switch (respuesta) {
                case "1" -> {
                    imprimirOpcion(Integer.parseInt(respuesta));
                    try {
                        File f = new File(Datos.ARCHIVO_ACTUALIZAR);

                        bd.actualizarJugadores(f);
                    } catch (IOException e) {
                        Colores.imprimirRojo("El archivo no existe");
                    }
                    sigue = continuarMenu();
                }
                case "2" -> {
                    imprimirOpcion(Integer.parseInt(respuesta));
                    try {
                        int codigo, puntosLocal, puntosVisitantes;
                        String equipoLocal, equipoVisitante, temporada;

                        codigo = solicitarEntero("Introduce el código");

                        teclado.nextLine(); // Esta instrucción es para evitar que el programa se salte la siguiente línea

                        System.out.println("Introduce el equipo local");
                        equipoLocal = teclado.nextLine();

                        System.out.println("Introduce el equipo visitante");
                        equipoVisitante = teclado.nextLine();

                        puntosLocal = solicitarEntero("Introduce los puntos del local");

                        puntosVisitantes = solicitarEntero("Introduce los puntos del visitante");

                        System.out.println("Introduce la temporada");
                        temporada = teclado.next();

                        bd.insertarPartido(codigo, equipoLocal, equipoVisitante, puntosLocal, puntosVisitantes, temporada);

                    } catch (Exception e) {
                        Colores.imprimirRojo(e.getMessage());
                    }
                    sigue = continuarMenu();
                }
                case "3" -> {
                    imprimirOpcion(Integer.parseInt(respuesta));
                    try {
                        System.out.println("Introduce la ciudad");
                        String ciudad = teclado.nextLine();
                        bd.filtarPorCiudadDelEquipo(ciudad);
                    } catch (SQLException e) {
                        Colores.imprimirRojo("Ha habido un error al filtrar por ciudad " + e.getMessage());
                    }
                    sigue = continuarMenu();
                }
                case "4" -> {
                    imprimirOpcion(Integer.parseInt(respuesta));
                    try {
                        System.out.println("Introduce el nombre del jugador");
                        String jugador = teclado.nextLine();
                        bd.verNumPartidos(jugador);
                    } catch (SQLException e) {
                        Colores.imprimirRojo("Ha habido un error al ver el número de partidos del jugador (El jugador puede estar duplicado)");
                    }
                    sigue = continuarMenu();
                }
                case "5" -> {
                    imprimirOpcion(Integer.parseInt(respuesta));
                    try {
                        bd.actualizarAPivote();
                    } catch (SQLException e) {
                        Colores.imprimirRojo("Ha habido un error al ver el actualizar la posición pivot a pivote");
                    }
                    sigue = continuarMenu();
                }
                case "6" -> {
                    imprimirOpcion(Integer.parseInt(respuesta));
                    System.out.println("Introduce el nombre del equipo que quieras eliminar");
                    String equipo = teclado.nextLine();
                    try {
                        bd.borrarEquipo(equipo);
                    } catch (SQLException e) {
                        Colores.imprimirRojo("Ha habido un error al eliminar los registros relacionados con el equipo indicado");
                    }
                    sigue = continuarMenu();
                }
                case "0" -> {
                    Colores.imprimirVerde("Has salido del programa con éxito");
                    sigue = false;
                }
                default -> Colores.imprimirRojo("Debes introducir el número asociado a una de las opciones presentadas");
            }
        }
    }

    /**
     * Método para preguntar al usuario si desea continuar el programa
     *
     * @return (Booleano de valor true si quiere seguir con el programa o false si quiere terminar el programa)
     */
    private static boolean continuarMenu() {
        boolean continuar = true;
        boolean sigue = true;
        while (sigue) {
            System.out.println("¿Deseas seguir con el programa?");
            String respuesta = teclado.next();
            if (respuesta.equalsIgnoreCase("si") || respuesta.equalsIgnoreCase("sí")) {
                sigue = false;
            } else if (respuesta.equalsIgnoreCase("no")) {
                Colores.imprimirVerde("Has salido del programa con éxito");
                continuar = false;
                sigue = false;
            } else {
                Colores.imprimirRojo("Por favor, responda sí o no");
            }
        }
        return continuar;
    }

    /**
     * Método para imprimir la opción seleccionada de color azul
     *
     * @param eleccion (Número que indica la posición de la opción en el array estático MENU_OPCIONES)
     */
    private static void imprimirOpcion(int eleccion) {
        Colores.imprimirAzul("Has seleccionado la opción: " + MENU_OPCIONES[eleccion - 1]);
    }

    /**
     * Método para crear el menú
     */
    private static void crearMenu() {
        for (int i = 0; i < MENU_OPCIONES.length - 1; i++) {
            Colores.imprimirAzul((i + 1) + ". " + MENU_OPCIONES[i]);
        }
        Colores.imprimirRojo("0. " + MENU_OPCIONES[MENU_OPCIONES.length - 1]);
    }

    /**
     * Método para pedir un dato de tipo entero constantemente hasta que no se equivoque de tipo
     *
     * @param msg (Mensaje que se le envía al usuario para pedirle que introduzca el dato)
     * @return (El número entero solicitado)
     */
    private static int solicitarEntero(String msg) {
        int num = 0;
        boolean sigue = true;

        while (sigue) {
            try {
                System.out.println(msg);
                num = teclado.nextInt();
                sigue = false;
            } catch (InputMismatchException e) {
                Colores.imprimirRojo("Debes introducir un número entero");
                teclado.nextLine(); // Sin esta instrucción el programa no leería el siguiente número y entraría en bucle infinito
            }
        }
        return num;
    }
}
