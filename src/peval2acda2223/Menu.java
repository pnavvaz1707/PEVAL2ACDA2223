package peval2acda2223;

public class Menu {
    //Variable que almacena todas las opciones del menú
    public static final String[] MENU_OPCIONES = new String[]{
            "Buscar el archivo \"Companies.txt\" y actualizarlo a investors (Opción 1 y 2)",
            "Buscar el archivo \"Companies.txt\" y crear archivo \"investor.obj\" (Opción 1 y 3)",
            "Crear el archivo \"Companies.xml\" a partir de archivo \"investor.obj\" (Opción 4)",
            "Leer el fichero xml \"Companies.xml\" (Opción 5)",
            "Salir"};

    /**
     * Método que imprime todas las opciones del menú y pone la última con un 0
     */
    public static void crearMenu() {
        for (int i = 0; i < MENU_OPCIONES.length - 1; i++) {
            System.out.println(i + 1 + ". " + MENU_OPCIONES[i]);
        }
        System.out.println("0. " + MENU_OPCIONES[MENU_OPCIONES.length - 1]);
    }

}
