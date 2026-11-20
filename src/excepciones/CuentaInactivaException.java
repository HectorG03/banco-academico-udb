package excepciones;

public class CuentaInactivaException extends BancoException{
    
    public CuentaInactivaException(String mensaje) {
        super(mensaje);
    }
        
}
