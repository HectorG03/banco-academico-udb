package servicio;

import dao.ClienteDAO;
import dao.CuentaDAO;
import dao.MovimientoDAO;
import excepciones.BancoException;
import excepciones.CuentaNoEncontradaException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import modelo.Cliente;
import modelo.Cuenta;
import modelo.EstadoCuenta;
import modelo.Movimiento;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ReporteServicio {

    private static final Logger logger = LogManager.getLogger(ReporteServicio.class);
    private final CuentaDAO cuentaDAO;
    private final ClienteDAO clienteDAO;
    private final MovimientoDAO movimientoDAO;

    public ReporteServicio() {
        this.cuentaDAO = new CuentaDAO();
        this.clienteDAO = new ClienteDAO();
        this.movimientoDAO = new MovimientoDAO();
    }

    // Reporte 1: Cuentas activas
    public List<Cuenta> obtenerCuentasActivas() throws SQLException {
        logger.info("Generando reporte: Cuentas Activas.");
        return cuentaDAO.listarPorEstado(EstadoCuenta.ACTIVA);
    }

    // Reporte 2: Cuentas bloqueadas
    public List<Cuenta> obtenerCuentasBloqueadas() throws SQLException {
        logger.info("Generando reporte: Cuentas Bloqueadas.");
        return cuentaDAO.listarPorEstado(EstadoCuenta.BLOQUEADA);
    }

    // Reporte 3: Clientes registrados
    public List<Cliente> obtenerClientesRegistrados() throws SQLException {
        logger.info("Generando reporte: Clientes Registrados.");
        return clienteDAO.listar();
    }

    // Reporte 4: Historial de transacciones
    public List<Movimiento> obtenerHistorialTransacciones() throws SQLException {
        logger.info("Generando reporte: Historial General de Transacciones.");
        return movimientoDAO.listarTodos();
    }

    // Reporte 5: Transacciones por cuenta
    public List<Movimiento> obtenerTransaccionesPorCuenta(String numeroCuenta) throws BancoException, SQLException {
        if (numeroCuenta == null || numeroCuenta.trim().isEmpty()) {
            throw new BancoException("Ingrese un número de cuenta de 12 dígitos para generar el reporte.");
        }
        if (cuentaDAO.buscarPorNumero(numeroCuenta.trim()) == null) {
            logger.warn("Reporte por cuenta fallido: cuenta {} no encontrada.", numeroCuenta);
            throw new CuentaNoEncontradaException("La cuenta " + numeroCuenta + " no existe.");
        }
        logger.info("Generando reporte: Transacciones de la cuenta {}.", numeroCuenta);
        return movimientoDAO.listarPorCuenta(numeroCuenta.trim());
    }

    // Reportes 6, 7 y 8: Total depositado [0], Total retirado [1], Total transferido [2]
    public BigDecimal[] obtenerTotalesGenerales() throws SQLException {
        logger.info("Consultando totales generales (Depositado, Retirado, Transferido).");
        return movimientoDAO.obtenerResumenTotales();
    }
}