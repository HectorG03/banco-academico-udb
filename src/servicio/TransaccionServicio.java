package servicio;

import dao.ConexionBD;
import dao.CuentaDAO;
import dao.MovimientoDAO;
import excepciones.BancoException;
import excepciones.CuentaInactivaException;
import excepciones.CuentaNoEncontradaException;
import excepciones.MontoInvalidoException;
import excepciones.SaldoInsuficienteException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import modelo.Cuenta;
import modelo.EstadoCuenta;
import modelo.Movimiento;
import modelo.TipoMovimiento;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TransaccionServicio {

    private static final Logger logger = LogManager.getLogger(TransaccionServicio.class);
    private final CuentaDAO cuentaDAO;
    private final MovimientoDAO movimientoDAO;

    public TransaccionServicio() {
        this.cuentaDAO = new CuentaDAO();
        this.movimientoDAO = new MovimientoDAO();
    }

    // DEPÓSITO
    public Movimiento realizarDeposito(String numeroCuentaDestino, BigDecimal monto, String descripcion)
            throws BancoException, SQLException {

        validarMonto(monto);
        BigDecimal montoNormalizado = monto.setScale(2, RoundingMode.HALF_UP);

        if (numeroCuentaDestino == null || numeroCuentaDestino.trim().isEmpty()) {
            logger.warn("Depósito rechazado: número de cuenta destino vacío.");
            throw new CuentaNoEncontradaException("Debe ingresar el número de cuenta destino.");
        }

        Cuenta cuentaDestino = cuentaDAO.buscarPorNumero(numeroCuentaDestino.trim());
        if (cuentaDestino == null) {
            logger.warn("Depósito rechazado: la cuenta destino {} no existe.", numeroCuentaDestino);
            throw new CuentaNoEncontradaException("La cuenta destino " + numeroCuentaDestino + " no existe.");
        }

        if (cuentaDestino.getEstado() != EstadoCuenta.ACTIVA) {
            logger.warn("Depósito rechazado: la cuenta {} está en estado {}.", numeroCuentaDestino, cuentaDestino.getEstado());
            throw new CuentaInactivaException("La cuenta destino no está activa (Estado actual: " + cuentaDestino.getEstado() + ").");
        }

        Connection con = null;
        try {
            con = ConexionBD.getConexion();
            con.setAutoCommit(false);

            BigDecimal nuevoSaldo = cuentaDestino.getSaldo().add(montoNormalizado);
            cuentaDAO.actualizarSaldo(con, cuentaDestino.getIdCuenta(), nuevoSaldo);

            Movimiento mov = new Movimiento();
            mov.setTipoMovimiento(TipoMovimiento.DEPOSITO);
            mov.setMonto(montoNormalizado);
            mov.setFechaHora(LocalDateTime.now());
            mov.setDescripcion((descripcion == null || descripcion.trim().isEmpty()) ? "Depósito en ventanilla" : descripcion.trim());
            mov.setIdCuentaOrigen(null);
            mov.setIdCuentaDestino(cuentaDestino.getIdCuenta());
            mov.setNumeroCuentaDestino(cuentaDestino.getNumeroCuenta());

            movimientoDAO.insertar(con, mov);
            con.commit();

            logger.info("DEPÓSITO exitoso | ID Mov: {} | Cuenta: {} | Monto: ${} | Nuevo Saldo: ${}",
                    mov.getIdMovimiento(), cuentaDestino.getNumeroCuenta(), montoNormalizado, nuevoSaldo);
            return mov;

        } catch (SQLException ex) {
            if (con != null) {
                con.rollback();
            }
            logger.error("Error SQL durante depósito en cuenta {}: {}", numeroCuentaDestino, ex.getMessage(), ex);
            throw ex;
        } finally {
            if (con != null) {
                con.setAutoCommit(true);
                con.close();
            }
        }
    }

    // RETIRO
    public Movimiento realizarRetiro(String numeroCuentaOrigen, BigDecimal monto, String descripcion)
            throws BancoException, SQLException {

        validarMonto(monto);
        BigDecimal montoNormalizado = monto.setScale(2, RoundingMode.HALF_UP);

        if (numeroCuentaOrigen == null || numeroCuentaOrigen.trim().isEmpty()) {
            logger.warn("Retiro rechazado: número de cuenta origen vacío.");
            throw new CuentaNoEncontradaException("Debe ingresar el número de cuenta origen.");
        }

        Cuenta cuentaOrigen = cuentaDAO.buscarPorNumero(numeroCuentaOrigen.trim());
        if (cuentaOrigen == null) {
            logger.warn("Retiro rechazado: la cuenta origen {} no existe.", numeroCuentaOrigen);
            throw new CuentaNoEncontradaException("La cuenta origen " + numeroCuentaOrigen + " no existe.");
        }

        if (cuentaOrigen.getEstado() != EstadoCuenta.ACTIVA) {
            logger.warn("Retiro rechazado: la cuenta {} está en estado {}.", numeroCuentaOrigen, cuentaOrigen.getEstado());
            throw new CuentaInactivaException("La cuenta origen no está activa (Estado actual: " + cuentaOrigen.getEstado() + ").");
        }

        if (cuentaOrigen.getSaldo().compareTo(montoNormalizado) < 0) {
            logger.warn("Retiro rechazado por saldo insuficiente | Cuenta: {} | Saldo: ${} | Intentado: ${}",
                    numeroCuentaOrigen, cuentaOrigen.getSaldo(), montoNormalizado);
            throw new SaldoInsuficienteException("Saldo insuficiente. Saldo disponible: $" + cuentaOrigen.getSaldo());
        }

        Connection con = null;
        try {
            con = ConexionBD.getConexion();
            con.setAutoCommit(false);

            BigDecimal nuevoSaldo = cuentaOrigen.getSaldo().subtract(montoNormalizado);
            cuentaDAO.actualizarSaldo(con, cuentaOrigen.getIdCuenta(), nuevoSaldo);

            Movimiento mov = new Movimiento();
            mov.setTipoMovimiento(TipoMovimiento.RETIRO);
            mov.setMonto(montoNormalizado);
            mov.setFechaHora(LocalDateTime.now());
            mov.setDescripcion((descripcion == null || descripcion.trim().isEmpty()) ? "Retiro en ventanilla" : descripcion.trim());
            mov.setIdCuentaOrigen(cuentaOrigen.getIdCuenta());
            mov.setIdCuentaDestino(null);
            mov.setNumeroCuentaOrigen(cuentaOrigen.getNumeroCuenta());

            movimientoDAO.insertar(con, mov);
            con.commit();

            logger.info("RETIRO exitoso | ID Mov: {} | Cuenta: {} | Monto: ${} | Nuevo Saldo: ${}",
                    mov.getIdMovimiento(), cuentaOrigen.getNumeroCuenta(), montoNormalizado, nuevoSaldo);
            return mov;

        } catch (SQLException ex) {
            if (con != null) {
                con.rollback();
            }
            logger.error("Error SQL durante retiro en cuenta {}: {}", numeroCuentaOrigen, ex.getMessage(), ex);
            throw ex;
        } finally {
            if (con != null) {
                con.setAutoCommit(true);
                con.close();
            }
        }
    }

    // TRANSFERENCIA
    public Movimiento realizarTransferencia(String numeroCuentaOrigen, String numeroCuentaDestino, BigDecimal monto, String descripcion)
            throws BancoException, SQLException {

        validarMonto(monto);
        BigDecimal montoNormalizado = monto.setScale(2, RoundingMode.HALF_UP);

        if (numeroCuentaOrigen == null || numeroCuentaOrigen.trim().isEmpty()
                || numeroCuentaDestino == null || numeroCuentaDestino.trim().isEmpty()) {
            logger.warn("Transferencia rechazada: cuenta origen o destino vacía.");
            throw new CuentaNoEncontradaException("Debe ingresar tanto la cuenta origen como la cuenta destino.");
        }

        if (numeroCuentaOrigen.trim().equals(numeroCuentaDestino.trim())) {
            logger.warn("Transferencia rechazada: cuenta origen y destino iguales ({}).", numeroCuentaOrigen);
            throw new BancoException("La cuenta origen y la cuenta destino no pueden ser la misma.");
        }

        Cuenta cuentaOrigen = cuentaDAO.buscarPorNumero(numeroCuentaOrigen.trim());
        if (cuentaOrigen == null) {
            logger.warn("Transferencia rechazada: cuenta origen {} no encontrada.", numeroCuentaOrigen);
            throw new CuentaNoEncontradaException("La cuenta origen " + numeroCuentaOrigen + " no existe.");
        }

        Cuenta cuentaDestino = cuentaDAO.buscarPorNumero(numeroCuentaDestino.trim());
        if (cuentaDestino == null) {
            logger.warn("Transferencia rechazada: cuenta destino {} no encontrada.", numeroCuentaDestino);
            throw new CuentaNoEncontradaException("La cuenta destino " + numeroCuentaDestino + " no existe.");
        }

        if (cuentaOrigen.getEstado() != EstadoCuenta.ACTIVA) {
            logger.warn("Transferencia rechazada: cuenta origen {} en estado {}.", numeroCuentaOrigen, cuentaOrigen.getEstado());
            throw new CuentaInactivaException("La cuenta origen no está activa (Estado: " + cuentaOrigen.getEstado() + ").");
        }

        if (cuentaDestino.getEstado() != EstadoCuenta.ACTIVA) {
            logger.warn("Transferencia rechazada: cuenta destino {} en estado {}.", numeroCuentaDestino, cuentaDestino.getEstado());
            throw new CuentaInactivaException("La cuenta destino no está activa (Estado: " + cuentaDestino.getEstado() + ").");
        }

        if (cuentaOrigen.getSaldo().compareTo(montoNormalizado) < 0) {
            logger.warn("Transferencia rechazada por saldo insuficiente | Origen: {} | Saldo: ${} | Monto: ${}",
                    numeroCuentaOrigen, cuentaOrigen.getSaldo(), montoNormalizado);
            throw new SaldoInsuficienteException("Saldo insuficiente en cuenta origen. Disponible: $" + cuentaOrigen.getSaldo());
        }

        Connection con = null;
        try {
            con = ConexionBD.getConexion();
            con.setAutoCommit(false);

            BigDecimal nuevoSaldoOrigen = cuentaOrigen.getSaldo().subtract(montoNormalizado);
            BigDecimal nuevoSaldoDestino = cuentaDestino.getSaldo().add(montoNormalizado);

            cuentaDAO.actualizarSaldo(con, cuentaOrigen.getIdCuenta(), nuevoSaldoOrigen);
            cuentaDAO.actualizarSaldo(con, cuentaDestino.getIdCuenta(), nuevoSaldoDestino);

            Movimiento mov = new Movimiento();
            mov.setTipoMovimiento(TipoMovimiento.TRANSFERENCIA);
            mov.setMonto(montoNormalizado);
            mov.setFechaHora(LocalDateTime.now());
            mov.setDescripcion((descripcion == null || descripcion.trim().isEmpty()) ? "Transferencia entre cuentas" : descripcion.trim());
            mov.setIdCuentaOrigen(cuentaOrigen.getIdCuenta());
            mov.setIdCuentaDestino(cuentaDestino.getIdCuenta());
            mov.setNumeroCuentaOrigen(cuentaOrigen.getNumeroCuenta());
            mov.setNumeroCuentaDestino(cuentaDestino.getNumeroCuenta());

            movimientoDAO.insertar(con, mov);
            con.commit();

            logger.info("TRANSFERENCIA exitosa | ID Mov: {} | Origen: {} -> Destino: {} | Monto: ${}",
                    mov.getIdMovimiento(), cuentaOrigen.getNumeroCuenta(), cuentaDestino.getNumeroCuenta(), montoNormalizado);
            return mov;

        } catch (SQLException ex) {
            if (con != null) {
                con.rollback();
            }
            logger.error("Error SQL durante transferencia {} -> {}: {}", numeroCuentaOrigen, numeroCuentaDestino, ex.getMessage(), ex);
            throw ex;
        } finally {
            if (con != null) {
                con.setAutoCommit(true);
                con.close();
            }
        }
    }

    // consultar movimiento por id
    public Movimiento consultarMovimiento(int idMovimiento) throws BancoException, SQLException {
        if (idMovimiento <= 0) {
            throw new BancoException("El ID de movimiento debe ser un número entero positivo.");
        }
        Movimiento mov = movimientoDAO.buscarPorId(idMovimiento);
        if (mov == null) {
            logger.warn("Consulta de movimiento ID {} sin resultados.", idMovimiento);
            throw new BancoException("No se encontró ningún movimiento con el ID " + idMovimiento + ".");
        }
        logger.info("Consulta puntual de movimiento ID {} realizada.", idMovimiento);
        return mov;
    }

    // historial de movimientos
    public List<Movimiento> listarHistorial() throws SQLException {
        return movimientoDAO.listarTodos();
    }

    public List<Movimiento> listarHistorialPorCuenta(String numeroCuenta) throws BancoException, SQLException {
        if (numeroCuenta == null || numeroCuenta.trim().isEmpty()) {
            throw new BancoException("Debe ingresar un número de cuenta para filtrar.");
        }
        if (cuentaDAO.buscarPorNumero(numeroCuenta.trim()) == null) {
            throw new CuentaNoEncontradaException("La cuenta " + numeroCuenta + " no existe en el sistema.");
        }
        return movimientoDAO.listarPorCuenta(numeroCuenta.trim());
    }

    private void validarMonto(BigDecimal monto) throws MontoInvalidoException {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            logger.warn("Operación rechazada: monto inválido ({}).", monto);
            throw new MontoInvalidoException("El monto de la operación debe ser mayor que cero ($0.00).");
        }
    }
}