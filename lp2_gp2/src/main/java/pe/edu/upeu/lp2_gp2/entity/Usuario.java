package pe.edu.upeu.lp2_gp2.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "usuario")
public class Usuario {

    @Id // ID-USUARIO (NÚMERICO)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idUsuario;

    // STRING USUARIO
    private String usuario;

    // NOMBRE (STRING)
    @Column(unique = true, nullable = false)
    private String nombre;

    // PASSWORD | COLUMNS
    @Column(nullable = false)
    private String password;

    // ROLES | COLUMNS
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rol;

    // PERSONA - OneToOne (Usuario c/u)
    @OneToOne
    @JoinColumn(name ="id_persona")
    private Persona persona;

    public Usuario() {
    }

    // CONSTRUCTOR COMPLETO USUARIO
    public Usuario(Long idUsuario, Persona persona, Rol rol, String password, String nombre, String usuario) {
        this.idUsuario = idUsuario;
        this.persona = persona;
        this.rol = rol;
        this.password = password;
        this.nombre = nombre;
        this.usuario = usuario;
    }

    // GETTERS AND SETTERS - CLASE USUARIO
    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }


    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public Persona getPersona() {
        return persona;
    }

    public void setPersona(Persona persona) {
        this.persona = persona;
    }
}
