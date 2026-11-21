package excepciones;

public class SaldoInsuficienteException extends BancoException {
    
    public SaldoInsuficienteException(String mensaje) {
        super(mensaje);
    }
    
}
