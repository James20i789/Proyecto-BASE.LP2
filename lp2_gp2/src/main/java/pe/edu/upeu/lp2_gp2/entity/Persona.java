package pe.edu.upeu.lp2_gp2.entity;


import jakarta.persistence.*;

import java.time.LocalDateTime;
@Entity
@Table(name = "persona")
public class Persona {

    @Id // CLASE PERSONA
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPersona;

    // NOMBRE
    @Column(nullable = false, length = 100)
    private String nombre;

    // EMAIL
    @Column(unique = true, nullable = false)
    private String email;

    private String telefono;
    private String direccion;

    // CREADO EN (AUTOCOMPLETADO DE DATOS)
    @Column(name = "creado_en",insertable = false, updatable = false)
    private LocalDateTime creadoEN;

    public Persona() {
    }

    // CONSTRUCTOR COMPLETO PERSONA
    public Persona(Long idPersona, String nombre, String email, String telefono, String direccion, LocalDateTime creadoEN) {
        this.idPersona = idPersona;
        this.nombre = nombre;
        this.email = email;
        this.telefono = telefono;
        this.direccion = direccion;
        this.creadoEN = creadoEN;
    }

    // GETTERS AND SETTERS - CLASE PERSONA
    public Long getIdPersona() {
        return idPersona;
    }

    public void setIdPersona(Long idPersona) {
        this.idPersona = idPersona;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public LocalDateTime getCreadoEN() {
        return creadoEN;
    }

    public void setCreadoEN(LocalDateTime creadoEN) {
        this.creadoEN = creadoEN;
    }
}
