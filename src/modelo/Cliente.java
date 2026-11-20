package modelo;

public class Cliente {
    private int codigoCliente;
    private String dui,nombre, telefono, correo, direccion;
    private EstadoCliente estado;
    
    public Cliente(){
        
    }

    public Cliente(int codigoCliente, String dui, String nombre, String telefono, String correo, String direccion, EstadoCliente estado) {
        this.codigoCliente = codigoCliente;
        this.dui = dui;
        this.nombre = nombre;
        this.telefono = telefono;
        this.correo = correo;
        this.direccion = direccion;
        this.estado = estado;
    }

    public int getCodigoCliente() {
        return codigoCliente;
    }

    public void setCodigoCliente(int codigoCliente) {
        this.codigoCliente = codigoCliente;
    }

    public String getDui() {
        return dui;
    }

    public void setDui(String dui) {
        this.dui = dui;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public EstadoCliente getEstado() {
        return estado;
    }

    public void setEstado(EstadoCliente estado) {
        this.estado = estado;
    }
    
@Override
    public String toString() {
        return "Cliente [Código=" + codigoCliente + ", DUI=" + dui + ", Nombre=" + nombre + ", Estado=" + estado + "]";
    }
}