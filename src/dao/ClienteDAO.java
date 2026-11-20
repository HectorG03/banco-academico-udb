package dao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.sql.ResultSet;

import modelo.Cliente;
import modelo.EstadoCliente;

public class ClienteDAO {
    
    public void insertar(Cliente p) throws SQLException {
        String sql = "INSERT INTO clientes (dui, nombre, telefono, correo, direccion, estado) "
            + "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, p.getDui());
            ps.setString(2, p.getNombre());
            ps.setString(3, p.getTelefono());
            ps.setString(4, p.getCorreo());
            ps.setString(5, p.getDireccion());
            ps.setString(6, p.getEstado().name());
            
            ps.executeUpdate();
        }
    }
    
    // listar clientes
    public List<Cliente> listar() throws SQLException{
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT codigo_cliente, dui, nombre, telefono, correo, direccion, estado FROM clientes";
        
        try (Connection con = ConexionBD.getConexion();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {
            
            while(rs.next()){
                lista.add(mapearCliente(rs));
            }            
        }
        return lista;
    }
    
    // buscar cliente por código    
    public Cliente buscarPorCodigo(int codigoCliente) throws SQLException {
        String sql = "SELECT codigo_cliente, dui, nombre, telefono, correo, direccion, estado FROM clientes WHERE codigo_cliente = ?";
        Cliente cliente = null;
        
        try (Connection con = ConexionBD.getConexion();                
            PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setInt(1, codigoCliente);
            
            try (ResultSet rs = ps.executeQuery()){
                if(rs.next()){
                    cliente = mapearCliente(rs);
                }
            }
        }
        return cliente;
    }
    
    // buscar cliente por dui
    public Cliente buscarPorDui(String dui) throws SQLException {
        String sql = "SELECT codigo_cliente, dui, nombre, telefono, correo, direccion, estado FROM clientes WHERE dui = ?";
        Cliente cliente = null;
        
        try (Connection con = ConexionBD.getConexion();
            PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, dui);
            
            try (ResultSet rs = ps.executeQuery()){
                if (rs.next()){
                    cliente = mapearCliente(rs);
                }
            }
        }
        return cliente;
    }
    
    // buscar cliente por correo
    public Cliente buscarPorCorreo(String correo) throws SQLException {
        String sql = "SELECT codigo_cliente, dui, nombre, telefono, correo, direccion, estado FROM clientes WHERE correo = ?";
        Cliente cliente = null;
        
        try (Connection con = ConexionBD.getConexion();
            PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, correo);
            
            try (ResultSet rs = ps.executeQuery()){
                if (rs.next()){
                    cliente = mapearCliente(rs);
                }
            }
        }
        return cliente;
    }
    
    
    // actualizar datos cliente
    public boolean actualizar(Cliente cliente) throws SQLException {
        String sql = "UPDATE clientes SET dui = ?, nombre = ?, telefono = ?, correo = ?, direccion = ?, estado = ? WHERE codigo_cliente = ?";
        
        try (Connection con = ConexionBD.getConexion();
            PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, cliente.getDui());
            ps.setString(2, cliente.getNombre());
            ps.setString(3, cliente.getTelefono());
            ps.setString(4, cliente.getCorreo());
            ps.setString(5, cliente.getDireccion());
            ps.setString(6, cliente.getEstado().name());
            ps.setInt(7, cliente.getCodigoCliente());
            
            return ps.executeUpdate() > 0;
        }
    }
    
    // cambiar estado
    public boolean cambiarEstado(int codigoCliente, EstadoCliente nuevoEstado) throws SQLException {
        String sql = "UPDATE clientes SET estado = ? WHERE codigo_cliente = ?";
        
        try (Connection con = ConexionBD.getConexion();
            PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, nuevoEstado.name());
            ps.setInt(2, codigoCliente);
            
            return ps.executeUpdate() > 0;            
        }
    }
    
    //metodo para mapear resultset
    private Cliente mapearCliente(ResultSet rs) throws SQLException {
        Cliente cliente = new Cliente();
        cliente.setCodigoCliente(rs.getInt("codigo_cliente"));
        cliente.setDui(rs.getString("dui"));
        cliente.setNombre(rs.getString("nombre"));
        cliente.setTelefono(rs.getString("telefono"));
        cliente.setCorreo(rs.getString("correo"));
        cliente.setDireccion(rs.getString("direccion"));
        
        // String de la bd a enum de Java
        cliente.setEstado(EstadoCliente.valueOf(rs.getString("estado").toUpperCase())); // Activo > ACTIVO
        return cliente;        
    }
}