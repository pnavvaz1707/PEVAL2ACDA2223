package peval2acda2223;

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;

import java.io.*;
import java.sql.*;
import java.util.Scanner;

public class Main {
    private static final String[] MENU_OPCIONES = new String[]{
            "Actualizar la tabla jugadores mediante el fichero \"fichajes.txt\"",
            "Insertar un partido mediante teclado",
            "Mostrar nombre, altura, peso, posición y equipo de todos los jugadores de los equipos de " + "una ciudad introducida por teclado",
            "Visualizar el número de partidos jugados en cada temporada por un jugador, diferenciando entre local y visitante",
            "Actualizar la posición de PIVOT a PIVOTE de los jugadores de la división Pacífica de la Conferencia Oeste",
            "Eliminar todos los datos de un equipo introduciendo el nombre del equipo por teclado",
            "Salir"};

    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        Scanner teclado = new Scanner(System.in);
        Class.forName("com.mysql.jdbc.Driver");

        Connection conexion = DriverManager.getConnection("jdbc:mysql://localhost:3306/basketlitle", "root", "");
        Statement sentencia = conexion.createStatement();

        StringBuilder sql;

        boolean sigue = true;
        while (sigue){
            crearMenu();
        }

        File f = new File("src/peval2acda2223/resources/fichajes.txt");
        try {
            FileReader fr = new FileReader(f);
            BufferedReader br = new BufferedReader(fr);
            String linea;
            br.readLine();
            int codigo = 617;
            String obtenerCodigo = "SELECT MAX(CODIGO) FROM JUGADORES";
            ResultSet resultSet = sentencia.executeQuery(obtenerCodigo);
            while (resultSet.next()) {
                codigo = resultSet.getInt(1);
            }
            System.out.println();
            while ((linea = br.readLine()) != null) {
                try {
                    codigo++;
                    sql = new StringBuilder();
                    String[] datos = linea.split(";");
                    sql.append("INSERT INTO JUGADORES VALUES (").append(codigo).append(",'");
                    sql.append(datos[0]).append("','").append(datos[1]).append("','");
                    sql.append(datos[2]).append("','").append(datos[3]).append("','");
                    sql.append(datos[4]).append("','").append(datos[5]).append("')");
                    System.out.println(sql);
                    System.out.println(sentencia.execute(sql.toString()));
                } catch (MySQLIntegrityConstraintViolationException e) {
                    if (e.getMessage().contains("equipo")) {
                        System.err.println("El equipo introducido no existe");
                    } else {
                        System.err.println(e.getMessage());
                    }
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Introduce el código");
        int codigo = teclado.nextInt();
        System.out.println("Introduce el equipo local");
        String equipoLocal = teclado.next();
        System.out.println("Introduce el equipo visitante");
        String equipoVisitante = teclado.next();
        System.out.println("Introduce los puntos del local");
        int puntosLocal = teclado.nextInt();
        System.out.println("Introduce los puntos del visitante");
        int puntosVisitantes = teclado.nextInt();
        System.out.println("Introduce la temporada");
        String temporada = teclado.next();


        System.out.println("Introduce la ciudad");
        String ciudad = teclado.next();

        String sqlCiudad = "SELECT NOMBRE,ALTURA,PESO,POSICION,NOMBRE_EQUIPO FROM JUGADORES WHERE PROCEDENCIA = '" + ciudad + "'";

        ResultSet rs = sentencia.executeQuery(sqlCiudad);
        while (rs.next()) {
            System.out.println("Nombre: " + rs.getString(1));
            System.out.println("Altura: " + rs.getString(2));
            System.out.println("Peso: " + rs.getInt(3));
            System.out.println("Posición: " + rs.getString(4));
            System.out.println("Equipo: " + rs.getString(5));
            System.out.println("///////////////////////////////////////////");
        }

        teclado.nextLine();

        System.out.println("Introduce el jugador");
        String jugador = teclado.nextLine();
        System.out.println(jugador);
        String sqlPartidos = "SELECT COUNT(*) FROM PARTIDOS WHERE EQUIPO_LOCAL = (SELECT NOMBRE_EQUIPO FROM JUGADORES WHERE NOMBRE = '" + jugador + "')";
        rs = sentencia.executeQuery(sqlPartidos);
        while (rs.next()) {
            System.out.println("Partidos locales --> " + rs.getInt(1));
        }
        sqlPartidos = "SELECT COUNT(*) FROM PARTIDOS WHERE EQUIPO_VISITANTE = (SELECT NOMBRE_EQUIPO FROM JUGADORES WHERE NOMBRE = '" + jugador + "')";
        rs = sentencia.executeQuery(sqlPartidos);
        while (rs.next()) {
            System.out.println("Partidos visitantes --> " + rs.getInt(1));
        }

        String sqlDivision = "SELECT NOMBRE,NOMBRE_EQUIPO,POSICION FROM JUGADORES WHERE POSICION = 'PIVOT' AND NOMBRE IN (" +
                "SELECT NOMBRE FROM JUGADORES WHERE NOMBRE_EQUIPO IN (" +
                "SELECT NOMBRE FROM EQUIPOS WHERE DIVISION ='PACIFIC' AND CONFERENCIA ='WEST'))";
        rs = sentencia.executeQuery(sqlDivision);
        while (rs.next()) {
            System.out.println("Nombre --> " + rs.getString(1));
            System.out.println("Nombre equipo --> " + rs.getString(2));
            System.out.println("Posición --> " + rs.getString(3));
        }

        System.out.println("Introduce el nombre del equipo que quieras eliminar");
        String equipo = teclado.next();
        String sqlEliminarEquipo = "DELETE FROM EQUIPOS WHERE NOMBRE = '" + equipo + "'";
        System.out.println(sentencia.execute(sqlEliminarEquipo));

    }

    private static void crearMenu() {
        for (int i = 0; i < MENU_OPCIONES.length - 1; i++) {
            Colores.imprimirAzul((i + 1) + ". " + MENU_OPCIONES[i]);
        }
        Colores.imprimirRojo("0. " + MENU_OPCIONES[MENU_OPCIONES.length - 1]);
    }
}
