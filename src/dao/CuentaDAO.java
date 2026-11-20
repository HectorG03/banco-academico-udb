package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import modelo.Cuenta;
import modelo.EstadoCuenta;
import modelo.TipoCuenta;

public class CuentaDAO {
    
    // insertar nueva cuenta
    public void insertar (Cuenta cuenta) throws SQLException {
        String sql = "INSERT INTO cuentas (numero_cuenta, tipo_cuenta, saldo, estado, fecha_apertura, codigo_cliente) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, cuenta.getNumeroCuenta());
            ps.setString(2, cuenta.getTipoCuenta().name().replace("_", " "));
            ps.setBigDecimal(3, cuenta.getSaldo());
            ps.setString(4, cuenta.getEstado().name());
            ps.setObject(5, cuenta.getFechaApertura()); 
            ps.setInt(6, cuenta.getCodigoCliente());
            
            ps.executeUpdate();
        }
    }
    
    // listar todas las cuentas que pertenecen a un cliente especifico
    public List<Cuenta> listarPorCliente(int codigoCliente) throws SQLException {
        List<Cuenta> lista = new ArrayList<>();
        
        String sql = "SELECT id_cuenta, numero_cuenta, tipo_cuenta, saldo, estado, fecha_apertura, codigo_cliente "
                   + "FROM cuentas WHERE codigo_cliente = ?";
        
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setInt(1, codigoCliente);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearCuenta(rs));
                }
            }
        }
        return lista;
    }
    
    // buscar cuenta por numero de cuenta
    public Cuenta buscarPorNumero(String numeroCuenta) throws SQLException {
        String sql = "SELECT id_cuenta, numero_cuenta, tipo_cuenta, saldo, estado, fecha_apertura, codigo_cliente "
                   + "FROM cuentas WHERE numero_cuenta = ?";
        Cuenta cuenta = null;
        
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, numeroCuenta);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    cuenta = mapearCuenta(rs);
                }
            }
        }
        return cuenta;
    }
    
    // transformar fila de la bs en un objeto
    private Cuenta mapearCuenta(ResultSet rs) throws SQLException {
        Cuenta cuenta = new Cuenta();
        cuenta.setIdCuenta(rs.getInt("id_cuenta"));
        cuenta.setNumeroCuenta(rs.getString("numero_cuenta"));
        cuenta.setTipoCuenta(TipoCuenta.valueOf(rs.getString("tipo_cuenta").toUpperCase().replace(" ", "_")));
        cuenta.setSaldo(rs.getBigDecimal("saldo"));
        cuenta.setEstado(EstadoCuenta.valueOf(rs.getString("estado").toUpperCase()));
        cuenta.setFechaApertura(rs.getDate("fecha_apertura").toLocalDate());
        cuenta.setCodigoCliente(rs.getInt("codigo_cliente"));
        return cuenta;
    }
}
