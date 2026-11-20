package servicio;

import dao.ClienteDAO;
import dao.CuentaDAO;
import excepciones.BancoException;
import excepciones.CuentaNoEncontradaException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import modelo.Cliente;
import modelo.Cuenta;
import modelo.EstadoCliente;
import modelo.EstadoCuenta;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CuentaServicio {
    
    private static final Logger logger = LogManager.getLogger(CuentaServicio.class);
    private final CuentaDAO cuentaDAO;
    private final ClienteDAO clienteDAO;
    
    public CuentaServicio(){
        this.cuentaDAO = new CuentaDAO();
        this.clienteDAO = new ClienteDAO();
    }
    
    // validaciones
    public String validar(Cuenta cuenta){
        if (cuenta == null){
            return "La cuenta no puede ser nula.";
        }

        if (cuenta.getSaldo() == null){
           return "El saldo inicial es obligatorio."; 
        }
        
        if (cuenta.getSaldo().compareTo(BigDecimal.ZERO) < 0){
            return "El saldo de la cuenta no puede ser negativo.";
        }
        
        if (cuenta.getCodigoCliente() <= 0) {
            return "Debe especificar un código de cliente válido asociado a la cuenta.";
        }
        
        if (cuenta.getTipoCuenta() == null) {
            return "El tipo de cuenta es obligatorio.";
        }
        
        if (cuenta.getEstado() == null) {
            return "El estado de la cuenta es obligatorio.";
        }
        return null;
    }
    
    // genera un número de 12 dígitos que no exista en la base
    public String generarNumeroCuenta() throws SQLException {
        String numero;
        do {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 12; i++) {
                sb.append(ThreadLocalRandom.current().nextInt(10));
            }
            numero = sb.toString();
        } while (cuentaDAO.buscarPorNumero(numero) != null);
        return numero;
    }
    
    // registrar nueva cuenta
    public String registrar(Cuenta cuenta) throws SQLException {
        String errorValidacion = validar(cuenta);
        if (errorValidacion != null) {
            logger.warn("Apertura de cuenta rechazada: {}", errorValidacion);
            return errorValidacion;
        }
        
        // verificar que cliente exista en la bd
        Cliente clienteAsociado = clienteDAO.buscarPorCodigo(cuenta.getCodigoCliente());
        if (clienteAsociado == null) {
            logger.warn("Apertura de cuenta rechazada: cliente {} no existe.", cuenta.getCodigoCliente());
            return "El cliente especificado no existe en el sistema.";
        }
        
        // verificar que cliente esté activo
        if (clienteAsociado.getEstado() != EstadoCliente.ACTIVO) {
            logger.warn("Apertura de cuenta rechazada: cliente {} en estado {}.", clienteAsociado.getDui(), clienteAsociado.getEstado());
            return "No se puede abrir una cuenta a un cliente que se encuentra Inactivo o Bloqueado.";
        }
        
        // verificar si # cuenta ya existe
        if (cuentaDAO.buscarPorNumero(cuenta.getNumeroCuenta()) != null) {
            return "El número de cuenta ya se encuentra registrado en el sistema.";
        }
        
        cuentaDAO.insertar(cuenta);
        logger.info("Nueva cuenta creada | N°: {} | Tipo: {} | Saldo inicial: ${} | Cliente ID: {}",
                cuenta.getNumeroCuenta(), cuenta.getTipoCuenta(), cuenta.getSaldo(), cuenta.getCodigoCliente());
        return null;
    }
    
    public void cambiarEstadoCuenta(String numeroCuenta, EstadoCuenta nuevoEstado) throws BancoException, SQLException {
        Cuenta cuenta = cuentaDAO.buscarPorNumero(numeroCuenta);
        if (cuenta == null) {
            throw new CuentaNoEncontradaException("No se encontró la cuenta " + numeroCuenta);
        }
        cuentaDAO.cambiarEstado(cuenta.getIdCuenta(), nuevoEstado);
        logger.info("Estado de cuenta {} actualizado a {}", numeroCuenta, nuevoEstado);
    }
    
    // listar cuentas de cliente especifico
    public List<Cuenta> listarCuentasPorCliente(int codigoCliente) throws SQLException {
        return cuentaDAO.listarPorCliente(codigoCliente);
    }
    
    // buscar cuenta por numero
    public Cuenta buscarPorNumero(String numeroCuenta) throws SQLException {
        return cuentaDAO.buscarPorNumero(numeroCuenta);
    }
}
