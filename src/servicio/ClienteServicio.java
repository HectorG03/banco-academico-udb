package servicio;

import dao.ClienteDAO;
import modelo.Cliente;
import modelo.EstadoCliente;

import java.sql.SQLException;
import java.util.List;

public class ClienteServicio {
    
    private final ClienteDAO clienteDAO;
    
    public ClienteServicio() {
        this.clienteDAO = new ClienteDAO();
    }
    
    // validaciones
    public String validar(Cliente cliente) {
        if (cliente == null){
            return "Cliente no puede ser nulo.";
        }
        
        if (cliente.getDui() == null || cliente.getDui().trim().isEmpty()){
            return "El número de DUI es obligatorio.";
        }
        
        if (!cliente.getDui().matches("^[0-9]{8}-[0-9]{1}$")){
            return "El formato del DUI es inválido.";
        }
        
        if(cliente.getNombre() == null || cliente.getNombre().trim().isEmpty()){
            return "El nombre completo es obligatorio.";
        }
        
        if (cliente.getTelefono() == null || cliente.getTelefono().trim().isEmpty()){
            return "El número de teléfono es obligatorio.";
        }
        
        if (!cliente.getTelefono().matches("^[0-9]{4}-[0-9]{4}$")) {
            return "El teléfono debe tener el formato ####-####.";
        }
        
        if (cliente.getCorreo() == null || cliente.getCorreo().trim().isEmpty()) {
            return "El correo electrónico es obligatorio.";
        }
        
        if (!cliente.getCorreo().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            return "El formato del correo electrónico es inválido.";
        }
        
        if (cliente.getDireccion() == null || cliente.getDireccion().trim().isEmpty()){
            return "La dirección es obligatoria.";
        }
        
        return null;
    }
    
    // registrar nuevo cliente
    public String registrar(Cliente cliente) throws SQLException {
        
        String errorValidacion = validar(cliente);
        if(errorValidacion != null){
            return errorValidacion;
        }
        
        // por defecto asignamos ACTIVO
        if (cliente.getEstado() == null) {
            cliente.setEstado(EstadoCliente.ACTIVO);
        }
        
        // verificar dui no repetido
        
        if(clienteDAO.buscarPorDui(cliente.getDui()) != null){
            return "El número de DUI ya se encuentra registrado en el sistema.";
        }
        
        // correo no repetido
        if (clienteDAO.buscarPorCorreo(cliente.getCorreo()) != null) {
            return "El correo electrónico ya se encuentra registrado por otro cliente.";
        }
        
        clienteDAO.insertar(cliente);
        return null;
    }
    
    // modificar cliente existente
    public String modificar(Cliente cliente) throws SQLException {
        String errorValidacion = validar(cliente);
        if(errorValidacion != null) {
           
                return errorValidacion;
            }
        
        if (cliente.getEstado() == null) {
            return "El estado del cliente es obligatorio.";
        }
        
        // verificar si dui pertenece a otro cliente
        Cliente clienteConMismoDui = clienteDAO.buscarPorDui(cliente.getDui());
        
        if (clienteConMismoDui != null && clienteConMismoDui.getCodigoCliente() != cliente.getCodigoCliente()) {
            return "El DUI ingresado ya pertenece a otro cliente registrado.";
        }
        
        Cliente clienteConMismoCorreo = clienteDAO.buscarPorCorreo(cliente.getCorreo());
        if (clienteConMismoCorreo != null && clienteConMismoCorreo.getCodigoCliente() != cliente.getCodigoCliente()) {
            return "El correo electrónico ya pertenece a otro cliente registrado.";
        }

        if (!clienteDAO.actualizar(cliente)) {
            return "No se pudo actualizar el registro del cliente.";
        }        
        
        return null;
    }
    
    // cambiar estado cliente
    public String cambiarEstado(int codigoCliente, EstadoCliente nuevoEstado) throws SQLException {
        boolean cambiado = clienteDAO.cambiarEstado(codigoCliente, nuevoEstado);
        
        if (!cambiado){
            return "No se pudo cambiar el estado del cliente.";
        }
        return null;        
    }
    
    // listar clientes
    public List<Cliente> listarClientes() throws SQLException {
        return clienteDAO.listar();
    }
    
    // buscar por codigo
    public Cliente buscarPorCodigo(int codigoCliente) throws SQLException {
        return clienteDAO.buscarPorCodigo(codigoCliente);
    }
    
}
