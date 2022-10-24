package peval2acda2223;

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;

import java.io.*;
import java.sql.*;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    private static final String[] MENU_OPCIONES = new String[]{
            "Actualizar la tabla jugadores mediante el fichero \"fichajes.txt\"",
            "Insertar un partido mediante teclado",
            "Mostrar nombre, altura, peso, posición y equipo de todos los jugadores de los equipos de " + "una ciudad introducida por teclado",
            "Visualizar el número de partidos jugados en cada temporada por un jugador, diferenciando entre local y visitante",
            "Actualizar la posición de PIVOT a PIVOTE de los jugadores de la división Pacífica de la Conferencia Oeste",
            "Eliminar todos los datos de un equipo introduciendo el nombre del equipo por teclado",
            "Salir"};

    static Scanner teclado = new Scanner(System.in);

    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.jdbc.Driver");

        Connection conexion = DriverManager.getConnection("jdbc:mysql://localhost:3306/basketlite", "root", "");
        Statement sentencia = conexion.createStatement();

        StringBuilder sql;
        ResultSet rs;

        boolean sigue = true;
        while (sigue) {
            crearMenu();
            System.out.println("Introduce un número");
            String respuesta = teclado.next();

            switch (respuesta) {
                case "1":
                    imprimirOpcion(Integer.parseInt(respuesta));

                    try {
                        File f = new File("src/peval2acda2223/resources/fichajes.txt");

                        FileReader fr = new FileReader(f);
                        BufferedReader br = new BufferedReader(fr);

                        String linea;
                        br.readLine();
                        int codigo = 0;

                        sql = new StringBuilder("SELECT MAX(CODIGO) FROM JUGADORES");
                        rs = sentencia.executeQuery(sql.toString());

                        while (rs.next()) {
                            codigo = rs.getInt(1) + 1;
                        }

                        while ((linea = br.readLine()) != null) {
                            try {
                                sql = new StringBuilder();
                                String[] datos = linea.split(";");
                                sql.append("INSERT INTO JUGADORES VALUES (").append(codigo).append(",'");
                                sql.append(datos[0]).append("','").append(datos[1]).append("','");
                                sql.append(datos[2]).append("','").append(datos[3]).append("','");
                                sql.append(datos[4]).append("','").append(datos[5]).append("')");
                                if (!(sentencia.execute(sql.toString()))) {
                                    Colores.imprimirVerde("Se ha ejecutado exitosamente la siguiente instrucción " + sql);
                                }
                                codigo++;
                            } catch (MySQLIntegrityConstraintViolationException e) {
                                if (e.getMessage().contains("equipo")) {
                                    Colores.imprimirRojo("El equipo introducido no existe");
                                } else {
                                    Colores.imprimirRojo(e.getMessage());
                                }
                            }
                        }
                    } catch (IOException e) {
                        Colores.imprimirRojo("El archivo \"fichajes.txt\" no existe");
                    }

                    sigue = continuarMenu();
                    break;

                case "2":
                    imprimirOpcion(Integer.parseInt(respuesta));

                    try {

                        int codigo = solicitarEntero("Introduce el código");

                        System.out.println("Introduce el equipo local");
                        String equipoLocal = teclado.next();

                        System.out.println("Introduce el equipo visitante");
                        String equipoVisitante = teclado.next();

                        int puntosLocal = solicitarEntero("Introduce los puntos del local");

                        int puntosVisitantes = solicitarEntero("Introduce los puntos del visitante");

                        System.out.println("Introduce la temporada");
                        String temporada = teclado.next();

                        comprobacionErrores(conexion, codigo, equipoLocal, equipoVisitante, puntosLocal, puntosVisitantes, temporada);

                        sql = new StringBuilder();
                        sql.append("INSERT INTO PARTIDOS VALUES (");
                        sql.append(codigo).append(",'").append(equipoLocal).append("','");
                        sql.append(equipoVisitante).append("',").append(puntosLocal);
                        sql.append(",").append(puntosVisitantes).append(",'");
                        sql.append(temporada).append("')");

                        if (!sentencia.execute(sql.toString())) {
                            Colores.imprimirVerde("Registro insertado");
                        }

                    } catch (Exception e) {
                        Colores.imprimirRojo(e.getMessage());
                    }

                    sigue = continuarMenu();
                    break;

                case "3":
                    imprimirOpcion(Integer.parseInt(respuesta));

                    System.out.println("Introduce la ciudad");
                    String ciudad = teclado.next();

                    sql = new StringBuilder("SELECT NOMBRE,ALTURA,PESO,POSICION,NOMBRE_EQUIPO FROM JUGADORES WHERE PROCEDENCIA = '" + ciudad + "'");

                    rs = sentencia.executeQuery(sql.toString());
                    while (rs.next()) {
                        System.out.println("Nombre: " + rs.getString(1));
                        System.out.println("Altura: " + rs.getString(2));
                        System.out.println("Peso: " + rs.getInt(3));
                        System.out.println("Posición: " + rs.getString(4));
                        System.out.println("Equipo: " + rs.getString(5));
                        Colores.imprimirAzul("///////////////////////////////////////////");
                    }

                    sigue = continuarMenu();
                    break;

                case "4":
                    imprimirOpcion(Integer.parseInt(respuesta));

                    teclado.nextLine();
                    System.out.println("Introduce el nombre del jugador");
                    String jugador = teclado.nextLine();

                    sql = new StringBuilder("SELECT TEMPORADA, COUNT(*) FROM PARTIDOS WHERE EQUIPO_LOCAL = (SELECT NOMBRE_EQUIPO FROM JUGADORES WHERE NOMBRE = '" + jugador + "') GROUP BY TEMPORADA");
                    rs = sentencia.executeQuery(sql.toString());
                    while (rs.next()) {
                        System.out.println("Temporada: " + rs.getString(1));
                        System.out.println("Partidos locales: " + rs.getInt(2));
                    }

                    sql = new StringBuilder("SELECT TEMPORADA, COUNT(*) FROM PARTIDOS WHERE EQUIPO_VISITANTE = (SELECT NOMBRE_EQUIPO FROM JUGADORES WHERE NOMBRE = '" + jugador + "') GROUP BY TEMPORADA");
                    rs = sentencia.executeQuery(sql.toString());
                    while (rs.next()) {
                        System.out.println("Temporada: " + rs.getString(1));
                        System.out.println("Partidos visitantes: " + rs.getInt(2));
                    }

                    sigue = continuarMenu();
                    break;

                case "5":
                    imprimirOpcion(Integer.parseInt(respuesta));

                    sql = new StringBuilder("UPDATE JUGADORES SET POSICION = 'PIVOTE' WHERE POSICION = 'PIVOT' AND NOMBRE IN (" +
                            "SELECT NOMBRE FROM JUGADORES WHERE NOMBRE_EQUIPO IN (" +
                            "SELECT NOMBRE FROM EQUIPOS WHERE DIVISION ='PACIFIC' AND CONFERENCIA ='WEST'))");

                    sentencia.execute(sql.toString());
                    Colores.imprimirVerde("Se ha modificado " + sentencia.getUpdateCount() + " registro(s)");

                    sigue = continuarMenu();
                    break;

                case "6":
                    imprimirOpcion(Integer.parseInt(respuesta));

                    System.out.println("Introduce el nombre del equipo que quieras eliminar");
                    String equipo = teclado.next();

                    sql = new StringBuilder("DELETE FROM EQUIPOS WHERE NOMBRE = '" + equipo + "'");

                    if (!sentencia.execute(sql.toString())) {
                        Colores.imprimirVerde("Eliminación completada");
                    }

                    sigue = continuarMenu();
                    break;

                case "0":
                    Colores.imprimirVerde("Has salido del programa con éxito");
                    sigue = false;
                    break;

                default:
                    Colores.imprimirRojo("Debes introducir el número asociado a una de las opciones presentadas");
                    break;
            }
        }
    }

    private static void comprobacionErrores(Connection conexion, int codigo, String equipoLocal, String equipoVisitante, int puntosLocal, int puntosVisitantes, String temporada) throws Exception {

        if (equipoLocal.equalsIgnoreCase(equipoVisitante)) {
            throw new Exception("Debes introducir dos equipos distintos");
        }

        if (puntosLocal < 0 || puntosVisitantes < 0) {
            throw new Exception("Los puntos no pueden ser menores que 0");
        }

        if (codigo <= 0) {
            throw new Exception("El código debe ser mayor que 0");
        }

        Pattern pattern = Pattern.compile("\\d\\d/\\d\\d");
        Matcher matcher = pattern.matcher(temporada);

        if (!matcher.matches()) {
            throw new Exception("La temporada debe cumplir el siguiente formato (YY/YY) donde 'Y' equivale al año");
        }

        String sqlComprobacion = "SELECT CODIGO FROM JUGADORES WHERE CODIGO = " + codigo;
        Statement sentenciaComprobacion = conexion.createStatement();

        if (sentenciaComprobacion.executeQuery(sqlComprobacion).next()) {
            throw new Exception("El código introducido ya existe");
        }

        sqlComprobacion = "SELECT NOMBRE FROM EQUIPOS WHERE NOMBRE = '" + equipoLocal + "'";
        if (!sentenciaComprobacion.executeQuery(sqlComprobacion).next()) {
            throw new Exception("El equipo local no existe");
        }

        sqlComprobacion = "SELECT NOMBRE FROM EQUIPOS WHERE NOMBRE = '" + equipoVisitante + "'";
        if (!sentenciaComprobacion.executeQuery(sqlComprobacion).next()) {
            throw new Exception("El equipo visitante no existe");
        }
    }

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

    private static void imprimirOpcion(int eleccion) {
        Colores.imprimirAzul("Has seleccionado la opción: " + MENU_OPCIONES[eleccion - 1]);
    }

    private static void crearMenu() {
        for (int i = 0; i < MENU_OPCIONES.length - 1; i++) {
            Colores.imprimirAzul((i + 1) + ". " + MENU_OPCIONES[i]);
        }
        Colores.imprimirRojo("0. " + MENU_OPCIONES[MENU_OPCIONES.length - 1]);
    }

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
            }
        }
        return num;
    }

}
