package excepciones;

public class BancoException extends Exception {
    
    public BancoException(String mensaje){
        super(mensaje);
    }
    
    public BancoException(String mensaje, Throwable causa) {
        super (mensaje, causa);
    }
}
