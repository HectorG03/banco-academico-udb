package excepciones;


public class CuentaNoEncontradaException extends BancoException {
    
    public CuentaNoEncontradaException(String mensaje) {
        super(mensaje);
    }
   
}
