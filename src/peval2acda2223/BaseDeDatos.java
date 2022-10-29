package peval2acda2223;

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;

import java.io.*;
import java.sql.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Clase para conectarse a una base de datos y realizar distintas instrucciones sql
 */
public class BaseDeDatos {

    Connection conexion; // Conexión con la base de datos
    Statement sentencia; // Statement que ejecutará la instrucción almacenada en el campo sql

    StringBuilder sql; // StringBuilder que se almacenará las instrucciones sql de cada método
    ResultSet rs; // ResultSet que se almacenará los resultados de las consultas sql

    /**
     * Constructor parametrizado de la clase
     * @param url (Url de la base de datos)
     * @param user (Usuario de la base de datos)
     * @param password (Contraseña de la base de datos)
     * @throws SQLException (Excepción que ocurre al conectarse a la base de datos)
     */
    public BaseDeDatos(String url, String user, String password) throws SQLException {
        this.conexion = DriverManager.getConnection(url, user, password);
        this.sentencia = this.conexion.createStatement();
    }

    /**
     * Método para actualizar jugadores mediante ficheros
     * @param f (Archivo del cual se van a sacar los datos para actualziar la tabla jugadores)
     * @throws IOException (Excepción que ocurre al haber no encontrar el archivo)
     */
    public void actualizarJugadores(File f) throws IOException {
        FileReader fr = new FileReader(f);
        BufferedReader br = new BufferedReader(fr);

        String linea;
        br.readLine();
        int codigo = 0;

        sql = new StringBuilder("SELECT MAX(CODIGO) FROM JUGADORES");
        try {

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
                        //Con el substring obtenemos el nombre del equipo para especificar al usuario cuál es el equipo que no existe
                        Colores.imprimirRojo("El equipo introducido no existe (" + sql.substring(sql.lastIndexOf(",") + 1, sql.length() - 1) + ")");
                    } else {
                        Colores.imprimirRojo(e.getMessage());
                    }
                }
            }
        } catch (SQLException e) {
            Colores.imprimirRojo("Ha habido un error con la instrucción SQL");
        }
    }

    /**
     * Método para ver los datos de todos los jugadores de un equipo que pertenezca a la ciudad enviada como parámetro
     * @param ciudad (Cadena con el nombre de la ciudad de la que se desea ver el equipo)
     * @throws SQLException (Excepción que ocurre al haber un fallo en la instrucción SQL)
     */
    public void filtarPorCiudadDelEquipo(String ciudad) throws SQLException {
        sql = new StringBuilder("SELECT J.NOMBRE,J.ALTURA,J.PESO,J.POSICION,J.NOMBRE_EQUIPO FROM JUGADORES J, EQUIPOS E WHERE J.NOMBRE_EQUIPO = E.NOMBRE AND E.CIUDAD = '" + ciudad + "'");

        rs = sentencia.executeQuery(sql.toString());
        while (rs.next()) {
            System.out.println("Nombre: " + rs.getString(1));
            System.out.println("Altura: " + rs.getString(2));
            System.out.println("Peso: " + rs.getInt(3));
            System.out.println("Posición: " + rs.getString(4));
            System.out.println("Equipo: " + rs.getString(5));
            Colores.imprimirAzul("///////////////////////////////////////////");
        }
    }

    /**
     * Método para obtener el número de partidos locales y visitantes del jugador enviado como parámetro en cada temporada
     * @param jugador (Nombre del jugador del que se desea ver el número de partidos locales y visitantes)
     * @throws SQLException (Excepción que ocurre al haber un fallo en la instrucción SQL)
     */
    public void verNumPartidos(String jugador) throws SQLException {
        sql = new StringBuilder("SELECT TEMPORADA, COUNT(*) FROM PARTIDOS WHERE EQUIPO_LOCAL = (SELECT NOMBRE_EQUIPO FROM JUGADORES WHERE NOMBRE = '" + jugador + "') GROUP BY TEMPORADA");
        rs = sentencia.executeQuery(sql.toString());
        while (rs.next()) {
            System.out.println("Temporada: " + rs.getString(1) + "\tPartidos locales: " + rs.getInt(2));
        }

        sql = new StringBuilder("SELECT TEMPORADA, COUNT(*) FROM PARTIDOS WHERE EQUIPO_VISITANTE = (SELECT NOMBRE_EQUIPO FROM JUGADORES WHERE NOMBRE = '" + jugador + "') GROUP BY TEMPORADA");
        rs = sentencia.executeQuery(sql.toString());
        while (rs.next()) {
            System.out.println("Temporada: " + rs.getString(1) + "\tPartidos visitantes: " + rs.getInt(2));
        }
    }

    /**
     * Método que actualiza la posición de PIVOT a PIVOTE a los jugadores de la división Pacífico de la Conferencia Oeste
     * @throws SQLException (Excepción que ocurre al haber un fallo en la instrucción SQL)
     */
    public void actualizarAPivote() throws SQLException {
        sql = new StringBuilder("UPDATE JUGADORES SET POSICION = 'PIVOTE' WHERE POSICION = 'PIVOT' AND NOMBRE IN (" +
                "SELECT NOMBRE FROM JUGADORES WHERE NOMBRE_EQUIPO IN (" +
                "SELECT NOMBRE FROM EQUIPOS WHERE DIVISION ='PACIFIC' AND CONFERENCIA ='WEST'))");

        sentencia.execute(sql.toString());
        Colores.imprimirVerde("Se ha modificado " + sentencia.getUpdateCount() + " registro(s)");
    }

    /**
     * Método para borrar el equipo pasado como parámetro y todos los campos asociados con él
     * @param equipo (Nombre del equipo que se desea borrar)
     * @throws SQLException (Excepción que ocurre al haber un fallo en la instrucción SQL)
     */
    public void borrarEquipo(String equipo) throws SQLException {
        sql = new StringBuilder("DELETE FROM EQUIPOS WHERE NOMBRE = '" + equipo + "'");

        if (!sentencia.execute(sql.toString())) {
            Colores.imprimirVerde("Se ha eliminado " + sentencia.getUpdateCount() + " registro(s)");
        }
    }

    /**
     * Método para insertar un partido con los datos enviados como parámetros
     * @param codigo (Código del partido)
     * @param equipoLocal (Equipo local del partido)
     * @param equipoVisitante (Equipo visitante del partido)
     * @param puntosLocal (Puntos del equipo local)
     * @param puntosVisitantes (Puntos del equipo visitante)
     * @param temporada (Temporada en la que se jugó el partido)
     */
    public void insertarPartido(int codigo, String equipoLocal, String equipoVisitante, int puntosLocal, int puntosVisitantes, String temporada) {
        try {
            comprobacionErrores(codigo, equipoLocal, equipoVisitante, puntosLocal, puntosVisitantes, temporada);

            sql = new StringBuilder();
            sql.append("INSERT INTO PARTIDOS VALUES (");
            sql.append(codigo).append(",'").append(equipoLocal).append("','");
            sql.append(equipoVisitante).append("',").append(puntosLocal);
            sql.append(",").append(puntosVisitantes).append(",'");
            sql.append(temporada).append("')");

            if (!sentencia.execute(sql.toString())) {
                Colores.imprimirVerde("Registro insertado");
            }
        } catch (SQLException e) {
            Colores.imprimirRojo("Error al insertar el partido");
        } catch (Exception e) {
            Colores.imprimirRojo(e.getMessage());
        }
    }

    /**
     * Método para comprobar que los datos introducidos por teclado para insertar un partido son válidos
     * @param codigo (Código del partido, que no se puede repetir y no puede ser menor o igual que 0)
     * @param equipoLocal (Equipo local del partido, debe existir)
     * @param equipoVisitante (Equipo visitante del partido, debe existir)
     * @param puntosLocal (Puntos del equipo local, no puede ser menor que 0)
     * @param puntosVisitantes (Puntos del equipo visitante, no puede ser menor que 0)
     * @param temporada (Temporada en la que se jugó el partido, debe cumplir el formato YY/YY)
     * @throws Exception (Excepción personalizada según el error que se produzca)
     */
    private void comprobacionErrores(int codigo, String equipoLocal, String equipoVisitante, int puntosLocal, int puntosVisitantes, String temporada) throws Exception {

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
}

